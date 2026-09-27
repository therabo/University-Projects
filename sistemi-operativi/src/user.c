#include "../include/user.h"
#include "../include/random_utils.h"


int user_init(user_t *u,
              int userId,
              double pServ,
              int configMaxRequests,
              int simDuration) {
    if (!u) return -1;

    u->userId = userId;
    u->pServ = pServ;
    u->simDuration = simDuration;
    u->maxRequests = configMaxRequests;


    u->numRequests = 0;
    u->semId = -1;
    u->shmId = -1;
    u->reqTicketQueueId = -1;
    u->replyTicketQueueId = -1;
    u->rcvServiceId = -1;
    u->doneServiceId = -1;
    u->isInQueue = false;
    u->hasBeenServed = false;
    u->sd = NULL;
    return 0;
}


static int64_t user_get_ticket(user_t *u, int serviceType) {
    if (serviceType < 0 || serviceType >= NUM_SERVICES) return -1;

    if (day_ended) return -3;

    msgbuf_req req = {
            .mtype    = MSG_REQ_TICKET,
            .senderId = getpid(),
            .service  = serviceType,
    };


    if (msgsnd(u->reqTicketQueueId, &req, sizeof(req) - sizeof(long), 0) == -1) {
        if (errno == EINTR) {
            if (day_ended) {
                return -2;
            } else {
                return -1;
            }
        } else if (errno == EIDRM) {
            printf("[%s] [User %d] Error sending ticket request: Queue removed (EIDRM).\n", sim_time_str(u->sd),
                   u->userId);
            return -1;
        } else if (errno == EINVAL) {
            printf("[%s] [User %d] Error sending ticket request: Invalid queue ID (EINVAL).\n", sim_time_str(u->sd),
                   u->userId);
            return -1;
        } else {
            perror("user_get_ticket msgsnd");
            return -1;
        }
    }


    printf("[%s] [User %d] Requesting ticket for service=%d, userPid=%d\n", sim_time_str(u->sd), u->userId,
           serviceType, getpid());


    msg_ticket_id ticket;
    errno = 0;
    if (msgrcv(u->replyTicketQueueId, &ticket, sizeof(ticket) - sizeof(long), getpid(), 0) == -1) {

        if (errno == EINTR) {
            if (day_ended) {
                return -2;
            } else {
                return -1;
            }
        } else if (errno == EIDRM) {
            return -1;
        } else {
            perror("user_get_ticket msgrcv");
            return -1;
        }
    }


    printf("[%s] [User %d] Received ticket=%d for service=%d, userPid=%d\n",
           sim_time_str(u->sd), u->userId,
           ticket.n_ticket,
           ticket.service,
           getpid()
    );


    return ticket.ticketId;
}


static double
compute_sim_minutes(const struct timespec *start, const struct timespec *end, unsigned long nanos_per_min) {
    long delta_sec = end->tv_sec - start->tv_sec;
    long delta_nsec = end->tv_nsec - start->tv_nsec;
    long total_nsec = delta_sec * 1000000000LL + delta_nsec;
    return (double) total_nsec / nanos_per_min;
}


static void user_update_stats(user_t *u, int serviceType, double waitTime, double serviceTime, int served) {
    sem_lock(u->semId, SEM_STATS);
    stats_update_service(&u->sd->stats, serviceType, waitTime, serviceTime, served);
    sem_unlock(u->semId, SEM_STATS);
}


static bool wait_start(int msqId, int64_t ticket, struct timespec *t_called, msg_ticket_id *out_msg) {
    ssize_t n;
    errno = 0;

    while (1) {
        n = msgrcv(msqId, out_msg, sizeof(*out_msg) - sizeof(long), ticket, 0);
        if (n >= 0) {
            clock_gettime(CLOCK_MONOTONIC, t_called);
            return true;
        }

        if (errno == EINTR) {
            if (day_ended) {
                return false;
            }
            continue;
        }
        perror("wait_start: msgrcv");
        return false;
    }
}


static bool service_is_offered(user_t *u, int serviceType) {
    bool offered = false;
    if (sem_lock(u->semId, SEM_WINDOWS) < 0) return false;
    for (size_t i = 0; i < (size_t) u->sd->numWindows; ++i) {
        if (u->sd->windows[i].currentServiceType == serviceType) {
            offered = true;
            break;
        }
    }
    sem_unlock(u->semId, SEM_WINDOWS);
    return offered;
}

static bool wait_for_arrival(uint64_t nanoseconds) {
    if (day_ended) return false;
    struct timespec remaining = {
        .tv_sec = (time_t)(nanoseconds / 1000000000ULL),
        .tv_nsec = (long)(nanoseconds % 1000000000ULL)
    };
    while (nanosleep(&remaining, &remaining) < 0) {
        if (errno != EINTR || day_ended) return false;
    }
    return !day_ended;
}

static void user_mark_waiting_at_close(user_t *u) {
    if (sem_lock(u->semId, SEM_STATS) < 0) return;
    stats_count_waiting_user(&u->sd->stats);
    sem_unlock(u->semId, SEM_STATS);
}


void user_run(user_t *u) {

    setup_day_end_handler();

    u->sd = attach_shared_memory(u->shmId);
    if (!u->sd) return;
    if (sem_unlock(u->semId, SEM_READY) < 0) {
        detach_shared_memory(u->sd);
        return;
    }

    unsigned long nanos_per_min = u->sd->nanosec_min;


    for (int day = 1; day <= u->simDuration; ++day) {

        reset_day_end_flag();
        if (sem_lock(u->semId, SEM_DAY_START_US) < 0) break;


        u->numRequests = 0;
        u->hasBeenServed = false;


        double random_prob = sim_random_unit();
        if (random_prob < u->pServ) {
            int requests_today = (int)sim_random_bounded((uint32_t)u->maxRequests) + 1;
            int *requests = malloc((size_t)requests_today * sizeof(int));
            if (!requests) perror("malloc requests");
            if (requests) {
                for (int i = 0; i < requests_today; i++)
                    requests[i] = (int)sim_random_bounded(NUM_SERVICES);
            }
            int arrival_minute = (int)sim_random_bounded((uint32_t)u->sd->minutes_per_day);
            uint64_t arrival_delay = (uint64_t)arrival_minute * nanos_per_min;

            if (requests && wait_for_arrival(arrival_delay)) {
                for (int req_count = 0; req_count < requests_today; ++req_count) {
                    int serviceType = requests[req_count];
                    struct timespec t_ticket_req, t_called;
                    msg_ticket_id call_msg, served_msg;

                    if (day_ended) break;
                    if (!service_is_offered(u, serviceType)) {
                        user_update_stats(u, serviceType, 0, 0, 0);
                        continue;
                    }

                    clock_gettime(CLOCK_MONOTONIC, &t_ticket_req);
                    int64_t ticket = user_get_ticket(u, serviceType);

                    if (ticket < 0) {
                        user_update_stats(u, serviceType, 0, 0, 0);
                        if (ticket == -2) user_mark_waiting_at_close(u);
                        if (ticket == -2 || ticket == -3) break;
                        continue;
                    }

                    u->numRequests++;

                    if (!wait_start(u->rcvServiceId, ticket, &t_called, &call_msg)) {
                        user_update_stats(u, serviceType, 0, 0, 0);
                        if (day_ended) user_mark_waiting_at_close(u);
                        break;
                    }

                    double waitM = compute_sim_minutes(&t_ticket_req, &t_called, nanos_per_min);
                    ssize_t r;
                    do {
                        r = msgrcv(u->doneServiceId, &served_msg,
                                   sizeof(served_msg) - sizeof(long), ticket, 0);
                    } while (r < 0 && errno == EINTR);

                    if (r < 0) {
                        user_update_stats(u, serviceType, waitM, 0.0, 0);
                        break;
                    }

                    double serviceM = served_msg.durationService;
                    user_update_stats(u, served_msg.service, waitM, serviceM, 1);

                    if (!u->hasBeenServed) {
                        sem_lock(u->semId, SEM_STATS);
                        stats_count_user_served(&u->sd->stats);
                        sem_unlock(u->semId, SEM_STATS);
                        u->hasBeenServed = true;
                    }
                }
            }
            free(requests);
        }
        sem_lock(u->semId, SEM_DAY_END_US);
        sem_unlock(u->semId, SEM_DAY_DONE);

    }
    detach_shared_memory(u->sd);
    u->sd = NULL;
}


void user_cleanup(user_t *u) {
    printf("[User %d] Cleaning up.\n", u->userId);
}
