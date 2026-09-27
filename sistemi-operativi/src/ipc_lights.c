#include "../include/ipc_lights.h"
#include <limits.h>
#include <time.h>

int create_semaphores(key_t key, int nsems, int flags) {
    int semid = semget(key, nsems, flags);
    if (semid == -1) {
        perror("Error semget");
        return -1;
    }

    if (flags & IPC_CREAT) {
        union semun arg;
        for (int i = 0; i < nsems; i++) {
            if (i == SEM_DAY_END_OP || i == SEM_DAY_END_US || i == SEM_DAY_START_US ||
                i == SEM_DAY_START_OP || i == SEM_READY || i == SEM_DAY_DONE)
                arg.val = 0;
            else
                arg.val = 1;

            if (semctl(semid, i, SETVAL, arg) == -1) {
                perror("Error semctl SETVAL");
                return -1;
            }
        }
    }
    return semid;
}

int remove_semaphores(int sem_id) {
    if (semctl(sem_id, 0, IPC_RMID) == -1) {
        perror("Error remove_semaphores");
        return -1;
    }
    return 0;
}

int sem_lock(int sem_id, int sem_num) {
    struct sembuf op;
    op.sem_num = sem_num;
    op.sem_op = -1;
    op.sem_flg = 0;


    while (semop(sem_id, &op, 1) == -1) {
        if (errno == EINTR) {
            continue;
        } else {
            perror("Error sem_lock semop");
            return -1;
        }
    }
    return 0;
}

int sem_unlock(int sem_id, int sem_num) {
    struct sembuf op;
    op.sem_num = sem_num;
    op.sem_op = 1;
    op.sem_flg = 0;

    while (semop(sem_id, &op, 1) == -1) {
        if (errno == EINTR) {
            continue;
        } else {
            perror("Error sem_unlock semop");
            return -1;
        }
    }
    return 0;
}

int sem_wait_count(int sem_id, int sem_num, int count, int timeout_seconds) {
    if (count < 0 || count > SHRT_MAX || timeout_seconds <= 0) {
        errno = EINVAL;
        return -1;
    }
    if (count == 0) return 0;

    struct sembuf op = {
        .sem_num = (unsigned short)sem_num,
        .sem_op = (short)-count,
        .sem_flg = 0
    };

#ifndef __linux__
    struct timespec start;
    if (clock_gettime(CLOCK_MONOTONIC, &start) < 0) return -1;
    op.sem_flg = IPC_NOWAIT;
#endif

    for (;;) {
#ifdef __linux__
        struct timespec timeout = {.tv_sec = timeout_seconds, .tv_nsec = 0};
        int result = semtimedop(sem_id, &op, 1, &timeout);
#else
        int result = semop(sem_id, &op, 1);
#endif
        if (result == 0) return 0;
#ifndef __linux__
        if (errno == EAGAIN) {
            struct timespec now;
            if (clock_gettime(CLOCK_MONOTONIC, &now) < 0) return -1;
            time_t elapsed_seconds = now.tv_sec - start.tv_sec;
            if (elapsed_seconds > timeout_seconds ||
                (elapsed_seconds == timeout_seconds && now.tv_nsec >= start.tv_nsec)) {
                errno = ETIMEDOUT;
                perror("sem_wait_count");
                return -1;
            }
            struct timespec pause = {.tv_sec = 0, .tv_nsec = 10000000};
            nanosleep(&pause, NULL);
            continue;
        }
#endif
        if (errno != EINTR) {
            perror("sem_wait_count");
            return -1;
        }
    }
}
