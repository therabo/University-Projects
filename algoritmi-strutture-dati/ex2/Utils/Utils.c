#define _POSIX_C_SOURCE 200809L
#include "Utils.h"
#include "../Distance_algorithms/edit_distance.h"
#include <ctype.h>
#include <limits.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int load_text(const char *filename, char **text) {
    FILE *file = fopen(filename, "rb");
    if (file == NULL) {
        perror(filename);
        return -1;
    }

    size_t capacity = 4096;
    size_t used = 0;
    char *result = malloc(capacity);
    if (result == NULL) {
        fclose(file);
        return -1;
    }

    for (;;) {
        if (used == capacity - 1) {
            if (capacity > SIZE_MAX / 2) break;
            capacity *= 2;
            char *grown = realloc(result, capacity);
            if (grown == NULL) break;
            result = grown;
        }
        size_t read_count = fread(result + used, 1, capacity - used - 1, file);
        used += read_count;
        if (read_count == 0) {
            if (ferror(file)) break;
            result[used] = '\0';
            fclose(file);
            *text = result;
            return 0;
        }
    }

    free(result);
    fclose(file);
    fprintf(stderr, "Unable to read text file: %s\n", filename);
    return -1;
}

static void lowercase(char *word) {
    for (; *word != '\0'; word++) {
        *word = (char) tolower((unsigned char) *word);
    }
}

static int compare_words(const void *left, const void *right) {
    return strcmp(*(const char *const *) left, *(const char *const *) right);
}

void free_dictionary(char **dictionary, size_t dict_size) {
    if (dictionary == NULL) return;
    for (size_t i = 0; i < dict_size; i++) free(dictionary[i]);
    free(dictionary);
}

char **load_dictionary(const char *filename, size_t *dict_size) {
    FILE *file = fopen(filename, "r");
    if (file == NULL) {
        perror(filename);
        return NULL;
    }

    size_t capacity = 1024;
    size_t count = 0;
    char **dictionary = malloc(capacity * sizeof(*dictionary));
    char *line = NULL;
    size_t line_capacity = 0;
    if (dictionary == NULL) {
        fclose(file);
        return NULL;
    }

    while (getline(&line, &line_capacity, file) >= 0) {
        line[strcspn(line, "\r\n")] = '\0';
        if (line[0] == '\0') continue;
        if (count == capacity) {
            if (capacity > SIZE_MAX / (2 * sizeof(*dictionary))) goto error;
            capacity *= 2;
            char **grown = realloc(dictionary, capacity * sizeof(*dictionary));
            if (grown == NULL) goto error;
            dictionary = grown;
        }
        lowercase(line);
        dictionary[count] = strdup(line);
        if (dictionary[count] == NULL) goto error;
        count++;
    }
    if (ferror(file)) goto error;
    free(line);
    fclose(file);
    qsort(dictionary, count, sizeof(*dictionary), compare_words);
    *dict_size = count;
    return dictionary;

error:
    fprintf(stderr, "Unable to load dictionary: %s\n", filename);
    free(line);
    fclose(file);
    free_dictionary(dictionary, count);
    return NULL;
}

static void normalize(char *word) {
    char *write = word;
    for (char *read = word; *read != '\0'; read++) {
        unsigned char character = (unsigned char) *read;
        if (isalnum(character) || character == '\'') {
            *write++ = (char) tolower(character);
        }
    }
    *write = '\0';
}

int dyn_distance(const char *text, char **dictionary, size_t dict_size) {
    if (dict_size == 0) {
        fprintf(stderr, "Dictionary is empty.\n");
        return -1;
    }
    char *copy = strdup(text);
    char **suggestions = malloc(dict_size * sizeof(*suggestions));
    if (copy == NULL || suggestions == NULL) {
        free(copy);
        free(suggestions);
        return -1;
    }

    for (char *word = strtok(copy, " \t\r\n"); word != NULL;
         word = strtok(NULL, " \t\r\n")) {
        normalize(word);
        if (*word == '\0') continue;
        char *key = word;
        if (bsearch(&key, dictionary, dict_size, sizeof(*dictionary), compare_words) != NULL) {
            printf("La parola '%s' è corretta.\n\n", word);
            continue;
        }

        int minimum = INT_MAX;
        size_t suggestion_count = 0;
        size_t word_length = strlen(word);
        for (size_t i = 0; i < dict_size; i++) {
            size_t candidate_length = strlen(dictionary[i]);
            size_t length_difference = word_length > candidate_length
                                       ? word_length - candidate_length
                                       : candidate_length - word_length;
            if (length_difference > (size_t) minimum) continue;
            int distance = edit_distance_dyn(word, dictionary[i]);
            if (distance < 0) {
                free(suggestions);
                free(copy);
                return -1;
            }
            if (distance < minimum) {
                minimum = distance;
                suggestion_count = 0;
            }
            if (distance == minimum) suggestions[suggestion_count++] = dictionary[i];
        }

        printf("Parola: '%s'\n|Edit distance minima|: %d\nParole possibili:\n", word, minimum);
        for (size_t i = 0; i < suggestion_count; i++) printf("  - %s\n", suggestions[i]);
        putchar('\n');
    }

    free(suggestions);
    free(copy);
    return 0;
}
