/**
 * @file ipc_lights.h
 * @brief Provides functions and definitions for managing System V semaphores.
 *
 * This module encapsulates the creation, removal, and basic operations
 * on System V semaphore sets. It defines an enumeration `SemaphoreType` to give
 * meaningful names to the individual semaphores used within the simulation set.
 */

#ifndef SISTEMI_OPERATIVI_LAB2024_2025_IPC_LIGHTS_H
#define SISTEMI_OPERATIVI_LAB2024_2025_IPC_LIGHTS_H

#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/sem.h>
#include <errno.h>
#include <stdio.h>

#include "ipc_shared.h"

/**
 * @brief Enumeration for different semaphores in the project.
 */
typedef enum {
    SEM_STATS = 0,
    SEM_WINDOWS,
    SEM_DAY_START_OP,
    SEM_DAY_START_US,
    SEM_DAY_END_OP,
    SEM_DAY_END_US,
    SEM_READY,
    SEM_DAY_DONE,
    SEM_COUNT
} SemaphoreType;


/**
 * @brief Union required for SysV semaphores.
 */

#ifndef __APPLE__
union semun {
    int val;
    struct semid_ds *buf;
    unsigned short *array;
};
#endif

/**
 * @brief Creates or retrieves a set of semaphores.
 *
 * @param key IPC key (SysV)
 * @param nsems Number of semaphores in the set
 * @param flags Flags for permission and creation
 * @return The semaphore set identifier (sem_id) on success, or -1 on failure.
 */
int create_semaphores(key_t key, int nsems, int flags);
/**
 * @brief Removes the semaphore set.
 *
 * @param sem_id Identifier of the semaphore set.
 * @return 0 on success, -1 on failure.
 */
int remove_semaphores(int sem_id);

/**
 * @brief Decrements the semaphore.
 *
 * @param sem_id Identifier of the semaphore set.
 * @param sem_num Index of the semaphore in the set.
 * @return 0 on success, -1 on failure.
 */
int sem_lock(int sem_id, int sem_num);

/**
 * @brief Increments the semaphore.
 *
 * @param sem_id Identifier of the semaphore set.
 * @param sem_num Index of the semaphore in the set.
 * @return 0 on success, -1 on failure.
 */
int sem_unlock(int sem_id, int sem_num);
int sem_wait_count(int sem_id, int sem_num, int count, int timeout_seconds);

#endif //SISTEMI_OPERATIVI_LAB2024_2025_IPC_LIGHTS_H
