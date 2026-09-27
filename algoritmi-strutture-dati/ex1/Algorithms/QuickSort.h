#ifndef EX1_QUICKSORT_H
#define EX1_QUICKSORT_H

#include <stddef.h>

void quick_sort(void *base, size_t nitems, size_t size,
                int (*compar)(const void *, const void *));

#endif
