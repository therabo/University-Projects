/**
 * @file user_main.c
 * @brief Entry point for a User process.
 *
 * - Parses command-line arguments.
 * - Initializes a user_t via user_init().
 * - Configures IPC fields on the user struct.
 * - Calls user_run() to simulate daily visits, then user_cleanup().
 */

#include "include/user.h"

int main(int argc, char *argv[]) {

    if (argc != 11) {
        fprintf(stderr, "Usage: %s <userId> <pServ> <maxRequests> <simDuration> <shmId> <semId> <reqQueueId> <replyQueueId> <rcvServiceId> <doneServiceId>\n", argv[0]);
        return EXIT_FAILURE;
    }
    int userId             = atoi(argv[1]);
    double pServ           = atof(argv[2]);
    int maxRequests        = atoi(argv[3]);
    int simDuration        = atoi(argv[4]);
    int shmId              = atoi(argv[5]);
    int semId              = atoi(argv[6]);
    int reqTicketQueueId   = atoi(argv[7]);
    int replyTicketQueueId = atoi(argv[8]);
    int rcvServiceId       = atoi(argv[9]);
    int doneServiceId      = atoi(argv[10]);

    user_t u;
    user_init(&u, userId, pServ, maxRequests, simDuration);
    u.shmId             = shmId;
    u.semId             = semId;
    u.reqTicketQueueId  = reqTicketQueueId;
    u.replyTicketQueueId= replyTicketQueueId;
    u.rcvServiceId      = rcvServiceId;
    u.doneServiceId     = doneServiceId;

    user_run(&u);
    user_cleanup(&u);
    return EXIT_SUCCESS;
}
