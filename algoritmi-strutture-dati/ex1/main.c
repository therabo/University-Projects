#include "Records/RecordSort.h"
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>

int main(int argc, char **argv) {
    if (argc != 5 || strlen(argv[3]) != 1 || strlen(argv[4]) != 1 ||
        argv[3][0] < '1' || argv[3][0] > '3' ||
        argv[4][0] < '1' || argv[4][0] > '2') {
        fprintf(stderr, "Usage: %s <records.csv> <sorted.csv> <field:1-3> <algo:1-2>\n", argv[0]);
        return EXIT_FAILURE;
    }

    struct stat input_stat;
    struct stat output_stat;
    if (stat(argv[1], &input_stat) == 0 && stat(argv[2], &output_stat) == 0 &&
        input_stat.st_dev == output_stat.st_dev && input_stat.st_ino == output_stat.st_ino) {
        fputs("Input and output must be different files.\n", stderr);
        return EXIT_FAILURE;
    }

    FILE *input = fopen(argv[1], "r");
    if (input == NULL) {
        fprintf(stderr, "%s: %s\n", argv[1], strerror(errno));
        return EXIT_FAILURE;
    }
    FILE *output = fopen(argv[2], "w");
    if (output == NULL) {
        fprintf(stderr, "%s: %s\n", argv[2], strerror(errno));
        fclose(input);
        return EXIT_FAILURE;
    }

    int result = sort_records_checked(input, output,
                                      (size_t) (argv[3][0] - '0'),
                                      (size_t) (argv[4][0] - '0'));
    if (fclose(input) != 0) result = -1;
    if (fclose(output) != 0) result = -1;
    return result == 0 ? EXIT_SUCCESS : EXIT_FAILURE;
}
