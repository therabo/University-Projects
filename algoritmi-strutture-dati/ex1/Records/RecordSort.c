#include "RecordSort.h"
#include "../Algorithms/MergeSort.h"
#include "../Algorithms/QuickSort.h"
#include "../Structures/Record.h"
#include <errno.h>
#include <limits.h>
#include <math.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

static int parse_integer(const char *text, int *result) {
    char *end;
    errno = 0;
    long value = strtol(text, &end, 10);
    if (errno != 0 || end == text || *end != '\0' || value < INT_MIN || value > INT_MAX)
        return -1;
    *result = (int) value;
    return 0;
}

static int parse_record(char *line, Record *record) {
    char *fields[4];
    fields[0] = line;
    for (size_t i = 1; i < 4; i++) {
        char *separator = strchr(fields[i - 1], ',');
        if (separator == NULL) return -1;
        *separator = '\0';
        fields[i] = separator + 1;
    }
    if (strchr(fields[3], ',') != NULL || fields[1][0] == '\0' ||
        strlen(fields[1]) >= sizeof(record->word)) return -1;

    fields[3][strcspn(fields[3], "\r\n")] = '\0';
    char *end;
    errno = 0;
    double value = strtod(fields[3], &end);
    if (errno != 0 || end == fields[3] || *end != '\0' || !isfinite(value) ||
        parse_integer(fields[0], &record->index) != 0 ||
        parse_integer(fields[2], &record->val1) != 0) return -1;

    memcpy(record->word, fields[1], strlen(fields[1]) + 1);
    record->val2 = value;
    return 0;
}

static int append_record(Record **records, size_t *count, size_t *capacity, Record record) {
    if (*count == *capacity) {
        size_t grown = *capacity == 0 ? 1024 :
                       *capacity < (1U << 20) ? *capacity * 2 :
                       *capacity + *capacity / 4;
        if (grown <= *capacity || grown > SIZE_MAX / sizeof(**records)) return -1;
        Record *resized = realloc(*records, grown * sizeof(**records));
        if (resized == NULL) return -1;
        *records = resized;
        *capacity = grown;
    }
    (*records)[(*count)++] = record;
    return 0;
}

int sort_records_checked(FILE *infile, FILE *outfile, size_t field, size_t algo) {
    if (infile == NULL || outfile == NULL || infile == outfile ||
        field < 1 || field > 3 || algo < 1 || algo > 2) return -1;

    Record *records = NULL;
    size_t count = 0;
    size_t capacity = 0;
    size_t line_number = 0;
    char line[256];
    while (fgets(line, sizeof(line), infile) != NULL) {
        line_number++;
        if (strlen(line) == sizeof(line) - 1 && strchr(line, '\n') == NULL) {
            fprintf(stderr, "CSV line %zu is too long.\n", line_number);
            free(records);
            return -1;
        }
        Record record;
        if (parse_record(line, &record) != 0 ||
            append_record(&records, &count, &capacity, record) != 0) {
            fprintf(stderr, "Invalid CSV or insufficient memory at line %zu.\n", line_number);
            free(records);
            return -1;
        }
    }
    if (ferror(infile)) {
        free(records);
        return -1;
    }

    int (*compare)(const void *, const void *) =
            field == 1 ? compare_word : field == 2 ? compare_val1 : compare_val2;
    clock_t start = clock();
    errno = 0;
    if (algo == 1) merge_sort(records, count, sizeof(*records), compare);
    else quick_sort(records, count, sizeof(*records), compare);
    if (errno != 0) {
        free(records);
        return -1;
    }
    fprintf(stderr, "%s: %zu records in %.6f seconds.\n",
            algo == 1 ? "MergeSort" : "QuickSort", count,
            (double) (clock() - start) / CLOCKS_PER_SEC);

    for (size_t i = 0; i < count; i++) {
        if (fprintf(outfile, "%d,%s,%d,%.17g\n", records[i].index,
                    records[i].word, records[i].val1, records[i].val2) < 0) {
            free(records);
            return -1;
        }
    }
    free(records);
    return fflush(outfile) == 0 ? 0 : -1;
}

void sort_records(FILE *infile, FILE *outfile, size_t field, size_t algo) {
    (void) sort_records_checked(infile, outfile, field, algo);
}
