#include "../include/configuration.h"
#include <ctype.h>
#include <errno.h>
#include <limits.h>
#include <stdint.h>
#include <stdlib.h>

int load_configuration(struct Configuration *config, const char *filename) {
    if (!config || !filename) return -1;

    FILE *fp = fopen(filename, "r");
    if (!fp) {
        perror("Error opening configuration file");
        return -1;
    }

    static const char *keys[] = {
        "SIM_DURATION", "EXPLODE_THRESHOLD", "NOF_WORKERS", "NOF_WORKER_SEATS",
        "NOF_PAUSE", "P_SERV_MIN", "P_SERV_MAX", "N_NANO_SECS_PER_MIN",
        "NOF_USERS", "MAX_REQUESTS", "MINS_PER_DAY", "P_PAUSE"
    };
    unsigned int seen = 0;
    char buffer[128];
    int line = 0;

    while (fgets(buffer, sizeof(buffer), fp)) {
        line++;
        char *start = buffer;
        while (isspace((unsigned char)*start)) start++;
        if (*start == '\0' || *start == '#') continue;

        char *separator = strchr(start, '=');
        if (!separator || separator == start) goto invalid;
        *separator = '\0';

        char *end_key = separator;
        while (end_key > start && isspace((unsigned char)end_key[-1])) *--end_key = '\0';
        if (end_key == start) goto invalid;

        char *value = separator + 1;
        while (isspace((unsigned char)*value)) value++;
        errno = 0;
        char *end_value;
        unsigned long parsed = strtoul(value, &end_value, 10);
        if (value == end_value) goto invalid;
        while (isspace((unsigned char)*end_value)) end_value++;
        if (errno || *end_value != '\0' || value[0] == '-') goto invalid;

        int index = -1;
        for (int i = 0; i < 12; i++) {
            if (strcmp(start, keys[i]) == 0) {
                index = i;
                break;
            }
        }
        if (index < 0 || (seen & (1u << index))) goto invalid;
        if (index != 7 && parsed > INT_MAX) goto invalid;
        seen |= 1u << index;

        switch (index) {
            case 0: config->SIM_DURATION = (int)parsed; break;
            case 1: config->EXPLODE_THRESHOLD = (int)parsed; break;
            case 2: config->NOF_WORKERS = (int)parsed; break;
            case 3: config->NOF_WORKER_SEATS = (int)parsed; break;
            case 4: config->NOF_PAUSE = (int)parsed; break;
            case 5: config->P_SERV_MIN = (int)parsed; break;
            case 6: config->P_SERV_MAX = (int)parsed; break;
            case 7: config->N_NANO_SECS_PER_MIN = parsed; break;
            case 8: config->NOF_USERS = (int)parsed; break;
            case 9: config->MAX_REQUEST = (int)parsed; break;
            case 10: config->MINS_PER_DAY = (int)parsed; break;
            case 11: config->P_PAUSE = (int)parsed; break;
            default: goto invalid;
        }
    }

    if (ferror(fp) || seen != ((1u << 12) - 1u)) {
        fprintf(stderr, "Incomplete configuration: %s\n", filename);
        fclose(fp);
        return -1;
    }
    fclose(fp);
    snprintf(config->config_file_path, MAX_PATH_LEN, "%s", filename);
    return validate_configuration(config);

invalid:
    fprintf(stderr, "Invalid configuration at %s:%d\n", filename, line);
    fclose(fp);
    return -1;
}

int validate_configuration(const Configuration *config) {
    if (config->SIM_DURATION < 1 || config->EXPLODE_THRESHOLD < 0 ||
        config->NOF_USERS < 1 || config->NOF_WORKERS < 1 ||
        config->NOF_WORKER_SEATS < 1 || config->NOF_PAUSE < 0 ||
        config->P_SERV_MIN < 0 || config->P_SERV_MAX > 100 ||
        config->P_SERV_MIN > config->P_SERV_MAX ||
        config->N_NANO_SECS_PER_MIN == 0 || config->MAX_REQUEST < 1 ||
        config->MINS_PER_DAY < 1 || config->P_PAUSE < 0 || config->P_PAUSE > 100 ||
        (long)config->NOF_USERS + config->NOF_WORKERS + 1 > SHRT_MAX ||
        (uint64_t)config->MINS_PER_DAY > UINT64_MAX / config->N_NANO_SECS_PER_MIN) {
        fprintf(stderr, "Configuration values are out of range\n");
        return -1;
    }
    return 0;
}


void print_configuration(const Configuration *config) {
    if (!config) {
        fprintf(stderr, "print_configuration: config pointer is NULL\n");
        return;
    }

    printf("Configuration:\n");
    printf("  SIM_DURATION: %d\n", config->SIM_DURATION);
    printf("  EXPLODE_THRESHOLD: %d\n", config->EXPLODE_THRESHOLD);
    printf("  NOF_WORKERS: %d\n", config->NOF_WORKERS);
    printf("  NOF_WORKER_SEATS: %d\n", config->NOF_WORKER_SEATS);
    printf("  NOF_PAUSE: %d\n", config->NOF_PAUSE);
    printf("  P_SERV_MIN: %d\n", config->P_SERV_MIN);
    printf("  P_SERV_MAX: %d\n", config->P_SERV_MAX);
    printf("  N_NANO_SECS_PER_MIN: %lu\n", config->N_NANO_SECS_PER_MIN);
    printf("  NOF_USERS: %d\n", config->NOF_USERS);
    printf("  MAX_REQUESTS: %d\n", config->MAX_REQUEST);
    printf("  MINS_PER_DAY: %d\n", config->MINS_PER_DAY);
    printf("  P_PAUSE: %d\n", config->P_PAUSE);
    printf("  Config file path: %s\n", config->config_file_path);
}
