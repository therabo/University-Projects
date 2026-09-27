#include "../include/ipc_shared.h"

int create_shared_memory(key_t key, size_t size, int flags) {
    int shmid = shmget(key, size, flags);
    if (shmid == -1) {
        if ((flags & IPC_CREAT) && errno == EINVAL) {
            int old_shmid = shmget(key, 0, 0);
            if (old_shmid != -1) {
                printf("Found old shared memory segment (key %x), removing.\n", key);
                if (shmctl(old_shmid, IPC_RMID, NULL) == -1) {
                    perror("Error removing old shared memory (shmctl)");
                } else {
                    shmid = shmget(key, size, flags);
                }
            }
        }
        if (shmid == -1) {
            perror("Error creating shared memory (shmget)");
        }
    }
    return shmid;
}

struct SharedData *attach_shared_memory(int shmid) {
    void *shmaddr = shmat(shmid, NULL, 0);
    if (shmaddr == (void *) -1) {
        perror("Error attaching shared memory (shmat)");
        return NULL;
    }
    return (struct SharedData *) shmaddr;
}

int detach_shared_memory(const void *shmaddr) {
    if (shmdt(shmaddr) == -1) {
        perror("Error detaching shared memory (shmdt)");
        return -1;
    }
    return 0;
}

int remove_shared_memory(int shmid) {
    if (shmctl(shmid, IPC_RMID, NULL) == -1) {
        perror("Error removing shared memory (shmctl)");
        return -1;
    }
    return 0;
}


const char *sim_time_str(struct SharedData *sd) {
    static char buf[8];
    int tick = 0;

    tick = sd->current_tick;

    int total_minutes_from_midnight = (8 * 60) + tick;
    int hh = (total_minutes_from_midnight / 60) % 24;
    int mm = total_minutes_from_midnight % 60;

    snprintf(buf, sizeof(buf), "%02d:%02d", hh, mm);
    return buf;
}
