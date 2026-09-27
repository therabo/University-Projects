/**
 * @file operator.h
 * @brief Header file for the Operator module.
 *
 * The Operator process is responsible for providing one specific service
 * throughout the simulation. At the start of each day, it competes with
 * other operators to occupy a matching service window. Once
 * an Operator finds a free window, it serves users until the day ends or
 * until it decides to pause.
 */

#ifndef SISTEMI_OPERATIVI_LAB2024_2025_OPERATOR_H
#define SISTEMI_OPERATIVI_LAB2024_2025_OPERATOR_H

#include <stdbool.h>
#include <sys/types.h>
#include <sys/msg.h>
#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include <stdint.h>
#include <unistd.h>

#include "services.h"
#include "ipc_messages.h"
#include "ipc_shared.h"
#include "ipc_lights.h"
#include "day_end_signal.h"


typedef struct {
    int operatorId;
    int serviceType;
    int simDuration;
    int maxPauses;
    int usedPauses;
    int pPause;
    int windowId;
    bool isWorking;
    int semId;
    int shmId;
    int rcvServiceId;
    int doneServiceId;
    int newServiceId;
    int notifWindowQueueId;
    struct SharedData *sd;
} operator_t;

/**
 * @brief Initializes an Operator structure.
 * @param op Pointer to the operator_t struct.
 * @param operatorId Unique ID.
 * @param maxPauses Max total pauses.
 * @param simDuration Simulation duration in days.
 * @param pPause Pause probability (%).
 * @return 0 on success, -1 on error.
 */
int operator_init(operator_t *op, int operatorId, int maxPauses, int simDuration, int pPause);

/**
 * @brief Executes the main logic for the Operator during the simulation.
 *
 * For each day:
 *  1. Attempts to occupy a matching window that offers the same service type.
 *  2. If successful, serves users until the end of the day or until a pause is taken.
 *  3. Releases the window.
 *  4. Repeats this process on subsequent days until the simulation ends.
 *
 * @param op Pointer to the initialized operator_t structure.
 */

void operator_run(operator_t *op);

/**
 * @brief Cleans up Operator resources before exiting. (Currently minimal).
 * @param op Pointer to the operator_t struct.
 */
void operator_cleanup(operator_t *op);

#endif //SISTEMI_OPERATIVI_LAB2024_2025_OPERATOR_H
