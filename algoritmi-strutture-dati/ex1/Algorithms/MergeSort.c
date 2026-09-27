#include "MergeSort.h"
#include <errno.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>

static void merge_range(unsigned char *base, unsigned char *buffer,
                        size_t left, size_t right, size_t size,
                        int (*compar)(const void *, const void *)) {
    if (right - left < 2) return;
    size_t middle = left + (right - left) / 2;
    merge_range(base, buffer, left, middle, size, compar);
    merge_range(base, buffer, middle, right, size, compar);

    memcpy(buffer + left * size, base + left * size, (right - left) * size);
    size_t i = left;
    size_t j = middle;
    for (size_t destination = left; destination < right; destination++) {
        size_t source;
        if (i == middle) source = j++;
        else if (j == right) source = i++;
        else if (compar(buffer + i * size, buffer + j * size) <= 0) source = i++;
        else source = j++;
        memcpy(base + destination * size, buffer + source * size, size);
    }
}

void merge_sort(void *base, size_t nitems, size_t size,
                int (*compar)(const void *, const void *)) {
    if (nitems < 2) return;
    if (base == NULL || size == 0 || compar == NULL || nitems > SIZE_MAX / size) {
        errno = EINVAL;
        return;
    }
    unsigned char *buffer = malloc(nitems * size);
    if (buffer == NULL) {
        errno = ENOMEM;
        return;
    }
    merge_range(base, buffer, 0, nitems, size, compar);
    free(buffer);
}
