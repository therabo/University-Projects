#ifndef EX2_UTILS_H
#define EX2_UTILS_H

#include <stddef.h>

int load_text(const char *filename, char **text);
char **load_dictionary(const char *filename, size_t *dict_size);
void free_dictionary(char **dictionary, size_t dict_size);
/* dictionary must be the sorted array returned by load_dictionary. */
int dyn_distance(const char *text, char **dictionary, size_t dict_size);

#endif
