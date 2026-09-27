#include "QuickSort.h"
#include <errno.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>

static void swap_bytes(unsigned char *left, unsigned char *right, size_t size) {
    if (left == right) return;
    for (size_t i = 0; i < size; i++) {
        unsigned char temp = left[i];
        left[i] = right[i];
        right[i] = temp;
    }
}

static size_t median_of_three(unsigned char *base, size_t left, size_t right,
                              size_t size, int (*compar)(const void *, const void *)) {
    size_t middle = left + (right - left) / 2;
    size_t last = right - 1;
    if (compar(base + left * size, base + middle * size) > 0)
        swap_bytes(base + left * size, base + middle * size, size);
    if (compar(base + middle * size, base + last * size) > 0)
        swap_bytes(base + middle * size, base + last * size, size);
    if (compar(base + left * size, base + middle * size) > 0)
        swap_bytes(base + left * size, base + middle * size, size);
    return middle;
}

static void quick_range(unsigned char *base, size_t left, size_t right, size_t size,
                        int (*compar)(const void *, const void *), unsigned char *pivot) {
    while (right - left > 1) {
        size_t middle = median_of_three(base, left, right, size, compar);
        memcpy(pivot, base + middle * size, size);
        size_t lower = left;
        size_t current = left;
        size_t upper = right;
        while (current < upper) {
            int relation = compar(base + current * size, pivot);
            if (relation < 0) {
                swap_bytes(base + lower * size, base + current * size, size);
                lower++;
                current++;
            } else if (relation > 0) {
                upper--;
                swap_bytes(base + current * size, base + upper * size, size);
            } else {
                current++;
            }
        }
        if (lower - left < right - upper) {
            quick_range(base, left, lower, size, compar, pivot);
            left = upper;
        } else {
            quick_range(base, upper, right, size, compar, pivot);
            right = lower;
        }
    }
}

void quick_sort(void *base, size_t nitems, size_t size,
                int (*compar)(const void *, const void *)) {
    if (nitems < 2) return;
    if (base == NULL || size == 0 || compar == NULL || nitems > SIZE_MAX / size) {
        errno = EINVAL;
        return;
    }
    unsigned char *pivot = malloc(size);
    if (pivot == NULL) {
        errno = ENOMEM;
        return;
    }
    quick_range(base, 0, nitems, size, compar, pivot);
    free(pivot);
}
