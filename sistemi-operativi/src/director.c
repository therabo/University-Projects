#include "../include/director.h"
#include "../include/random_utils.h"
#include <limits.h>

static int create_ipc_resources(director_t *dir);

static int fork_ticket_dispenser(director_t *dir);

static int fork_operators(director_t *dir);

static int fork_users(director_t *dir);

static void print_daily_stats(director_t *dir, int day);

static void print_cumulative_stats(director_t *dir);

static int check_explode_threshold(director_t *dir);

int director_init(director_t *dir, const char *configuration_path) {
    if (!dir) {
        fprintf(stderr, "director_init: invalid pointer.\n");
        return -1;
    }

    memset(dir, 0, sizeof(*dir));
    dir->shmId = -1;
    dir->semId = -1;
    dir->reqTicketQueueId = -1;
    dir->replyTicketQueueId = -1;
    dir->rcvServiceId = -1;
    dir->doneServiceId = -1;

    dir->notifWindowQueueId = -1;
    dir->newServiceId = -1;
    dir->addUserQueueId = -1;
    dir->ticketDispenserPid = -1;

    if (load_configuration(&dir->config, configuration_path) < 0) return -1;

    if (create_ipc_resources(dir) < 0) {
        fprintf(stderr, "[director_init] Failed to create IPC resources.\n");
        director_cleanup(dir);
        return -1;
    }
    return 0;
}

static int create_ipc_resources(director_t *dir) {

    //Shared memory
    int numWindows = dir->config.NOF_WORKER_SEATS;
    size_t size = sizeof(struct SharedData) + numWindows * sizeof(window_t);

    key_t shmKey = 0x1234;
    dir->shmId = create_shared_memory(shmKey, size, 0666 | IPC_CREAT | IPC_EXCL);
    if (dir->shmId < 0) {
        perror("[create_ipc_resources] shmget");
        return -1;
    }

    dir->sd = attach_shared_memory(dir->shmId);
    if (!dir->sd) return -1;

    dir->sd->numWindows = numWindows;
    dir->sd->nanosec_min = dir->config.N_NANO_SECS_PER_MIN;
    dir->sd->minutes_per_day = dir->config.MINS_PER_DAY;
    dir->sd->current_tick = 0;

    stats_init(&dir->sd->stats);


    //Init array number of operators for each service
    for (size_t i = 0; i < NUM_SERVICES; i++) {
        dir->sd->active_operators_by_service[i] = 0;
    }

    //Init windows
    for (int i = 0; i < numWindows; i++) {
        window_init(&dir->sd->windows[i], i);
    }

    //Semaphores
    key_t semKey = 0x2345;
    dir->semId = create_semaphores(semKey, SEM_COUNT, 0666 | IPC_CREAT | IPC_EXCL);
    if (dir->semId < 0) return -1;

    //Queue
    key_t reqKey = 0x3456;
    dir->reqTicketQueueId = msgget(reqKey, 0666 | IPC_CREAT | IPC_EXCL);
    if (dir->reqTicketQueueId < 0) return -1;

    key_t replyTicketKey = 0x8149;
    dir->replyTicketQueueId = msgget(replyTicketKey, 0666 | IPC_CREAT | IPC_EXCL);
    if (dir->replyTicketQueueId < 0) return -1;

    key_t rcvServiceKey = 0x6742;
    dir->rcvServiceId = msgget(rcvServiceKey, 0666 | IPC_CREAT | IPC_EXCL);
    if (dir->rcvServiceId < 0) return -1;

    key_t doneKey = 0x5678;
    dir->doneServiceId = msgget(doneKey, 0666 | IPC_CREAT | IPC_EXCL);
    if (dir->doneServiceId < 0) return -1;

    key_t winKey = 0x4567;
    dir->notifWindowQueueId = msgget(winKey, 0666 | IPC_CREAT | IPC_EXCL);
    if (dir->notifWindowQueueId < 0) return -1;

    key_t newServiceKey = 0x9242;
    dir->newServiceId = msgget(newServiceKey, 0666 | IPC_CREAT | IPC_EXCL);
    if (dir->newServiceId < 0) return -1;

    key_t addUserKey = 0x7777;
    dir->addUserQueueId = msgget(addUserKey, 0666 | IPC_CREAT | IPC_EXCL);
    if (dir->addUserQueueId < 0) return -1;

    return 0;
}


void director_cleanup(director_t *dir) {
    if (dir->ticketDispenserPid > 0)
        kill(dir->ticketDispenserPid, SIGTERM);
    if (dir->userPids) {
        for (int i = 0; i < dir->config.NOF_USERS; i++)
            if (dir->userPids[i] > 0) kill(dir->userPids[i], SIGTERM);
    }
    if (dir->operatorPids) {
        for (int i = 0; i < dir->config.NOF_WORKERS; i++)
            if (dir->operatorPids[i] > 0) kill(dir->operatorPids[i], SIGTERM);
    }


    int status;
    while (waitpid(-1, &status, 0) > 0);

    free(dir->operatorPids);
    free(dir->userPids);
    dir->operatorPids = NULL;
    dir->userPids = NULL;
    if (dir->sd) detach_shared_memory(dir->sd);
    dir->sd = NULL;


    if (dir->rcvServiceId != -1) msgctl(dir->rcvServiceId, IPC_RMID, NULL);
    if (dir->reqTicketQueueId != -1) msgctl(dir->reqTicketQueueId, IPC_RMID, NULL);
    if (dir->doneServiceId != -1) msgctl(dir->doneServiceId, IPC_RMID, NULL);
    if (dir->notifWindowQueueId != -1) msgctl(dir->notifWindowQueueId, IPC_RMID, NULL);
    if (dir->replyTicketQueueId != -1) msgctl(dir->replyTicketQueueId, IPC_RMID, NULL);
    if (dir->newServiceId != -1) msgctl(dir->newServiceId, IPC_RMID, NULL);
    if (dir->addUserQueueId != -1) msgctl(dir->addUserQueueId, IPC_RMID, NULL);
    if (dir->semId != -1) remove_semaphores(dir->semId);
    if (dir->shmId != -1) remove_shared_memory(dir->shmId);

    printf("[Director] cleanup complete\n");
}

static int fork_ticket_dispenser(director_t *dir) {
    pid_t pid = fork();
    if (pid < 0) return -1;
    if (pid == 0) {

        char dispenser_id_str[16];
        char shm_id_str[16];
        char sem_id_str[16];
        char req_ticket_queue_id_str[16];
        char reply_ticket_queue_id_str[16];
        char new_service_id_str[16];

        snprintf(dispenser_id_str, sizeof(dispenser_id_str), "%d", 0);
        snprintf(shm_id_str, sizeof(shm_id_str), "%d", dir->shmId);
        snprintf(sem_id_str, sizeof(sem_id_str), "%d", dir->semId);
        snprintf(req_ticket_queue_id_str, sizeof(req_ticket_queue_id_str), "%d", dir->reqTicketQueueId);
        snprintf(reply_ticket_queue_id_str, sizeof(reply_ticket_queue_id_str), "%d", dir->replyTicketQueueId);
        snprintf(new_service_id_str, sizeof(new_service_id_str), "%d", dir->newServiceId);

        char *argv_td[] = {
                (char*)"./bin/ticket_dispenser_main",
                dispenser_id_str,
                shm_id_str,
                sem_id_str,
                req_ticket_queue_id_str,
                reply_ticket_queue_id_str,
                new_service_id_str,
                NULL
        };
        execve(argv_td[0], argv_td, NULL);
        perror("execve ticket_dispenser_proc failed");
        _exit(EXIT_FAILURE);
    }
    dir->ticketDispenserPid = pid;
    return 0;
}

static int fork_operators(director_t *dir) {
    int n = dir->config.NOF_WORKERS;
    dir->operatorPids = calloc(n, sizeof(pid_t));
    if (!dir->operatorPids) return -1;

    int *serviceTypes = malloc(n * sizeof(int));
    if (!serviceTypes) return -1;

    //Array for operator services
    for (int i = 0; i < n; i++) {
        if (i < NUM_SERVICES)
            serviceTypes[i] = i;
        else
            serviceTypes[i] = (int)sim_random_bounded(NUM_SERVICES);
    }

    sem_lock(dir->semId, SEM_STATS);
    for (int j = 0; j < n; j++) {
        if (serviceTypes[j] >= 0 && serviceTypes[j] < NUM_SERVICES) {
            dir->operatorsByService[serviceTypes[j]]++;
            dir->sd->active_operators_by_service[serviceTypes[j]]++;
        }
    }
    sem_unlock(dir->semId, SEM_STATS);


    for (int j = 0; j < n; j++) {
        pid_t pid = fork();
        if (pid < 0) return -1;
        if (pid == 0) {
            char operator_id_str[16],
                 service_type_str[16],
                 max_pauses_str[16],
                 sim_duration_str[16],
                 p_pause_str[16],
                 shm_id_str[16],
                 sem_id_str[16],
                 rcv_service_id_str[16],
                 done_service_id_str[16],
                 notif_window_queue_id_str[16],
                 new_service_id_str[16];


            snprintf(operator_id_str, sizeof(operator_id_str), "%d", j);
            snprintf(service_type_str, sizeof(service_type_str), "%d", serviceTypes[j]);
            snprintf(max_pauses_str, sizeof(max_pauses_str), "%d", dir->config.NOF_PAUSE);
            snprintf(sim_duration_str, sizeof(sim_duration_str), "%d", dir->config.SIM_DURATION);
            snprintf(p_pause_str, sizeof(p_pause_str), "%d", dir->config.P_PAUSE);
            snprintf(shm_id_str, sizeof(shm_id_str), "%d", dir->shmId);
            snprintf(sem_id_str, sizeof(sem_id_str), "%d", dir->semId);
            snprintf(rcv_service_id_str, sizeof(rcv_service_id_str), "%d", dir->rcvServiceId);
            snprintf(done_service_id_str, sizeof(done_service_id_str), "%d", dir->doneServiceId);
            snprintf(notif_window_queue_id_str, sizeof(notif_window_queue_id_str), "%d", dir->notifWindowQueueId);
            snprintf(new_service_id_str, sizeof(new_service_id_str), "%d", dir->newServiceId);

            char *argv_operator[] = {
                    (char *) "./bin/operator_main",
                    operator_id_str,
                    service_type_str,
                    max_pauses_str,
                    sim_duration_str,
                    p_pause_str,
                    shm_id_str,
                    sem_id_str,
                    rcv_service_id_str,
                    done_service_id_str,
                    notif_window_queue_id_str,
                    new_service_id_str,
                    NULL
            };
            execve(argv_operator[0], argv_operator, NULL);
            perror("execve operator_proc failed");
            _exit(EXIT_FAILURE);
        }
        dir->operatorPids[j] = pid;
    }
    free(serviceTypes);
    return 0;
}


static double compute_random_pServ(const Configuration *cfg) {
    double minP = (double)cfg->P_SERV_MIN / 100.0;
    double maxP = (double)cfg->P_SERV_MAX / 100.0;
    return minP + sim_random_unit() * (maxP - minP);
}


static int fork_users(director_t *dir) {
    int n = dir->config.NOF_USERS;
    dir->userPids = calloc(n, sizeof(pid_t));
    if (!dir->userPids) return -1;

    int maxReq = dir->config.MAX_REQUEST;

    for (int i = 0; i < n; i++) {
        pid_t pid = fork();
        if (pid < 0) return -1;
        if (pid == 0) {

            char user_id_str[16],
                 p_serv_str[16],
                 max_requests_str[16],
                 sim_duration_str[16],
                 shm_id_str[16],
                 sem_id_str[16],
                 req_ticket_queue_id_str[16],
                 reply_ticket_queue_id_str[16],
                 rcv_service_id_str[16],
                 done_service_id_str[16];

            double pServ = compute_random_pServ(&dir->config);

            snprintf(user_id_str, sizeof(user_id_str), "%d", i);
            snprintf(p_serv_str, sizeof(p_serv_str), "%f", pServ);
            snprintf(max_requests_str, sizeof(max_requests_str), "%d", maxReq);
            snprintf(sim_duration_str, sizeof(sim_duration_str), "%d", dir->config.SIM_DURATION);
            snprintf(shm_id_str, sizeof(shm_id_str), "%d", dir->shmId);
            snprintf(sem_id_str, sizeof(sem_id_str), "%d", dir->semId);
            snprintf(req_ticket_queue_id_str, sizeof(req_ticket_queue_id_str), "%d", dir->reqTicketQueueId);
            snprintf(reply_ticket_queue_id_str, sizeof(reply_ticket_queue_id_str), "%d", dir->replyTicketQueueId);
            snprintf(rcv_service_id_str, sizeof(rcv_service_id_str), "%d", dir->rcvServiceId);
            snprintf(done_service_id_str, sizeof(done_service_id_str), "%d", dir->doneServiceId);

            char *argv_user[] = {
                    (char *) "./bin/user_main",
                    user_id_str,
                    p_serv_str,
                    max_requests_str,
                    sim_duration_str,
                    shm_id_str,
                    sem_id_str,
                    req_ticket_queue_id_str,
                    reply_ticket_queue_id_str,
                    rcv_service_id_str,
                    done_service_id_str,
                    NULL
            };
            execve(argv_user[0], argv_user, NULL);
            perror("execve user_proc failed");
            _exit(EXIT_FAILURE);
        }
        dir->userPids[i] = pid;
    }
    return 0;
}

int director_create_processes(director_t *dir) {
    if (fork_ticket_dispenser(dir) < 0) return -1;
    if (fork_operators(dir) < 0) return -1;
    if (fork_users(dir) < 0) return -1;
    return sem_wait_count(dir->semId, SEM_READY,
                          dir->config.NOF_WORKERS + dir->config.NOF_USERS + 1, 30);
}

static int handle_add_users(director_t *dir) {
    msgbuf_add addMsg;
    while (msgrcv(dir->addUserQueueId, &addMsg, sizeof(addMsg.count),
                  MSG_ADD_USERS, IPC_NOWAIT) >= 0) {
        int toAdd = addMsg.count;
        int oldU = dir->config.NOF_USERS;
        if (toAdd <= 0 || toAdd > SHRT_MAX - dir->config.NOF_WORKERS - 1 - oldU) {
            fprintf(stderr, "[Director] Invalid additional user count: %d\n", toAdd);
            continue;
        }
        int newU = oldU + toAdd;

        pid_t *newPids = realloc(dir->userPids, (size_t)newU * sizeof(pid_t));
        if (!newPids) {
            perror("realloc userPids");
            return -1;
        }
        dir->userPids = newPids;

        for (int j = oldU; j < newU; j++) {

            double pServ = compute_random_pServ(&dir->config);

            pid_t pid = fork();
            if (pid < 0) {
                perror("fork new users");
                dir->config.NOF_USERS = j;
                return -1;
            }
            if (pid == 0) {

                char user_id_str[16],
                     p_serv_str[16],
                     max_requests_str[16],
                     sim_duration_str[16],
                     shm_id_str[16],
                     sem_id_str[16],
                     req_ticket_queue_id_str[16],
                     reply_ticket_queue_id_str[16],
                     rcv_service_id_str[16],
                     done_service_id_str[16];

                snprintf(user_id_str,    sizeof(user_id_str),    "%d", j);
                snprintf(p_serv_str,  sizeof(p_serv_str),  "%f", pServ);
                snprintf(max_requests_str,sizeof(max_requests_str),  "%d", dir->config.MAX_REQUEST);
                snprintf(sim_duration_str,sizeof(sim_duration_str),  "%d", dir->config.SIM_DURATION - dir->currentDay + 1);
                snprintf(shm_id_str,   sizeof(shm_id_str),     "%d", dir->shmId);
                snprintf(sem_id_str,   sizeof(sem_id_str),     "%d", dir->semId);
                snprintf(req_ticket_queue_id_str,   sizeof(req_ticket_queue_id_str),     "%d", dir->reqTicketQueueId);
                snprintf(reply_ticket_queue_id_str,   sizeof(reply_ticket_queue_id_str),     "%d", dir->replyTicketQueueId);
                snprintf(rcv_service_id_str,   sizeof(rcv_service_id_str),     "%d", dir->rcvServiceId);
                snprintf(done_service_id_str,  sizeof(done_service_id_str),    "%d", dir->doneServiceId);

                char *argv[] = {
                        "./bin/user_main",
                        user_id_str,
                        p_serv_str,
                        max_requests_str,
                        sim_duration_str,
                        shm_id_str,
                        sem_id_str,
                        req_ticket_queue_id_str,
                        reply_ticket_queue_id_str,
                        rcv_service_id_str,
                        done_service_id_str,
                        NULL
                };
                execve(argv[0], argv, NULL);
                perror("execve user_main");
                _exit(EXIT_FAILURE);
            }
            dir->userPids[j] = pid;
        }
        dir->config.NOF_USERS = newU;
        if (sem_wait_count(dir->semId, SEM_READY, toAdd, 30) < 0) return -1;
        printf("[Director] Added %d new users\n", toAdd);
    }
    if (errno != ENOMSG) {
        perror("msgrcv add users");
        return -1;
    }
    return 0;
}

static void print_daily_stats(director_t *dir, int day) {
    sem_lock(dir->semId, SEM_STATS);
    stats_print_daily(&dir->sd->stats, day);
    sem_unlock(dir->semId, SEM_STATS);
}

static void print_cumulative_stats(director_t *dir) {
    sem_lock(dir->semId, SEM_STATS);
    stats_print_cumulative(&dir->sd->stats);
    sem_unlock(dir->semId, SEM_STATS);
}


static void cleanQueues(director_t *dir) {
    msg_ticket_id buf_t;
    msgbuf_req buf_req;
    while (msgrcv(dir->reqTicketQueueId, &buf_req, sizeof(buf_req) - sizeof(long), 0, IPC_NOWAIT) >= 0) {}

    while (msgrcv(dir->rcvServiceId, &buf_t, sizeof(buf_t) - sizeof(long), 0, IPC_NOWAIT) >= 0) {}

    while (msgrcv(dir->replyTicketQueueId, &buf_t, sizeof(buf_t) - sizeof(long), 0, IPC_NOWAIT) >= 0) {}

    msgbuf_w buf_w;
    while (msgrcv(dir->notifWindowQueueId, &buf_w, sizeof(buf_w) - sizeof(long), 0, IPC_NOWAIT) >= 0) {}

    while (msgrcv(dir->doneServiceId, &buf_t, sizeof(buf_t) - sizeof(long), 0, IPC_NOWAIT) >= 0) {}

    while (msgrcv(dir->newServiceId, &buf_t, sizeof(buf_t) - sizeof(long), 0, IPC_NOWAIT) >= 0) {}
}

static int check_explode_threshold(director_t *dir) {
    int waiting = dir->sd->stats.daily_users_waiting_at_close;
    if (waiting <= dir->config.EXPLODE_THRESHOLD) return 0;
    printf("[Director] Explode threshold superato: %d utenti in attesa (soglia=%d)\n",
           waiting, dir->config.EXPLODE_THRESHOLD);
    return 1;
}

int daily_routine(director_t *dir) {
    const char *csvPath = "data/stats.csv";
    const char *termination = "timeout";
    int result = -1;

    if (stats_csv_begin(csvPath) < 0) goto finish;

    for (int day = 1; day <= dir->config.SIM_DURATION; day++) {
        dir->currentDay = day;
        union semun arg;
        arg.val = 0;
        if (semctl(dir->semId, SEM_DAY_END_OP, SETVAL, arg) < 0 ||
            semctl(dir->semId, SEM_DAY_END_US, SETVAL, arg) < 0 ||
            semctl(dir->semId, SEM_DAY_START_OP, SETVAL, arg) < 0 ||
            semctl(dir->semId, SEM_DAY_START_US, SETVAL, arg) < 0 ||
            semctl(dir->semId, SEM_DAY_DONE, SETVAL, arg) < 0) {
            perror("semctl day reset");
            goto finish;
        }

        if (sem_lock(dir->semId, SEM_STATS) < 0) goto finish;
        stats_reset_daily(&dir->sd->stats);
        memcpy(dir->sd->active_operators_by_service, dir->operatorsByService,
               sizeof(dir->operatorsByService));
        if (sem_unlock(dir->semId, SEM_STATS) < 0) goto finish;
        dir->sd->current_tick = 0;

        if (handle_add_users(dir) < 0) goto finish;
        printf("[%s] === Start day %d ===\n", sim_time_str(dir->sd), day);
        for (int w = 0; w < dir->sd->numWindows; w++) {
            int service = ((w % NUM_SERVICES) + (day - 1) % NUM_SERVICES) % NUM_SERVICES;
            window_set_service(&dir->sd->windows[w], service);
            printf("[%s]   [Day %d] Window %d ⇒ service %d\n",
                   sim_time_str(dir->sd), day, w, service);
        }

        for (int i = 0; i < dir->config.NOF_WORKERS; i++)
            if (sem_unlock(dir->semId, SEM_DAY_START_OP) < 0) goto finish;
        for (int i = 0; i < dir->config.NOF_USERS; i++)
            if (sem_unlock(dir->semId, SEM_DAY_START_US) < 0) goto finish;

        for (int minute = 0; minute < dir->config.MINS_PER_DAY; minute++) {
            unsigned long nanos = dir->config.N_NANO_SECS_PER_MIN;
            struct timespec remaining = {
                .tv_sec = (time_t)(nanos / 1000000000UL),
                .tv_nsec = (long)(nanos % 1000000000UL)
            };
            while (nanosleep(&remaining, &remaining) < 0) {
                if (errno != EINTR) {
                    perror("nanosleep director");
                    goto finish;
                }
            }
            dir->sd->current_tick = (unsigned long)minute + 1;
        }

        for (int i = 0; i < dir->config.NOF_USERS; i++) {
            if (kill(dir->userPids[i], SIGUSR1) < 0) {
                perror("kill user SIGUSR1");
                goto finish;
            }
        }
        for (int i = 0; i < dir->config.NOF_WORKERS; i++) {
            if (kill(dir->operatorPids[i], SIGUSR1) < 0) {
                perror("kill operator SIGUSR1");
                goto finish;
            }
        }
        for (int i = 0; i < dir->config.NOF_USERS; i++)
            if (sem_unlock(dir->semId, SEM_DAY_END_US) < 0) goto finish;
        for (int i = 0; i < dir->config.NOF_WORKERS; i++)
            if (sem_unlock(dir->semId, SEM_DAY_END_OP) < 0) goto finish;

        if (sem_wait_count(dir->semId, SEM_DAY_DONE,
                           dir->config.NOF_USERS + dir->config.NOF_WORKERS, 30) < 0) goto finish;

        printf("[%s] === End day %d ===\n", sim_time_str(dir->sd), day);
        print_daily_stats(dir, day);
        if (stats_csv_append_daily(&dir->sd->stats, csvPath, day) < 0) goto finish;
        if (check_explode_threshold(dir)) {
            termination = "explode";
            break;
        }
        cleanQueues(dir);
    }

    print_cumulative_stats(dir);
    if (stats_csv_append_final(&dir->sd->stats, csvPath, dir->currentDay, termination) < 0)
        goto finish;
    printf("Causa terminazione: %s\n", termination);
    result = 0;

finish:
    if (result < 0) fprintf(stderr, "Simulation terminated due to an error\n");
    director_cleanup(dir);
    return result;
}
