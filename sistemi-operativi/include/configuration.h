/**
 * @file configuration.h
 * @brief Configuration structure and helpers for the post-office simulation.
 *
 * The file defines the Configuration record, a set of utility
 * functions to read a configuration file, fall back to defaults
 * and print the active parameters.
 */
#ifndef SISTEMI_OPERATIVI_LAB2024_2025_CONFIGURATION_H
#define SISTEMI_OPERATIVI_LAB2024_2025_CONFIGURATION_H

#define MAX_PATH_LEN 256

#include <stdio.h>
#include <string.h>

/**
 * @brief Structure that holds the simulation configuration parameters.
 *
 * This structure stores all the necessary parameters for the simulation including
 * duration, thresholds, number of users, workers, and timing parameters.
 */
typedef struct Configuration {
    int SIM_DURATION;
    int EXPLODE_THRESHOLD;
    int NOF_WORKERS;
    int NOF_WORKER_SEATS;
    int NOF_PAUSE;
    int P_SERV_MIN;
    int P_SERV_MAX;
    int NOF_USERS;
    unsigned long N_NANO_SECS_PER_MIN;
    int MAX_REQUEST;
    int MINS_PER_DAY;
    int P_PAUSE;
    char config_file_path[MAX_PATH_LEN];
}Configuration;

int load_configuration(struct Configuration *config, const char *filename);
int validate_configuration(const Configuration *config);

/**
 * @brief Dump @p config to stdout in human-readable form.
 *
 * @param[in] config configuration to display (must not be @c NULL)
 */
void print_configuration(const struct Configuration *config);

#endif //SISTEMI_OPERATIVI_LAB2024_2025_CONFIGURATION_H
