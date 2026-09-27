#ifndef EX1_RECORD_SORT_H
#define EX1_RECORD_SORT_H

#include <stddef.h>
#include <stdio.h>

void sort_records(FILE *infile, FILE *outfile, size_t field, size_t algo);
int sort_records_checked(FILE *infile, FILE *outfile, size_t field, size_t algo);

#endif
