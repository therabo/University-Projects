/**
 * @file add_user_main.c
 * @brief Utility to request additional users from the Director.
 *
 * - Reads the number of new users from argv[1].
 * - Opens the MSG_ADD_USERS queue and sends a msgbuf_add with the count.
 * - Prints confirmation to stdout.
 */

#include "include/ipc_messages.h"
#include <errno.h>
#include <limits.h>

int main(int argc, char **argv) {
    if (argc != 2) {
        fprintf(stderr, "Usage: %s <N_NEW_USERS>\n", argv[0]);
        return EXIT_FAILURE;
    }
    errno = 0;
    char *end;
    long parsed = strtol(argv[1], &end, 10);
    if (errno || *end != '\0' || parsed < 1 || parsed > INT_MAX) {
        fprintf(stderr, "N_NEW_USERS must be a positive integer\n");
        return EXIT_FAILURE;
    }
    int n = (int)parsed;
    key_t key = 0x7777;
    int qid = msgget(key, 0666);
    if (qid < 0) { perror("msgget"); return EXIT_FAILURE; }
    msgbuf_add m = { .mtype = MSG_ADD_USERS, .count = n };
    if (msgsnd(qid, &m, sizeof(m.count), 0) < 0) {
        perror("msgsnd");
        return EXIT_FAILURE;
    }
    printf("Requested %d new users \n", n);
    return EXIT_SUCCESS;
}
