/**
 * @file stats.h
 * @brief Defines the structure and functions for simulation statistics.
 *
 * Defines the `Stats` structure for collecting daily and cumulative metrics.
 * Provides functions for reset, update, and printing.
 * Relies on macros in ipc_shared.h for per-service array access.
 */

#ifndef SISTEMI_OPERATIVI_LAB2024_2025_STATS_H
#define SISTEMI_OPERATIVI_LAB2024_2025_STATS_H

#include <stdio.h>
#include <stddef.h>

#include "services.h"


typedef struct Stats {
    // Cumulative statistics
    int total_users_served;
    int total_services_served;
    int total_services_not_served;
    double total_wait_time;
    double total_service_time;
    int services_served_by_type[NUM_SERVICES];
    int services_not_served_by_type[NUM_SERVICES];
    double wait_time_by_type[NUM_SERVICES];
    double service_time_by_type[NUM_SERVICES];
    int total_operator_pauses;
    int total_operators_active;

    // Daily statistics
    int daily_users_served;
    int daily_services_served;
    int daily_services_not_served;
    double daily_wait_time;
    double daily_service_time;
    int daily_services_served_by_type[NUM_SERVICES];
    int daily_services_not_served_by_type[NUM_SERVICES];
    double daily_wait_time_by_type[NUM_SERVICES];
    double daily_service_time_by_type[NUM_SERVICES];
    int daily_operator_pauses;
    int daily_operators_active;
    int daily_users_waiting_at_close;
} Stats;

/**
 * @brief Initialize all cumulative and daily counters to zero.
 * @param s Pointer to Stats to initialize.
 */
void stats_init(Stats *s);

/**
 * @brief Resets daily statistics counters to zero. Call at day start.
 * @param s Pointer to the Stats structure within SharedData.
 */
void stats_reset_daily(Stats *s);

/**
 * @brief Updates the statistics after serving a user or after a user leaves.
 *
 * E.g. increment counters, update wait and service times, handle type-based arrays.
 *
 * @param s Pointer to the Stats structure.
 * @param serviceType The type of service requested.
 * @param waitTime How long the user waited.
 * @param serviceTime How long the service took.
 * @param served If 1, user was served; otherwise, user left.
 */
void stats_update_service(Stats *s, int serviceType, double waitTime, double serviceTime, int served);

/**
 * @brief Increments unique user served count. Call once per user per day.
 * @param s Pointer to the Stats structure within SharedData.
 */
void stats_count_user_served(Stats *s);
void stats_count_waiting_user(Stats *s);

/**
 * @brief Prints formatted daily statistics report. Call at day end.
 * @param s Pointer to the Stats structure within SharedData.
 * @param day Current day number.
 */
void stats_print_daily(const Stats *s, int day);

/**
 * @brief Prints formatted cumulative statistics report. Call at simulation end.
 * @param s Pointer to the Stats structure within SharedData.
 * @param totalDays Total simulation days.
 */
void stats_print_cumulative(const Stats *s);

int stats_csv_begin(const char *filename);
int stats_csv_append_daily(const Stats *s, const char *filename, int day);
int stats_csv_append_final(const Stats *s, const char *filename, int day, const char *termination);

#endif //SISTEMI_OPERATIVI_LAB2024_2025_STATS_H
