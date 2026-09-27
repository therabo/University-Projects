/**
 * @file director_main.c
 * @brief Entry point for the Director process.
 *
 * - Calls director_init() to load configuration and set up IPC.
 * - Calls director_create_processes() to fork the ticket dispenser, operators, and users.
 * - Runs daily_routine() to execute the simulation loop until completion.
 */

#include "include/director.h"

int main(int argc, char *argv[]) {
    if (argc > 2) {
        fprintf(stderr, "Usage: %s [configuration_file]\n", argv[0]);
        return EXIT_FAILURE;
    }

    director_t dir;
    const char *configuration_path = argc == 2 ? argv[1] : "config/configuration_file.conf";
    if (director_init(&dir, configuration_path) < 0) {
        fprintf(stderr, "Error initializing director\n");
        return EXIT_FAILURE;
    }
    if (director_create_processes(&dir) < 0) {
        fprintf(stderr, "Error creating processes\n");
        director_cleanup(&dir);
        return EXIT_FAILURE;
    }
    return daily_routine(&dir) == 0 ? EXIT_SUCCESS : EXIT_FAILURE;
}
