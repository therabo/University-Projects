#include "../include/operator.h"
#include "../include/random_utils.h"

int operator_init(operator_t *op, int operatorId, int maxPauses, int simDuration, int pPause) {
    if (!op) return -1;

    op->pPause = pPause;
    op->operatorId = operatorId;
    op->serviceType = -1;
    op->simDuration = simDuration;
    op->maxPauses = maxPauses;
    op->usedPauses = 0;
    op->windowId = -1;
    op->isWorking = false;
    op->semId = -1;
    op->shmId = -1;
    op->rcvServiceId = -1;
    op->doneServiceId = -1;
    op->notifWindowQueueId = -1;
    op->newServiceId = -1;
    op->sd = NULL;
    return 0;
}


static int find_and_occupy_window(struct SharedData *sd, int semId, int serviceType, int operatorId,
                                  int notifWindowQueueId) {
    int winId = -1;
    while (winId < 0) {
        if (day_ended) {
            printf("[%s] [Operator %d] Day ended before finding window.\n", sim_time_str(sd), operatorId);
            return -1;
        }

        sem_lock(semId, SEM_WINDOWS);
        for (size_t i = 0; i < (size_t)sd->numWindows; ++i) {
            if (sd->windows[i].currentServiceType == serviceType && !sd->windows[i].isOccupied) {
                window_occupy(&sd->windows[i], getpid());
                winId = (int) i;
                break;
            }
        }
        sem_unlock(semId, SEM_WINDOWS);


        if (winId >= 0) {
            printf("[%s] [Operator %d] Occupied window %d for service %d\n",
                   sim_time_str(sd), operatorId, winId, serviceType);
            return winId;
        }


        msgbuf_w msg;
        if (msgrcv(notifWindowQueueId, &msg, sizeof(msg) - sizeof(long), serviceType + 1, 0) < 0) {
            if (errno == EINTR) {
                if (day_ended) {
                    printf("[%s] [Operator %d] Day ended while waiting for window notification.\n", sim_time_str(sd),
                           operatorId);
                    return -1;
                }
                continue;
            }
            if (errno == EIDRM) {
                return -1;
            }
            perror("find_and_occupy_window msgrcv");
            return -1;
        }
    }
    return -1;
}

static void
release_window(struct SharedData *sd, int semId, int winId, int notifWindowQueueId, int operatorId) {
    int serviceType = -1;
    sem_lock(semId, SEM_WINDOWS);
    if (winId >= 0 && (size_t) winId < (size_t)sd->numWindows) {
        serviceType = sd->windows[winId].currentServiceType;
        window_free(&sd->windows[winId]);
        printf("[%s] [Operator %d] Released window %d\n", sim_time_str(sd), operatorId, winId);
    }
    sem_unlock(semId, SEM_WINDOWS);

    if (serviceType < 0) return;
    msgbuf_w notify_msg = {
        .mtype = serviceType + 1,
        .operatorPid = getpid(),
        .windowId = winId
    };
    msgsnd(notifWindowQueueId, &notify_msg, sizeof(notify_msg) - sizeof(long), IPC_NOWAIT);
}


static void increment_active_operators(struct SharedData *sd, int semId) {
    sem_lock(semId, SEM_STATS);
    sd->stats.total_operators_active++;
    sd->stats.daily_operators_active++;
    sem_unlock(semId, SEM_STATS);
}


double simulate_service_time(int serviceType, operator_t *op) {
    double avgMin = (double) TIME_AVERAGE_SERVICES[serviceType];
    double variation_percentage = sim_random_unit() - 0.5;
    double simMins = avgMin * (1.0 + variation_percentage);

    unsigned long nanos_per_min = op->sd->nanosec_min;

    unsigned long nanos = (unsigned long) (simMins * nanos_per_min);
    struct timespec ts = {.tv_sec = nanos / 1000000000ULL, .tv_nsec = nanos % 1000000000ULL};
    while (nanosleep(&ts, &ts) < 0 && errno == EINTR) {}
    return simMins;
}


void operator_run(operator_t *op) {

    setup_day_end_handler();

    op->sd = attach_shared_memory(op->shmId);
    if (!op->sd) return;
    if (sem_unlock(op->semId, SEM_READY) < 0) {
        detach_shared_memory(op->sd);
        return;
    }

    for (int day = 1; day <= op->simDuration; ++day) {

        reset_day_end_flag();

        sem_lock(op->semId, SEM_DAY_START_OP);

        int winId = find_and_occupy_window(op->sd,
                                           op->semId,
                                           op->serviceType,
                                           op->operatorId,
                                           op->notifWindowQueueId);

        if (winId < 0) {
            sem_lock(op->semId, SEM_DAY_END_OP);
            sem_unlock(op->semId, SEM_DAY_DONE);
            continue;
        }

        op->windowId = winId;
        op->isWorking = true;
        increment_active_operators(op->sd, op->semId);

        msg_ticket_id svc;

        while (op->isWorking) {

            if (day_ended) {
                break;
            }

            ssize_t r = msgrcv(op->newServiceId, &svc, sizeof(svc) - sizeof(long), op->serviceType + 1, 0);
            if (r < 0) {
                if (errno == EINTR) {
                    if (day_ended) {
                        break;
                    }
                    continue;
                }
                perror("operator_run msgrcv newServiceId");
                op->isWorking = false;
                break;
            }


            long ticketId = svc.ticketId;
            svc.mtype = ticketId;

            bool call_sent = false;
            while (!call_sent) {
                if (msgsnd(op->rcvServiceId, &svc, sizeof(svc) - sizeof(long), 0) == 0) {
                    call_sent = true;
                    break;
                }
                if (errno == EINTR && !day_ended) continue;
                if (errno != EINTR && errno != EIDRM) perror("operator_run msgsnd rcvServiceId");
                break;
            }
            if (!call_sent) break;

            printf("[%s] [Operator %d] Start service ticket=%d service=%d window=%d userPid=%d\n",
                   sim_time_str(op->sd), op->operatorId,
                   svc.n_ticket,
                   svc.service,
                   op->windowId,
                   svc.user);

            double simMins = simulate_service_time(op->serviceType, op);

            msg_ticket_id doneSvc = {
                    .ticketId     = svc.ticketId,
                    .user = svc.user,
                    .service  = op->serviceType,
                    .durationService = simMins,
                    .operator = getpid(),
                    .n_ticket = svc.n_ticket,
                    .mtype = svc.ticketId
            };

            while (msgsnd(op->doneServiceId, &doneSvc, sizeof(doneSvc) - sizeof(long), 0) < 0) {
                if (errno == EINTR) continue;
                if (errno != EIDRM) perror("operator_run msgsnd doneServiceId");
                break;
            }

            printf("[%s] [Operator %d] End service ticket=%d service=%d window=%d userPid=%d\n",
                   sim_time_str(op->sd), op->operatorId,
                   doneSvc.n_ticket,
                   doneSvc.service,
                   op->windowId,
                   doneSvc.user
            );




            bool take_pause = ((int)sim_random_bounded(100) < op->pPause) && (op->usedPauses < op->maxPauses);

            int activeOpsForService = 0;

            sem_lock(op->semId, SEM_STATS);
            activeOpsForService = op->sd->active_operators_by_service[op->serviceType];
            sem_unlock(op->semId, SEM_STATS);

            if (take_pause && activeOpsForService > 1) {
                sem_lock(op->semId, SEM_STATS);
                op->sd->stats.total_operator_pauses++;
                op->sd->stats.daily_operator_pauses++;
                op->sd->active_operators_by_service[op->serviceType]--;
                sem_unlock(op->semId, SEM_STATS);

                op->usedPauses++;
                printf("[%s] [Operator %d] Pause %d/%d.\n",
                       sim_time_str(op->sd),
                       op->operatorId,
                       op->usedPauses,
                       op->maxPauses);
                op->isWorking = false;
                break;
            }

            if (day_ended) {
                break;
            }

        }

        release_window(op->sd, op->semId, op->windowId, op->notifWindowQueueId, op->operatorId);
        op->isWorking = false;
        op->windowId = -1;

        sem_lock(op->semId, SEM_DAY_END_OP);
        sem_unlock(op->semId, SEM_DAY_DONE);

    }
    detach_shared_memory(op->sd);
    op->sd = NULL;
}


void operator_cleanup(operator_t *op) {
    printf("[Operator Cleanup] id=%d service=%d usedPauses=%d\n",
           op->operatorId,
           op->serviceType,
           op->usedPauses);
}
