#include "Record.h"
#include <strings.h>

static int compare_int(int left, int right) {
    return (left > right) - (left < right);
}

int compare_idx(const void *left, const void *right) {
    return compare_int(((const Record *) left)->index, ((const Record *) right)->index);
}

int compare_word(const void *left, const void *right) {
    return strcasecmp(((const Record *) left)->word, ((const Record *) right)->word);
}

int compare_val1(const void *left, const void *right) {
    return compare_int(((const Record *) left)->val1, ((const Record *) right)->val1);
}

int compare_val2(const void *left, const void *right) {
    double first = ((const Record *) left)->val2;
    double second = ((const Record *) right)->val2;
    return (first > second) - (first < second);
}
