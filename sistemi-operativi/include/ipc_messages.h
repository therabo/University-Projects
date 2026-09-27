/**
 * @file ipc_messages.h
 * @brief Defines IPC message types and a generic message structure.
 *
 * This header provides the message type macros and the msgbuf_req, msgbuf_w, msg_ticket_id, msgbuf_add structures,
 * used for communication between processes in the post office simulation.
 */
#ifndef SISTEMI_OPERATIVI_LAB2024_2025_IPC_MESSAGES_H
#define SISTEMI_OPERATIVI_LAB2024_2025_IPC_MESSAGES_H

#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <sys/msg.h>

#define MSG_REQ_TICKET    1
#define MSG_ADD_USERS     3

//MSG_REQ_TICKET/MSG_REP_TICKET
typedef struct {
    long mtype;
    int senderId;
    int service;
} msgbuf_req;

typedef struct {
    long mtype;
    pid_t operatorPid;
    int windowId;
} msgbuf_w;

//TICKET
typedef struct {
    long mtype;
    int64_t ticketId;
    pid_t user;
    int service;
    int operator;
    double durationService;
    int n_ticket;
} msg_ticket_id;

//ADD_NEW_USERS
typedef struct {
    long mtype;
    int count;
} msgbuf_add;

#endif //SISTEMI_OPERATIVI_LAB2024_2025_IPC_MESSAGES_H
