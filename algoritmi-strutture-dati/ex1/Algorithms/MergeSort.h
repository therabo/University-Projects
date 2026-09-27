#ifndef EX1_MERGESORT_H
#define EX1_MERGESORT_H

#include <stddef.h>

void merge_sort(void *base, size_t nitems, size_t size,
                int (*compar)(const void *, const void *));

#endif
