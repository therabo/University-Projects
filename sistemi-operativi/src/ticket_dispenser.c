#include "../include/ticket_dispenser.h"

int ticket_dispenser_init(ticket_dispenser_t *dispenser, int dispenserId) {
    if (!dispenser) return -1;
    dispenser->dispenserId = dispenserId;
    dispenser->reqTicketQueueId = -1;
    dispenser->replyTicketQueueId = -1;
    dispenser->newServiceId = -1;
    dispenser->semId = -1;
    dispenser->shmId = -1;
    dispenser->isRunning = true;
    dispenser->sd = NULL;
    return 0;
}


void ticket_dispenser_run(ticket_dispenser_t *d) {

    static int local_seq = 0;

    d->sd = attach_shared_memory(d->shmId);
    if (!d->sd) return;
    if (sem_unlock(d->semId, SEM_READY) < 0) {
        detach_shared_memory(d->sd);
        return;
    }

    printf("[Dispenser %d] Running...\n", d->dispenserId);

    msgbuf_req req;

    while (d->isRunning) {
        if (msgrcv(d->reqTicketQueueId, &req, sizeof(req) - sizeof(long), MSG_REQ_TICKET, 0) < 0) {
            if (errno == EINTR) continue;
            if (errno == EIDRM) {
                printf("[Dispenser %d] Message queue removed. Exiting.\n", d->dispenserId);
            } else {
                perror("[Dispenser] msgrcv error receiving request");
            }
            d->isRunning = false;
            break;
        }

        uint8_t svc = req.service;
        pid_t pid = req.senderId;
        int64_t ticket = ticket_id_encode(svc, pid, ++local_seq);

        msg_ticket_id tckt = {
                .mtype = req.senderId,
                .ticketId = ticket,
                .n_ticket = local_seq,
                .service = svc,
                .user = req.senderId,
                .operator = 0,
                .durationService = 0
        };

        if (msgsnd(d->replyTicketQueueId, &tckt, sizeof(tckt) - sizeof(long), 0) < 0) {
            if (errno != EIDRM) {
                perror("[Dispenser] msgsnd error sending reply");
            }
        }

        msg_ticket_id svcMsg = {
                .mtype = svc + 1,
                .ticketId  = ticket,
                .service   = svc,
                .n_ticket  = local_seq,
                .user      = req.senderId,
        };
        if (msgsnd(d->newServiceId, &svcMsg, sizeof(svcMsg) - sizeof(long), 0) < 0) {
            if (errno != EIDRM) {
                perror("[Dispenser] msgsnd error sending ticket to operator");
            }
        }
    }
    detach_shared_memory(d->sd);
}


void ticket_dispenser_cleanup(ticket_dispenser_t *dispenser) {
    if (!dispenser) return;
    printf("[Dispenser Cleanup] id=%d\n", dispenser->dispenserId);
    dispenser->isRunning = false;
}
