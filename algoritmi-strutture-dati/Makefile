.PHONY: all test clean

all:
	$(MAKE) -C ex1 all
	$(MAKE) -C ex2 all
	$(MAKE) -C ex3-4 all

test: all
	$(MAKE) -C ex1 test
	$(MAKE) -C ex2 test
	$(MAKE) -C ex3-4 run_tests

clean:
	$(MAKE) -C ex1 clean
	$(MAKE) -C ex2 clean
	$(MAKE) -C ex3-4 clean
