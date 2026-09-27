#include <stdio.h>
#include <stdlib.h>
#include "Utils/Utils.h"

int main(int argc, char **argv) {
    if (argc != 3) {
        fprintf(stderr, "Usage: %s <dictionary.txt> <correctme.txt>\n", argv[0]);
        return EXIT_FAILURE;
    }

    char *text = NULL;
    if (load_text(argv[2], &text) != 0) {
        return EXIT_FAILURE;
    }

    size_t dict_size = 0;
    char **dictionary = load_dictionary(argv[1], &dict_size);
    if (dictionary == NULL) {
        free(text);
        return EXIT_FAILURE;
    }

    int result = dyn_distance(text, dictionary, dict_size);
    free_dictionary(dictionary, dict_size);
    free(text);
    return result == 0 ? EXIT_SUCCESS : EXIT_FAILURE;
}
