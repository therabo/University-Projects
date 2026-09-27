/**
 * @file ipc_shared.h
 * @brief Defines the shared memory structure and management functions.
 *
 * Specifies the `SharedData` layout and provides
 * macros/functions for shared memory segment lifecycle.
 */

#ifndef SISTEMI_OPERATIVI_LAB2024_2025_IPC_SHARED_H
#define SISTEMI_OPERATIVI_LAB2024_2025_IPC_SHARED_H

#include <stddef.h>
#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/shm.h>
#include <stdio.h>
#include <errno.h>
#include <string.h>

#include "window.h"
#include "stats.h"
#include "services.h"

/**
 * @brief Core data structure stored in shared memory.
 *
 * This structure holds the critical resources needed by the
 * various processes in the simulation, such as the dynamically allocated
 * array of post office windows a global ticket counter, and the shared statistics.
 */
struct SharedData {
    int numWindows;
    struct Stats stats;
    unsigned long current_tick;
    unsigned long nanosec_min;
    int minutes_per_day;
    int active_operators_by_service[NUM_SERVICES];
    struct window_t windows[];
};

/**
 * @brief Creates or retrieves a shared memory segment.
 *
 * @param key IPC key used to identify the segment.
 * @param size Size of the segment in bytes.
 * @param flags Flags used for permissions and creation.
 * @return The shared memory ID on success, or -1 on failure.
 */
int create_shared_memory(key_t key, size_t size, int flags);

/**
 * @brief Attaches the shared memory segment to the process address space.
 *
 * @param shmid Shared memory ID.
 * @return Pointer to the SharedData structure, or NULL on failure.
 */
struct SharedData* attach_shared_memory(int shmid);

/**
 * @brief Detaches the shared memory segment from the process address space.
 *
 * @param shmaddr Pointer to the mapped shared memory region.
 * @return 0 on success, -1 on failure.
 */
int detach_shared_memory(const void *shmaddr);

/**
 * @brief Deallocates the shared memory segment.
 *
 * @param shmid Shared memory ID.
 * @return 0 on success, -1 on failure.
 */
int remove_shared_memory(int shmid);

/**
 * @brief Format the current simulation time as HH:MM (since 8:00).
 * @param sd Pointer to shared SharedData.
 * @return Static string "HH:MM" for logging.
 */
const char *sim_time_str(struct SharedData *sd);

#endif // SISTEMI_OPERATIVI_LAB2024_2025_IPC_SHARED_H
