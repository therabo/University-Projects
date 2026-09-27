#ifndef EX1_RECORD_H
#define EX1_RECORD_H

typedef struct {
    int index;
    char word[16];
    int val1;
    double val2;
} Record;

int compare_idx(const void *left, const void *right);
int compare_word(const void *left, const void *right);
int compare_val1(const void *left, const void *right);
int compare_val2(const void *left, const void *right);

#endif
