/**
 * @file ticket_dispenser_main.c
 * @brief Entry point for the Ticket Dispenser process.
 *
 * - Parses command-line arguments.
 * - Initializes a ticket_dispenser_t via ticket_dispenser_init().
 * - Configures IPC fields on the dispenser struct.
 * - Calls ticket_dispenser_run() to handle requests, then ticket_dispenser_cleanup().
 */

#include "include/ticket_dispenser.h"

int main(int argc, char *argv[]) {

    if (argc != 7) {
        fprintf(stderr, "Usage: %s <dispenserId> <shmId> <semId> <reqQueueId> <replyQueueId> <newServiceId>\n", argv[0]);
        return EXIT_FAILURE;
    }
    int dispenserId       = atoi(argv[1]);
    int shmId             = atoi(argv[2]);
    int semId             = atoi(argv[3]);
    int reqTicketQueueId  = atoi(argv[4]);
    int replyTicketQueueId= atoi(argv[5]);
    int newServiceId      = atoi(argv[6]);

    ticket_dispenser_t dispenser;
    if (ticket_dispenser_init(&dispenser, dispenserId) < 0) {
        fprintf(stderr, "Error initializing ticket dispenser\n");
        return EXIT_FAILURE;
    }
    dispenser.shmId            = shmId;
    dispenser.semId            = semId;
    dispenser.reqTicketQueueId = reqTicketQueueId;
    dispenser.replyTicketQueueId = replyTicketQueueId;
    dispenser.newServiceId     = newServiceId;

    ticket_dispenser_run(&dispenser);
    ticket_dispenser_cleanup(&dispenser);
    return EXIT_SUCCESS;
}
