/**
 * @file window.h
 * @brief Defines the structure representing a service window.
 *
 * Models a physical service point with ID, assigned service,
 * occupancy status, and operator PID. Stored in shared memory.
 */


#ifndef SISTEMI_OPERATIVI_LAB2024_2025_WINDOW_H
#define SISTEMI_OPERATIVI_LAB2024_2025_WINDOW_H

#include <stdbool.h>
#include <sys/types.h>
#include <stdlib.h>
#include <stdio.h>

#include "services.h"

typedef struct window_t {
    int windowId;
    int currentServiceType;
    bool isOccupied;
    pid_t currentOperatorPid;
} window_t;

/**
 * @brief Initializes a window with the given identifier.
 *
 * Typically called by the Director at the start of the simulation or at the beginning of a day.
 * The Director may then assign a service type to this window.
 *
 * @param w Pointer to the window_t structure to initialize.
 * @param windowId Unique identifier for the window (e.g. 0, 1, 2...).
 */
void window_init(window_t *w, int windowId);

/**
 * @brief Sets the service type for the given window, indicating which service it offers today.
 *
 * @param w Pointer to the window_t structure.
 * @param service The service_t enum value for the service assigned to this window.
 */
void window_set_service(window_t *w, int service);

/**
 * @brief Attempts to occupy the window with the given operator's PID.
 *
 * Marks the window as occupied and stores the operator PID. Returns true if successful,
 * or false if the window is already occupied.
 *
 * @param w Pointer to the window_t structure.
 * @param operatorPid The PID of the operator attempting to occupy this window.
 * @return true if the window was free and is now occupied, false otherwise.
 */
bool window_occupy(window_t *w, pid_t operatorPid);

/**
 * @brief Frees the window, making it available for another operator.
 *
 * Resets isOccupied to false and currentOperatorPid to -1 (or 0).
 *
 * @param w Pointer to the window_t structure.
 */
void window_free(window_t *w);
#endif //SISTEMI_OPERATIVI_LAB2024_2025_WINDOW_H
