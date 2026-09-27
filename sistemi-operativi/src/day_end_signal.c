#include "../include/day_end_signal.h"

volatile sig_atomic_t day_ended = 0;


static void day_end_handler(int signo) {
    (void)signo;
    day_ended = 1;
}

void setup_day_end_handler(void) {
    struct sigaction sa;
    memset(&sa, 0, sizeof(sa));
    sa.sa_handler = day_end_handler;
    sigemptyset(&sa.sa_mask);
    sa.sa_flags = 0;
    sigaction(SIGUSR1, &sa, NULL);
}

void reset_day_end_flag(void) {
    day_ended = 0;
}
