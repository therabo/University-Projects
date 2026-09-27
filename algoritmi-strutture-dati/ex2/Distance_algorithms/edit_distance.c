#include "edit_distance.h"
#include <limits.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>

static int min(int a, int b, int c) {
    int result = a;
    if (b < result) result = b;
    if (c < result) result = c;
    return result;
}

int edit_distance(const char *s1, const char *s2) {
    if (*s1 == '\0') return (int) strlen(s2);
    if (*s2 == '\0') return (int) strlen(s1);

    int unchanged = (*s1 == *s2) ? edit_distance(s1 + 1, s2 + 1) : INT_MAX;
    int deletion = 1 + edit_distance(s1, s2 + 1);
    int insertion = 1 + edit_distance(s1 + 1, s2);
    return min(unchanged, deletion, insertion);
}

static int edit_distance_dyn_sub(const char *s1, const char *s2, size_t i, size_t j,
                                 size_t columns, int *memo) {
    size_t index = i * columns + j;
    if (memo[index] != -1) return memo[index];
    if (s1[i] == '\0') return memo[index] = (int) strlen(s2 + j);
    if (s2[j] == '\0') return memo[index] = (int) strlen(s1 + i);

    int unchanged = s1[i] == s2[j]
                    ? edit_distance_dyn_sub(s1, s2, i + 1, j + 1, columns, memo)
                    : INT_MAX;
    int deletion = 1 + edit_distance_dyn_sub(s1, s2, i, j + 1, columns, memo);
    int insertion = 1 + edit_distance_dyn_sub(s1, s2, i + 1, j, columns, memo);
    return memo[index] = min(unchanged, deletion, insertion);
}

int edit_distance_dyn(const char *s1, const char *s2) {
    if (s1 == NULL || s2 == NULL) return -1;
    size_t rows = strlen(s1) + 1;
    size_t columns = strlen(s2) + 1;
    if (rows > INT_MAX || columns > INT_MAX ||
        rows > SIZE_MAX / columns / sizeof(int)) return -1;

    size_t cells = rows * columns;
    int *memo = malloc(cells * sizeof(*memo));
    if (memo == NULL) return -1;
    for (size_t i = 0; i < cells; i++) memo[i] = -1;

    int result = edit_distance_dyn_sub(s1, s2, 0, 0, columns, memo);
    free(memo);
    return result;
}
