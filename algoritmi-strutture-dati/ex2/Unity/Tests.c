#define _POSIX_C_SOURCE 200809L
#include "unity.h"
#include "../Distance_algorithms/edit_distance.h"
#include "../Utils/Utils.h"
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

/* Test per la funzione edit_distance con stringhe vuote */
void test_edit_distance_empty_strings(void) {
    const char *s1 = "";
    const char *s2 = "";
    int expected = 0;

    int result_recursive = edit_distance(s1, s2);
    int result_dynamic = edit_distance_dyn(s1, s2);

    TEST_ASSERT_EQUAL_INT_MESSAGE(expected, result_recursive, "edit_distance con stringhe vuote fallito (ricorsivo)");
    TEST_ASSERT_EQUAL_INT_MESSAGE(expected, result_dynamic, "edit_distance_dyn con stringhe vuote fallito (dinamico)");
}

/* Test per la funzione edit_distance con stringa vuota e stringa non vuota */
void test_edit_distance_empty_and_non_empty(void) {
    const char *s1 = "";
    const char *s2 = "test";
    int expected = strlen(s2);

    int result_recursive = edit_distance(s1, s2);
    int result_dynamic = edit_distance_dyn(s1, s2);

    TEST_ASSERT_EQUAL_INT_MESSAGE(expected, result_recursive, "edit_distance con stringa vuota e non vuota fallito (ricorsivo)");
    TEST_ASSERT_EQUAL_INT_MESSAGE(expected, result_dynamic, "edit_distance_dyn con stringa vuota e non vuota fallito (dinamico)");
}

/* Test per la funzione edit_distance con stringhe identiche */
void test_edit_distance_same_strings(void) {
    const char *s1 = "test";
    const char *s2 = "test";
    int expected = 0;

    int result_recursive = edit_distance(s1, s2);
    int result_dynamic = edit_distance_dyn(s1, s2);

    TEST_ASSERT_EQUAL_INT_MESSAGE(expected, result_recursive, "edit_distance con stringhe identiche fallito (ricorsivo)");
    TEST_ASSERT_EQUAL_INT_MESSAGE(expected, result_dynamic, "edit_distance_dyn con stringhe identiche fallito (dinamico)");
}

/* Test per la funzione edit_distance con una cancellazione */
void test_edit_distance_one_deletion(void) {
    const char *s1 = "test";
    const char *s2 = "tes";
    int expected = 1;

    int result_recursive = edit_distance(s1, s2);
    int result_dynamic = edit_distance_dyn(s1, s2);

    TEST_ASSERT_EQUAL_INT_MESSAGE(expected, result_recursive, "edit_distance con una cancellazione fallito (ricorsivo)");
    TEST_ASSERT_EQUAL_INT_MESSAGE(expected, result_dynamic, "edit_distance_dyn con una cancellazione fallito (dinamico)");
}

/* Test per la funzione edit_distance con una inserzione */
void test_edit_distance_one_insertion(void) {
    const char *s1 = "tes";
    const char *s2 = "test";
    int expected = 1;

    int result_recursive = edit_distance(s1, s2);
    int result_dynamic = edit_distance_dyn(s1, s2);

    TEST_ASSERT_EQUAL_INT_MESSAGE(expected, result_recursive, "edit_distance con una inserzione fallito (ricorsivo)");
    TEST_ASSERT_EQUAL_INT_MESSAGE(expected, result_dynamic, "edit_distance_dyn con una inserzione fallito (dinamico)");
}

/* Test per la funzione edit_distance con stringhe completamente diverse */
void test_edit_distance_completely_different_strings(void) {
    const char *s1 = "aaa";
    const char *s2 = "bbb";
    int expected = 6;

    int result_recursive = edit_distance(s1, s2);
    int result_dynamic = edit_distance_dyn(s1, s2);

    TEST_ASSERT_EQUAL_INT_MESSAGE(expected, result_recursive, "edit_distance con stringhe diverse fallito (ricorsivo)");
    TEST_ASSERT_EQUAL_INT_MESSAGE(expected, result_dynamic, "edit_distance_dyn con stringhe diverse fallito (dinamico)");
}

/* Test per la funzione edit_distance con stringhe lunghe */
void test_edit_distance_long_strings(void) {
    const char *s1 = "abcdefgh";
    const char *s2 = "abcdwxyz";
    int expected = 8;

    int result_recursive = edit_distance(s1, s2);
    int result_dynamic = edit_distance_dyn(s1, s2);

    TEST_ASSERT_EQUAL_INT_MESSAGE(expected, result_recursive, "edit_distance con stringhe lunghe fallito (ricorsivo)");
    TEST_ASSERT_EQUAL_INT_MESSAGE(expected, result_dynamic, "edit_distance_dyn con stringhe lunghe fallito (dinamico)");
}

/* Test per la funzione edit_distance con operazioni multiple */
void test_edit_distance_multiple_operations(void) {
    const char *s1 = "kitten";
    const char *s2 = "sitting";
    int expected = 5;

    int result_recursive = edit_distance(s1, s2);
    int result_dynamic = edit_distance_dyn(s1, s2);

    TEST_ASSERT_EQUAL_INT_MESSAGE(expected, result_recursive, "edit_distance con operazioni multiple fallito (ricorsivo)");
    TEST_ASSERT_EQUAL_INT_MESSAGE(expected, result_dynamic, "edit_distance_dyn con operazioni multiple fallito (dinamico)");
}

/* Test per confrontare le versioni ricorsiva e dinamica con stringhe */
void test_edit_distance_recursive_vs_dynamic(void) {
    const char *s1 = "pneumono";
    const char *s2 = "ultramicro";
    int expected_recursive = edit_distance(s1, s2);
    int expected_dynamic = edit_distance_dyn(s1, s2);

    TEST_ASSERT_EQUAL_INT_MESSAGE(expected_recursive, expected_dynamic, "Le versioni ricorsiva e dinamica non corrispondono");
}

void test_edit_distance_dynamic_over_hundred_characters(void) {
    char first[122];
    char second[121];
    memset(first, 'a', 120);
    memset(second, 'a', 120);
    first[120] = 'b';
    first[121] = '\0';
    second[120] = '\0';
    TEST_ASSERT_EQUAL_INT(1, edit_distance_dyn(first, second));
}

void test_dictionary_loads_more_than_ten_thousand_words(void) {
    char path[] = "/tmp/alg-dictionary-XXXXXX";
    int descriptor = mkstemp(path);
    TEST_ASSERT_TRUE(descriptor >= 0);
    FILE *file = fdopen(descriptor, "w");
    TEST_ASSERT_NOT_NULL(file);
    for (int i = 0; i < 10001; i++) fprintf(file, "word%d\n", i);
    fclose(file);

    size_t count = 0;
    char **dictionary = load_dictionary(path, &count);
    unlink(path);
    TEST_ASSERT_NOT_NULL(dictionary);
    TEST_ASSERT_EQUAL_UINT(10001, count);
    free_dictionary(dictionary, count);
}

void test_text_loader_does_not_truncate_large_files(void) {
    char path[] = "/tmp/alg-text-XXXXXX";
    int descriptor = mkstemp(path);
    TEST_ASSERT_TRUE(descriptor >= 0);
    FILE *file = fdopen(descriptor, "w");
    TEST_ASSERT_NOT_NULL(file);
    for (int i = 0; i < 12000; i++) fputc('a', file);
    fclose(file);

    char *text = NULL;
    int result = load_text(path, &text);
    unlink(path);
    TEST_ASSERT_EQUAL_INT(0, result);
    TEST_ASSERT_EQUAL_UINT(12000, strlen(text));
    free(text);
}

int main(void) {
    UNITY_BEGIN();

    RUN_TEST(test_edit_distance_empty_strings);
    RUN_TEST(test_edit_distance_empty_and_non_empty);
    RUN_TEST(test_edit_distance_same_strings);
    RUN_TEST(test_edit_distance_one_deletion);
    RUN_TEST(test_edit_distance_one_insertion);
    RUN_TEST(test_edit_distance_completely_different_strings);
    RUN_TEST(test_edit_distance_long_strings);
    RUN_TEST(test_edit_distance_multiple_operations);
    RUN_TEST(test_edit_distance_recursive_vs_dynamic);
    RUN_TEST(test_edit_distance_dynamic_over_hundred_characters);
    RUN_TEST(test_dictionary_loads_more_than_ten_thousand_words);
    RUN_TEST(test_text_loader_does_not_truncate_large_files);

    return UNITY_END();
}
