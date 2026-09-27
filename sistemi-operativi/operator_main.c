/**
 * @file operator_main.c
 * @brief Entry point for an Operator process.
 *
 * - Parses command-line arguments.
 * - Initializes an operator_t via operator_init().
 * - Sets IPC fields and serviceType on the operator struct.
 * - Calls operator_run() to perform daily service routine, then operator_cleanup().
 */

#include "include/operator.h"

int main(int argc, char *argv[]) {

    if (argc != 12) {
        fprintf(stderr, "Usage: %s <operatorId> <maxPauses> <simDuration> <pPause> <shmId> <semId> <rcvServiceId> <doneServiceId> <notifWindowQueueId>\n", argv[0]);
        return EXIT_FAILURE;
    }
    int operatorId           = atoi(argv[1]);
    int serviceType          = atoi(argv[2]);
    int maxPauses            = atoi(argv[3]);
    int simDuration          = atoi(argv[4]);
    int pPause               = atoi(argv[5]);
    int shmId                = atoi(argv[6]);
    int semId                = atoi(argv[7]);
    int rcvServiceId         = atoi(argv[8]);
    int doneServiceId        = atoi(argv[9]);
    int notifWindowQueueId   = atoi(argv[10]);
    int newServiceId         = atoi(argv[11]);

    operator_t op;
    operator_init(&op, operatorId, maxPauses, simDuration, pPause);
    op.serviceType        = serviceType;
    op.shmId              = shmId;
    op.semId              = semId;
    op.rcvServiceId       = rcvServiceId;
    op.doneServiceId      = doneServiceId;
    op.notifWindowQueueId = notifWindowQueueId;
    op.newServiceId       = newServiceId;

    operator_run(&op);
    operator_cleanup(&op);
    return EXIT_SUCCESS;
}
