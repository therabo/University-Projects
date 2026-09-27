CC = gcc
CFLAGS = -std=c11 -D_POSIX_C_SOURCE=200809L -Wall -Wextra -Wpedantic -I./Distance_algorithms -I./Utils -I./Unity

# Cartelle
BIN_DIR = bin
DIST_DIR = Distance_algorithms
UTILS_DIR = Utils
UNITY_DIR = Unity

# File sorgente per l'eseguibile principale
MAIN_SRC = main.c \
           $(DIST_DIR)/edit_distance.c \
           $(UTILS_DIR)/Utils.c

# File sorgente per l'eseguibile dei test
TEST_SRC = $(UNITY_DIR)/Tests.c \
           $(UNITY_DIR)/unity.c \
           $(DIST_DIR)/edit_distance.c \
           $(UTILS_DIR)/Utils.c

# Eseguibili
MAIN_EXEC = $(BIN_DIR)/main_ex2
TEST_EXEC = $(BIN_DIR)/test_ex2

# Target default
all: $(MAIN_EXEC) $(TEST_EXEC)

# Compilazione dell'eseguibile principale
$(MAIN_EXEC): $(MAIN_SRC) | $(BIN_DIR)
	$(CC) $(CFLAGS) $^ -o $@

# Compilazione dell'eseguibile dei test
$(TEST_EXEC): $(TEST_SRC) | $(BIN_DIR)
	$(CC) $(CFLAGS) $^ -o $@

# Pulizia dei file eseguibili
clean:
	rm -f $(MAIN_EXEC) $(TEST_EXEC)

# Esecuzione dei test
test: $(TEST_EXEC)
	./$(TEST_EXEC)

$(BIN_DIR):
	mkdir -p $(BIN_DIR)

.PHONY: all clean test
