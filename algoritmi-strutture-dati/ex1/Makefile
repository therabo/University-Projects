CC = gcc
CFLAGS = -std=c11 -Wall -Wextra -Wpedantic

SOURCES = Algorithms/MergeSort.c Algorithms/QuickSort.c Structures/Record.c Records/RecordSort.c

.PHONY: all test clean

all: bin/main_ex1 bin/test_ex1

bin:
	mkdir -p bin

bin/main_ex1: main.c $(SOURCES) | bin
	$(CC) $(CFLAGS) $^ -o $@

bin/test_ex1: Unity/Tests.c Unity/unity.c $(SOURCES) | bin
	$(CC) $(CFLAGS) $^ -o $@

test: bin/test_ex1
	./bin/test_ex1

clean:
	rm -f bin/main_ex1 bin/test_ex1
