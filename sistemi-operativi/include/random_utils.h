#ifndef SISTEMI_OPERATIVI_LAB2024_2025_RANDOM_UTILS_H
#define SISTEMI_OPERATIVI_LAB2024_2025_RANDOM_UTILS_H

#include <stdint.h>

uint32_t sim_random_u32(void);
uint32_t sim_random_bounded(uint32_t bound);
double sim_random_unit(void);

#endif
