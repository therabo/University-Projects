JAVAC = javac
# Directory
SRC_DIR = src
BIN_DIR = bin
LIB_DIR = lib

# Definizione delle librerie
JUNIT_JAR = $(LIB_DIR)/junit-4.13.2.jar
HAMCREST_JAR = $(LIB_DIR)/hamcrest-core-1.3.jar

# Rilevamento del sistema operativo per impostare il separatore di classpath e comandi di pulizia
ifeq ($(OS),Windows_NT)
    CLASSPATH_SEPARATOR = ;
    CLEAN_CMD = del /s /q $(BIN_DIR)\*.class
    MKDIR_CMD = if not exist $(BIN_DIR) mkdir $(BIN_DIR)
    RUN_MAIN_CMD = java -cp "bin;lib/junit-4.13.2.jar;lib/hamcrest-core-1.3.jar" Main
    RUN_TESTS_CMD = java -cp "bin;lib/junit-4.13.2.jar;lib/hamcrest-core-1.3.jar" org.junit.runner.JUnitCore Unity.UnityGraphTest Unity.UnityQueueTest Unity.UnityPrimTest
else
    CLASSPATH_SEPARATOR = :
    CLEAN_CMD = find $(BIN_DIR) -name "*.class" -delete
    MKDIR_CMD = mkdir -p $(BIN_DIR)
    RUN_MAIN_CMD = java -cp "bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" Main
    RUN_TESTS_CMD = java -cp "bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" org.junit.runner.JUnitCore Unity.UnityGraphTest Unity.UnityQueueTest Unity.UnityPrimTest
endif

# Classpath: include bin directory and all JARs in lib directory
CLASSPATH = $(BIN_DIR)$(CLASSPATH_SEPARATOR)$(JUNIT_JAR)$(CLASSPATH_SEPARATOR)$(HAMCREST_JAR)

# Elenco manuale dei file .java
SOURCES = \
    src/Graph/AbstractEdge.java \
    src/Graph/AbstractGraph.java \
    src/Graph/Edge.java \
    src/Graph/Graph.java \
    src/Prim/Prim.java \
    src/Prim/PrimCsv.java \
    src/QueueClass/AbstractQueue.java \
    src/QueueClass/PriorityQueue.java \
    src/Unity/UnityGraphTest.java \
    src/Unity/UnityQueueTest.java \
    src/Unity/UnityPrimTest.java \
    src/Main.java

# Obiettivi phony
.PHONY: all clean run_main run_tests run_prim

# Obiettivo 'all' per compilare tutti i file .java
all: $(BIN_DIR) $(BIN_DIR)/Main.class

# Creazione della directory 'bin/' se non esiste
$(BIN_DIR):
	@echo "Creazione della directory 'bin/' se non esiste..."
	$(MKDIR_CMD)

# Compilazione della classe principale e di tutte le altre classi
$(BIN_DIR)/Main.class: $(SOURCES)
	$(JAVAC) -d $(BIN_DIR) -cp "$(CLASSPATH)" $(SOURCES)

# Obiettivo per eseguire la classe principale
run_main: all
	$(RUN_MAIN_CMD)

# Obiettivo per eseguire le classi di test
run_tests: all
	$(RUN_TESTS_CMD)

run_prim: all
	java -cp "$(BIN_DIR)" Prim.Prim "$(GRAPH)"

# Pulizia dei file compilati
clean:
	$(CLEAN_CMD)
