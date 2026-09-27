#include "../include/stats.h"
#include "../include/ipc_shared.h"


#define SD_FROM_STATS(s) \
   ((struct SharedData*)(((char*)(s)) - offsetof(struct SharedData, stats)))

void stats_init(Stats *s){
    for (size_t i = 0; i < NUM_SERVICES; i++) {
        s->services_served_by_type[i] = 0;
        s->services_not_served_by_type[i] = 0;
        s->wait_time_by_type[i] = 0.0;
        s->service_time_by_type[i] = 0.0;
    }
    s->total_users_served = 0;
    s->total_services_served = 0;
    s->total_services_not_served = 0;
    s->total_wait_time = 0.0;
    s->total_service_time = 0.0;
    s->total_operator_pauses = 0;
    s->total_operators_active = 0;

    stats_reset_daily(s);
}

void stats_reset_daily(Stats *s) {
    s->daily_users_served = 0;
    s->daily_services_served = 0;
    s->daily_services_not_served = 0;
    s->daily_wait_time = 0.0;
    s->daily_service_time = 0.0;
    s->daily_operator_pauses = 0;
    s->daily_operators_active = 0;
    s->daily_users_waiting_at_close = 0;

    for (size_t i = 0; i < NUM_SERVICES; ++i) {
        s->daily_services_served_by_type[i] = 0;
        s->daily_services_not_served_by_type[i] = 0;
        s->daily_wait_time_by_type[i] = 0.0;
        s->daily_service_time_by_type[i] = 0.0;
    }
}

void stats_update_service(Stats *s,
                          int serviceType,
                          double waitTime,
                          double serviceTime,
                          int served) {

    if (serviceType < 0 || serviceType >= NUM_SERVICES) {
        fprintf(stderr, "stats_update_service: Invalid serviceType %d\n", serviceType);
        return;
    }

    s->total_wait_time += waitTime;
    s->total_service_time += serviceTime;

    if (served) {
        s->total_services_served++;
        s->services_served_by_type[serviceType]++;
        s->service_time_by_type[serviceType] += serviceTime;
    } else {
        s->total_services_not_served++;
        s->services_not_served_by_type[serviceType]++;

    }
    s->wait_time_by_type[serviceType] += waitTime;


    s->daily_wait_time += waitTime;

    if (served) {
        s->daily_services_served++;
        s->daily_services_served_by_type[serviceType]++;
        s->daily_service_time += serviceTime;
        s->daily_service_time_by_type[serviceType] += serviceTime;
    } else {
        s->daily_services_not_served++;
        s->daily_services_not_served_by_type[serviceType]++;
    }
    s->daily_wait_time_by_type[serviceType] += waitTime;
}

void stats_count_user_served(Stats *s) {
    s->total_users_served++;
    s->daily_users_served++;
}

void stats_count_waiting_user(Stats *s) {
    s->daily_users_waiting_at_close++;
}

static double window_operator_ratio(const struct SharedData *sd, int windowIndex) {
    int service = sd->windows[windowIndex].currentServiceType;
    int seats = 0;
    for (int i = 0; i < sd->numWindows; i++) {
        if (sd->windows[i].currentServiceType == service) seats++;
    }
    return seats > 0 ? (double)sd->active_operators_by_service[service] / seats : 0.0;
}

void stats_print_daily(const Stats *s, int day) {
    struct SharedData *sd = SD_FROM_STATS(s);
    size_t numWindows = sd->numWindows;

    printf("\n--- Statistiche Giornaliere %d ---\n", day);
    printf("Utenti serviti: %d\n", s->daily_users_served);
    printf("Servizi erogati: %d\n", s->daily_services_served);
    printf("Servizi non erogati: %d\n", s->daily_services_not_served);
    printf("Utenti in attesa alla chiusura: %d\n", s->daily_users_waiting_at_close);
    if (s->daily_services_served > 0) {
        printf("Tempo medio d’attesa: %.2f min\n",
               s->daily_wait_time / s->daily_services_served);
        printf("Tempo medio di erogazione: %.2f min\n",
               s->daily_service_time / s->daily_services_served);
    } else {
        printf("Tempo medio d’attesa: N/A (0 servizi erogati)\n");
        printf("Tempo medio di erogazione: N/A (0 servizi erogati)\n");
    }
    printf("Operatori attivi: %d\n", s->daily_operators_active);
    printf("Pause operatori: %d\n", s->daily_operator_pauses);

    if (numWindows > 0) {
        double ratio = (double) s->daily_operators_active / (double) numWindows;
        printf("Rapporto operatori/sportelli: %.2f\n", ratio);
        for (size_t w = 0; w < numWindows; w++) {
            printf("  Sportello %zu (servizio %d): operatori disponibili/sportelli %.2f\n",
                   w, sd->windows[w].currentServiceType, window_operator_ratio(sd, (int)w));
        }
    } else {
        printf("Rapporto operatori/sportelli: N/A (0 sportelli)\n");
    }


    printf("\nStatistiche per tipo di servizio (giornaliere):\n");
    for (size_t i = 0; i < NUM_SERVICES; ++i) {
        int served = s->daily_services_served_by_type[i];
        int notServ = s->daily_services_not_served_by_type[i];
        double avgW = (served > 0) ? s->daily_wait_time_by_type[i] / (double)served : 0.0;
        double avgS = (served > 0) ? s->daily_service_time_by_type[i] /(double) served : 0.0;
        printf("  Service %zu: erogati=%d, non erogati=%d, attesa media=%.2f min, tempo erogazione servizio=%.2f min\n",
               i, served, notServ, avgW, avgS);
    }


    if (day > 0) {
        double avgUsers = (double) s->total_users_served / day;
        double avgServ = (double) s->total_services_served / day;
        double avgNonServ = (double) s->total_services_not_served / day;
        double avgPauses = (double) s->total_operator_pauses / day;
        printf("\n--- Medie fino al giorno %d ---\n", day);
        printf("Utenti medi al giorno: %.2f\n", avgUsers);
        printf("Servizi erogati medi al giorno: %.2f\n", avgServ);
        printf("Servizi non erogati medi al giorno: %.2f\n", avgNonServ);
        printf("Pause medie al giorno: %.2f\n\n", avgPauses);
    }
}

void stats_print_cumulative(const Stats *s) {

    printf("\n--- Statistiche complessive ---\n");
    printf("Utenti totali serviti: %d\n", s->total_users_served);
    printf("Servizi totali erogati: %d\n", s->total_services_served);
    printf("Servizi totali non erogati: %d\n", s->total_services_not_served);
    if (s->total_services_served > 0) {
        printf("Attesa media: %.2f min\n",
               s->total_wait_time / s->total_services_served);
        printf("Erogazione media: %.2f min\n",
               s->total_service_time / s->total_services_served);
    } else {
        printf("Attesa media: N/A (0 servizi erogati)\n");
        printf("Erogazione media: N/A (0 servizi erogati)\n");
    }
    printf("Operatori attivi totali: %d\n", s->total_operators_active);
    printf("Pause totali effettuate: %d\n", s->total_operator_pauses);

    printf("\nStatistiche per tipo di servizio (complessive):\n");
    for (size_t i = 0; i < NUM_SERVICES; ++i) {
        int served = s->services_served_by_type[i];
        int notServ = s->services_not_served_by_type[i];
        double avgW = (served > 0) ? s->wait_time_by_type[i] / served : 0.0;
        double avgS = (served > 0) ? s->service_time_by_type[i] / served : 0.0;
        printf("  Service %zu: erogati=%d, non erogati=%d, attesa media=%.2f min, tempo erogazione servizio=%.2f min\n",
               i, served, notServ, avgW, avgS);
    }
}


static void csv_metric(FILE *f, const char *scope, int day, int service, int window,
                       const char *metric, double value) {
    fprintf(f, "%s,%d,", scope, day);
    if (service >= 0) fprintf(f, "%d,", service);
    else fputc(',', f);
    if (window >= 0) fprintf(f, "%d,", window);
    else fputc(',', f);
    fprintf(f, "%s,%.6f\n", metric, value);
}

static int close_csv(FILE *f) {
    int failed = ferror(f);
    if (fclose(f) != 0) failed = 1;
    if (failed) perror("CSV write");
    return failed ? -1 : 0;
}

int stats_csv_begin(const char *filename) {
    FILE *f = fopen(filename, "w");
    if (!f) {
        perror("CSV open");
        return -1;
    }
    fprintf(f, "Scope,Day,Service,Window,Metric,Value\n");
    return close_csv(f);
}

int stats_csv_append_daily(const Stats *s, const char *filename, int day) {
    const struct SharedData *sd = SD_FROM_STATS(s);
    FILE *f = fopen(filename, "a");
    if (!f) {
        perror("CSV open");
        return -1;
    }

    csv_metric(f, "daily", day, -1, -1, "UsersServed", s->daily_users_served);
    csv_metric(f, "daily", day, -1, -1, "ServicesServed", s->daily_services_served);
    csv_metric(f, "daily", day, -1, -1, "ServicesNotServed", s->daily_services_not_served);
    csv_metric(f, "daily", day, -1, -1, "UsersWaitingAtClose", s->daily_users_waiting_at_close);
    csv_metric(f, "daily", day, -1, -1, "WaitTimeTotal", s->daily_wait_time);
    csv_metric(f, "daily", day, -1, -1, "ServiceTimeTotal", s->daily_service_time);
    csv_metric(f, "daily", day, -1, -1, "AverageWaitTime",
               s->daily_services_served ? s->daily_wait_time / s->daily_services_served : 0.0);
    csv_metric(f, "daily", day, -1, -1, "AverageServiceTime",
               s->daily_services_served ? s->daily_service_time / s->daily_services_served : 0.0);
    csv_metric(f, "daily", day, -1, -1, "OperatorsActive", s->daily_operators_active);
    csv_metric(f, "daily", day, -1, -1, "OperatorPauses", s->daily_operator_pauses);
    csv_metric(f, "daily", day, -1, -1, "OperatorWindowRatio",
               sd->numWindows ? (double)s->daily_operators_active / sd->numWindows : 0.0);
    csv_metric(f, "daily", day, -1, -1, "AverageUsersPerDay", (double)s->total_users_served / day);
    csv_metric(f, "daily", day, -1, -1, "AverageServicesServedPerDay", (double)s->total_services_served / day);
    csv_metric(f, "daily", day, -1, -1, "AverageServicesNotServedPerDay",
               (double)s->total_services_not_served / day);
    csv_metric(f, "daily", day, -1, -1, "AverageOperatorPausesPerDay",
               (double)s->total_operator_pauses / day);

    for (int service = 0; service < NUM_SERVICES; service++) {
        int served = s->daily_services_served_by_type[service];
        csv_metric(f, "daily", day, service, -1, "ServicesServed", served);
        csv_metric(f, "daily", day, service, -1, "ServicesNotServed",
                   s->daily_services_not_served_by_type[service]);
        csv_metric(f, "daily", day, service, -1, "AverageWaitTime",
                   served ? s->daily_wait_time_by_type[service] / served : 0.0);
        csv_metric(f, "daily", day, service, -1, "AverageServiceTime",
                   served ? s->daily_service_time_by_type[service] / served : 0.0);
    }
    for (int window = 0; window < sd->numWindows; window++) {
        int service = sd->windows[window].currentServiceType;
        csv_metric(f, "daily", day, service, window, "AvailableOperatorWindowRatio",
                   window_operator_ratio(sd, window));
    }
    return close_csv(f);
}

int stats_csv_append_final(const Stats *s, const char *filename, int day,
                           const char *termination) {
    FILE *f = fopen(filename, "a");
    if (!f) {
        perror("CSV open");
        return -1;
    }

    csv_metric(f, "total", day, -1, -1, "DaysSimulated", day);
    csv_metric(f, "total", day, -1, -1, "UsersServed", s->total_users_served);
    csv_metric(f, "total", day, -1, -1, "ServicesServed", s->total_services_served);
    csv_metric(f, "total", day, -1, -1, "ServicesNotServed", s->total_services_not_served);
    csv_metric(f, "total", day, -1, -1, "WaitTimeTotal", s->total_wait_time);
    csv_metric(f, "total", day, -1, -1, "ServiceTimeTotal", s->total_service_time);
    csv_metric(f, "total", day, -1, -1, "AverageWaitTime",
               s->total_services_served ? s->total_wait_time / s->total_services_served : 0.0);
    csv_metric(f, "total", day, -1, -1, "AverageServiceTime",
               s->total_services_served ? s->total_service_time / s->total_services_served : 0.0);
    csv_metric(f, "total", day, -1, -1, "OperatorsActive", s->total_operators_active);
    csv_metric(f, "total", day, -1, -1, "OperatorPauses", s->total_operator_pauses);
    csv_metric(f, "total", day, -1, -1, "AverageUsersPerDay", (double)s->total_users_served / day);
    csv_metric(f, "total", day, -1, -1, "AverageServicesServedPerDay",
               (double)s->total_services_served / day);
    csv_metric(f, "total", day, -1, -1, "AverageServicesNotServedPerDay",
               (double)s->total_services_not_served / day);
    csv_metric(f, "total", day, -1, -1, "AverageOperatorPausesPerDay",
               (double)s->total_operator_pauses / day);

    for (int service = 0; service < NUM_SERVICES; service++) {
        int served = s->services_served_by_type[service];
        csv_metric(f, "total", day, service, -1, "ServicesServed", served);
        csv_metric(f, "total", day, service, -1, "ServicesNotServed",
                   s->services_not_served_by_type[service]);
        csv_metric(f, "total", day, service, -1, "AverageWaitTime",
                   served ? s->wait_time_by_type[service] / served : 0.0);
        csv_metric(f, "total", day, service, -1, "AverageServiceTime",
                   served ? s->service_time_by_type[service] / served : 0.0);
    }
    fprintf(f, "total,%d,,,Termination,%s\n", day, termination);
    return close_csv(f);
}
