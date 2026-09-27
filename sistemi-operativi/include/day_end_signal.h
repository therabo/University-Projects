#ifndef SISTEMI_OPERATIVI_LAB2024_2025_DAY_END_SIGNAL_H
#define SISTEMI_OPERATIVI_LAB2024_2025_DAY_END_SIGNAL_H

#include <signal.h>
#include <stdbool.h>
#include <string.h>

extern volatile sig_atomic_t day_ended;


void setup_day_end_handler(void);


void reset_day_end_flag(void);

#endif //SISTEMI_OPERATIVI_LAB2024_2025_DAY_END_SIGNAL_H
