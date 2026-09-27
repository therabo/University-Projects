#include "unity.h"
#include "../Algorithms/MergeSort.h"
#include "../Algorithms/QuickSort.h"
#include "../Records/RecordSort.h"
#include "../Structures/Record.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static int compare_numbers(const void *left, const void *right) {
    int a = *(const int *) left;
    int b = *(const int *) right;
    return (a > b) - (a < b);
}

void test_merge_sort_generic_integers(void) {
    int values[] = {5, -1, 5, 0, 3};
    int expected[] = {-1, 0, 3, 5, 5};
    merge_sort(values, 5, sizeof(values[0]), compare_numbers);
    TEST_ASSERT_EQUAL_INT_ARRAY(expected, values, 5);
}

void test_quick_sort_generic_integers(void) {
    int values[] = {5, -1, 5, 0, 3};
    int expected[] = {-1, 0, 3, 5, 5};
    quick_sort(values, 5, sizeof(values[0]), compare_numbers);
    TEST_ASSERT_EQUAL_INT_ARRAY(expected, values, 5);
}

void test_sort_empty_arrays(void) {
    merge_sort(NULL, 0, sizeof(int), compare_numbers);
    quick_sort(NULL, 0, sizeof(int), compare_numbers);
    TEST_PASS();
}

void test_quick_sort_already_sorted_large_array(void) {
    enum { COUNT = 10000 };
    int *values = malloc(COUNT * sizeof(*values));
    TEST_ASSERT_NOT_NULL(values);
    for (int i = 0; i < COUNT; i++) values[i] = i;
    quick_sort(values, COUNT, sizeof(*values), compare_numbers);
    for (int i = 0; i < COUNT; i++) TEST_ASSERT_EQUAL_INT(i, values[i]);
    free(values);
}

void test_quick_sort_duplicate_records(void) {
    Record records[4] = {
            {1, "same", 2, 1.0}, {2, "same", 2, 1.0},
            {3, "same", 2, 1.0}, {4, "same", 2, 1.0}
    };
    quick_sort(records, 4, sizeof(records[0]), compare_word);
    for (size_t i = 0; i < 4; i++) TEST_ASSERT_EQUAL_STRING("same", records[i].word);
}

static FILE *csv_input(const char *content) {
    FILE *file = tmpfile();
    if (file == NULL) return NULL;
    fputs(content, file);
    rewind(file);
    return file;
}

void test_sort_records_all_fields_and_algorithms(void) {
    const char *source = "1,zeta,3,4.1\n2,alpha,1,2.2\n3,beta,2,1.3\n";
    const int expected_first_id[3] = {2, 2, 3};
    for (size_t field = 1; field <= 3; field++) {
        for (size_t algorithm = 1; algorithm <= 2; algorithm++) {
            FILE *input = csv_input(source);
            FILE *output = tmpfile();
            TEST_ASSERT_NOT_NULL(input);
            TEST_ASSERT_NOT_NULL(output);
            TEST_ASSERT_EQUAL_INT(0, sort_records_checked(input, output, field, algorithm));
            rewind(output);
            char line[100];
            TEST_ASSERT_NOT_NULL(fgets(line, sizeof(line), output));
            TEST_ASSERT_EQUAL_INT(expected_first_id[field - 1], atoi(line));
            fclose(output);
            fclose(input);
        }
    }
}

void test_sort_records_preserves_double_precision(void) {
    FILE *input = csv_input("7,noto,233460,32209.073312\n");
    FILE *output = tmpfile();
    TEST_ASSERT_NOT_NULL(input);
    TEST_ASSERT_NOT_NULL(output);
    TEST_ASSERT_EQUAL_INT(0, sort_records_checked(input, output, 3, 1));
    rewind(output);
    char line[100];
    TEST_ASSERT_NOT_NULL(fgets(line, sizeof(line), output));
    char *last_comma = strrchr(line, ',');
    TEST_ASSERT_NOT_NULL(last_comma);
    TEST_ASSERT_TRUE(strtod(last_comma + 1, NULL) == 32209.073312);
    fclose(output);
    fclose(input);
}

void test_sort_records_rejects_overlong_word(void) {
    FILE *input = csv_input("1,abcdefghijklmnopqrstuvwxyz,2,3.0\n");
    FILE *output = tmpfile();
    TEST_ASSERT_NOT_NULL(input);
    TEST_ASSERT_NOT_NULL(output);
    TEST_ASSERT_EQUAL_INT(-1, sort_records_checked(input, output, 1, 1));
    fclose(output);
    fclose(input);
}

void test_sort_records_only_reads_available_rows(void) {
    FILE *input = csv_input("1,one,1,1.0\n2,two,2,2.0\n");
    FILE *output = tmpfile();
    TEST_ASSERT_NOT_NULL(input);
    TEST_ASSERT_NOT_NULL(output);
    TEST_ASSERT_EQUAL_INT(0, sort_records_checked(input, output, 2, 1));
    rewind(output);
    char line[100];
    TEST_ASSERT_NOT_NULL(fgets(line, sizeof(line), output));
    TEST_ASSERT_NOT_NULL(fgets(line, sizeof(line), output));
    TEST_ASSERT_NULL(fgets(line, sizeof(line), output));
    fclose(output);
    fclose(input);
}

int main(void) {
    UNITY_BEGIN();
    RUN_TEST(test_merge_sort_generic_integers);
    RUN_TEST(test_quick_sort_generic_integers);
    RUN_TEST(test_sort_empty_arrays);
    RUN_TEST(test_quick_sort_already_sorted_large_array);
    RUN_TEST(test_quick_sort_duplicate_records);
    RUN_TEST(test_sort_records_all_fields_and_algorithms);
    RUN_TEST(test_sort_records_preserves_double_precision);
    RUN_TEST(test_sort_records_rejects_overlong_word);
    RUN_TEST(test_sort_records_only_reads_available_rows);
    return UNITY_END();
}
