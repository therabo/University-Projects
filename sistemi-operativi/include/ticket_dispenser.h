/**
 * @file ticket_dispenser.h
 * @brief Contains definitions for  the ticket dispenser process.
 *
 * In addition to the window_t structure this file provides the ticket_dispenser_t structure and its associated functions.
 * The ticket dispenser process receives ticket requests from users, assigns a unique ticket
 * number for each requested service, and communicates those numbers back to the requesting users.
 *
 */

#ifndef SISTEMI_OPERATIVI_LAB2024_2025_TICKET_DISPENSER_H
#define SISTEMI_OPERATIVI_LAB2024_2025_TICKET_DISPENSER_H

#pragma once

#include <stdint.h>
#include <sys/types.h>
#include <stdbool.h>
#include <stdio.h>
#include <errno.h>
#include <sys/msg.h>
#include <unistd.h>
#include <time.h>

#include "ipc_messages.h"
#include "ipc_shared.h"
#include "ipc_lights.h"
#include "services.h"

typedef struct {
    int dispenserId;
    int reqTicketQueueId;
    int replyTicketQueueId;
    int newServiceId;
    int semId;
    int shmId;
    int simDuration;
    bool isRunning;
    struct SharedData *sd;
} ticket_dispenser_t;

/**
 * @brief Initializes the ticket dispenser structure.
 *
 * Sets the ticket counters to zero for each service, configures the provided
 * dispenserId, and records the relevant IPC identifiers.
 *
 * @param dispenser Pointer to the ticket_dispenser_t structure to be initialized.
 * @param dispenserId A unique identifier for this ticket dispenser.
 * @return 0 on success, or a negative error code on failure.
 */
int ticket_dispenser_init(ticket_dispenser_t *dispenser, int dispenserId);

/**
 * @brief Main loop: receive requests, issue ticket IDs, notify operators.
 * @param dispenser Pointer to ticket_dispenser_t.
 */
void ticket_dispenser_run(ticket_dispenser_t *dispenser);

/**
 * @brief Cleans up resources used by the ticket dispenser.
 *
 * This function stops the ticket dispenser’s main loop.
 *
 * @param dispenser Pointer to the ticket_dispenser_t structure.
 */
void ticket_dispenser_cleanup(ticket_dispenser_t *dispenser);


/**
 * @brief Encode a 64-bit ticket ID from service, PID, and sequence.
 * @param service Service type.
 * @param pid User process ID.
 * @param seq Sequential ticket number for this service.
 * @return Encoded ticket identifier.
 */
static inline int64_t ticket_id_encode(uint8_t service, pid_t pid, int seq) {
    return ((int64_t) service << 56) |
           (((int64_t) pid & 0xFFFFFF) << 32) |
           seq;
}

#endif //SISTEMI_OPERATIVI_LAB2024_2025_TICKET_DISPENSER_H
