/**
 * @file director.h
 * @brief Header file for the Director module.
 *
 * The Director is responsible for initializing and coordinating all components
 * of the simulation, including creating the required processes (ticket dispenser,
 * operators, and users), managing the daily cycle of the simulation, and collecting
 * as well as printing statistics.
 *
 * The structures below holds configuration and runtime data needed for proper
 * orchestration. The associated functions allow the Director to:
 *   - Parse configuration settings (e.g., from config files).
 *   - Create and initialize resources and child processes.
 *   - Start and manage the simulation day by day.
 *   - Print daily and final statistics.
 *   - Deallocate resources and terminate the simulation.
 *
 * This module is a key coordinator in the entire system, ensuring that all
 * other processes (ticket dispenser, operators, users) run as intended.
 *
 */

#ifndef SISTEMI_OPERATIVI_LAB2024_2025_DIRECTOR_H
#define SISTEMI_OPERATIVI_LAB2024_2025_DIRECTOR_H

#include <sys/types.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <stdint.h>
#include <string.h>
#include <signal.h>
#include <sys/ipc.h>
#include <sys/msg.h>
#include <sys/wait.h>
#include <time.h>

#include "services.h"
#include "configuration.h"
#include "ipc_lights.h"
#include "ipc_shared.h"
#include "operator.h"
#include "user.h"
#include "ticket_dispenser.h"


/**
 * @struct director_t
 * @brief Encapsulates state and data needed by the Director process.
 *
 * The director_t structure contains everything the Director needs at runtime:
 * configuration data, references to IPC object identifiers (shared memory,
 * semaphores, message queue), PIDs of child processes for management,
 * the list of available services, and the current simulation day.
 * It tracks the simulation progress, handles daily statistics collection,
 * and manages the overall lifecycle.
 */
typedef struct {
    Configuration config;
    int currentDay;
    int shmId;
    int semId;
    int rcvServiceId;
    int doneServiceId;
    int reqTicketQueueId;
    int replyTicketQueueId;
    int notifWindowQueueId;
    int newServiceId;
    int addUserQueueId;
    struct SharedData *sd;
    pid_t ticketDispenserPid;
    pid_t *operatorPids;
    pid_t *userPids;
    int operatorsByService[NUM_SERVICES];
} director_t;

/**
 * @brief Initializes the Director by reading configuration, loading services, and setting up IPC resources.
 *
 * This function performs the initial setup for the Director process.
 * It loads the service definitions from their configuration file. Crucially, it
 * creates and initializes the System V IPC resources required for inter-process communication and
 * data sharing. It prepares the director_t structure for runtime.
 *
 * @param dir Pointer to the director_t structure to initialize.
 * @return 0 on success, or a negative error code on failure.
 */
int director_init(director_t *dir, const char *configuration_path);

/**
 * @brief Creates the child processes.
 *
 * After director_init has successfully set up the environment and IPC resources,
 * this function forks the required number of child processes based on the
 * configuration. It stores their PIDs in the director_t structure for later management
 * Each child process is initialized with the necessary IPC identifiers
 * and parameters to perform its role.
 *
 * @param dir Pointer to the initialized director_t structure containing configuration and IPC IDs.
 * @return 0 on success, or a negative error code on failure.
 */
int director_create_processes(director_t *dir);

/**
 * @brief Runs the main simulation loop, managing the progression through days.
 *
 * This function implements the core logic of the simulation's progression. It waits
 * for all child processes to be ready, then iterates
 * through each simulation day:
 *
 * 1. Resets daily state.
 * 2. Check new users.
 * 3. Assigns services to windows for the day.
 * 4. Signals the start of the day to child processes (using SEM_DAY_START).
 * 5. Simulates the passage of time for the day's duration.
 * 6. Signals the end of the day (using SEM_DAY_END).
 * 7. Waits for children to acknowledge the end of the day.
 * 8. Prints daily statistics.
 * 9. Checks for termination conditions.
 * After all days are simulated, it prints cumulative statistics.
 *
 * @param dir Pointer to the initialized director_t structure.
 */
int daily_routine(director_t *dir);

/**
 * @brief Cleans up resources and terminates child processes gracefully.
 *
 * This function is called at the end of the simulation.
 * It sends termination signals to all child processes,
 * waits for them to exit, and then removes the System V IPC resources
 * that it created during initialization. It also frees
 * any dynamically allocated memory.
 *
 * @param dir Pointer to the director_t structure containing PIDs and IPC IDs.
 */
void director_cleanup(director_t *dir);

#endif //SISTEMI_OPERATIVI_LAB2024_2025_DIRECTOR_H
