#include "../include/random_utils.h"
#include <stdlib.h>
#include <sys/types.h>
#include <time.h>
#include <unistd.h>

uint32_t sim_random_u32(void) {
    static pid_t seeded_pid = 0;
    pid_t current_pid = getpid();
    if (seeded_pid != current_pid) {
        struct timespec now;
        clock_gettime(CLOCK_REALTIME, &now);
        unsigned int seed = (unsigned int)now.tv_nsec ^ (unsigned int)now.tv_sec ^
                            ((unsigned int)current_pid << 16);
        srandom(seed);
        seeded_pid = current_pid;
    }
    return ((uint32_t)random() << 1) | ((uint32_t)random() & 1u);
}

uint32_t sim_random_bounded(uint32_t bound) {
    if (bound == 0) return 0;
    uint32_t threshold = (uint32_t)(-bound) % bound;
    uint32_t value;
    do {
        value = sim_random_u32();
    } while (value < threshold);
    return value % bound;
}

double sim_random_unit(void) {
    return (double)sim_random_u32() / 4294967296.0;
}
