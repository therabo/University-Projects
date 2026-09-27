/**
 * @file user.h
 * @brief Header file for the User module.
 *
 * This file defines the structure and functions related to a user in the simulation.
 * The user structure user_t holds all necessary information for simulating a user's
 * behavior at the post office, including identification, service request details, and
 * IPC identifiers used for communication with other processes.
 *
 * The module provides functions to initialize, run, and clean up a user.
 *
 */

#ifndef SISTEMI_OPERATIVI_LAB2024_2025_USER_H
#define SISTEMI_OPERATIVI_LAB2024_2025_USER_H

#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdint.h>
#include <string.h>
#include <sys/ipc.h>
#include <sys/msg.h>
#include <time.h>

#include "signal.h"
#include "services.h"
#include "ipc_shared.h"
#include "ipc_lights.h"
#include "stats.h"
#include "ipc_messages.h"
#include "day_end_signal.h"

typedef struct {
    int userId;
    double pServ;
    int numRequests;
    int maxRequests;
    int reqTicketQueueId;
    int replyTicketQueueId;
    int rcvServiceId;
    int doneServiceId;
    int semId;
    int shmId;
    bool isInQueue;
    bool hasBeenServed;
    int simDuration;
    struct SharedData *sd;
} user_t;

/**
 * @brief Initializes a User structure. Allocates request array.
 * @param u Pointer to the user_t struct.
 * @param userId Unique ID.
 * @param pServ Daily visit probability (0.0-1.0).
 * @param configMaxRequests Max daily service attempts.
 * @param simDuration Simulation duration in days.
 * @return 0 on success, -1 on failure.
 */
int user_init(user_t *u, int userId, double pServ, int configMaxRequests, int simDuration);

/**
 * @brief Main loop for the User process.
 *
 * Daily cycle: wait day start, decide visit, attempt services (check active,
 * get ticket, wait for operator messages, update stats), wait day end.
 * @param u Pointer to the initialized user_t struct.
 */
void user_run(user_t *u);

/**
 * @brief Cleans up User resources.
 * @param u Pointer to the user_t struct.
 */
void user_cleanup(user_t *u);

#endif //SISTEMI_OPERATIVI_LAB2024_2025_USER_H
