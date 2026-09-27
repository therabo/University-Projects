# ============================================================================
#  Makefile – Post-Office Simulator (SO Lab 2024-2025)
# ----------------------------------------------------------------------------
#  Targets
#      make            → compile all executable in ./bin
#      make run        → start the director main
#      make clean      → remove .obj
#      make distclean  → remove also  ./bin
# ─────────────────────────────────────────────────────────────────────────────
#  Settings
# ─────────────────────────────────────────────────────────────────────────────
CC       := gcc
CFLAGS   := -std=c11 -D_GNU_SOURCE -Wvla -Wextra -Werror -Wall -pedantic
LDFLAGS  :=
INC_DIR  := include
SRC_DIR  := src
OBJ_DIR  := build
BIN_DIR  := bin

# ─────────────────────────────────────────────────────────────────────────────
#
# ─────────────────────────────────────────────────────────────────────────────
CORE_SRCS := \
	$(SRC_DIR)/configuration.c \
	$(SRC_DIR)/day_end_signal.c \
	$(SRC_DIR)/director.c \
	$(SRC_DIR)/ipc_lights.c \
	$(SRC_DIR)/ipc_shared.c \
	$(SRC_DIR)/operator.c \
	$(SRC_DIR)/random_utils.c \
	$(SRC_DIR)/stats.c \
	$(SRC_DIR)/ticket_dispenser.c \
	$(SRC_DIR)/user.c \
	$(SRC_DIR)/window.c

CORE_OBJS := $(patsubst $(SRC_DIR)/%.c,$(OBJ_DIR)/%.o,$(CORE_SRCS))

# ─────────────────────────────────────────────────────────────────────────────
#
# ─────────────────────────────────────────────────────────────────────────────
MAIN_SRCS_FILES := director_main.c operator_main.c ticket_dispenser_main.c user_main.c add_user_main.c
MAIN_BINS := $(patsubst %.c,$(BIN_DIR)/%,$(MAIN_SRCS_FILES))
MAIN_OBJS := $(patsubst %.c,$(OBJ_DIR)/%.o,$(MAIN_SRCS_FILES))

# Target
.PHONY: all
all: $(MAIN_BINS)

# ---- Linking --------------------------------------------------------------

$(BIN_DIR)/director_main:          $(OBJ_DIR)/director_main.o          $(CORE_OBJS) | $(BIN_DIR)
	$(CC) $(LDFLAGS) $^ -o $@

$(BIN_DIR)/operator_main:          $(OBJ_DIR)/operator_main.o          $(CORE_OBJS) | $(BIN_DIR)
	$(CC) $(LDFLAGS) $^ -o $@

$(BIN_DIR)/ticket_dispenser_main:  $(OBJ_DIR)/ticket_dispenser_main.o  $(CORE_OBJS) | $(BIN_DIR)
	$(CC) $(LDFLAGS) $^ -o $@

$(BIN_DIR)/user_main:              $(OBJ_DIR)/user_main.o              $(CORE_OBJS) | $(BIN_DIR)
	$(CC) $(LDFLAGS) $^ -o $@

$(BIN_DIR)/add_user_main:              $(OBJ_DIR)/add_user_main.o              $(CORE_OBJS) | $(BIN_DIR)
	$(CC) $(LDFLAGS) $^ -o $@

# ---- Compilation library -------------------------------------------------

$(OBJ_DIR)/%.o: $(SRC_DIR)/%.c | $(OBJ_DIR)
	$(CC) $(CFLAGS) -I$(INC_DIR) -c $< -o $@

# ---- Compilation main ----------------------------------------

$(OBJ_DIR)/%.o: %.c | $(OBJ_DIR)
	$(CC) $(CFLAGS) -I$(INC_DIR) -c $< -o $@

# ---- Utility dirs ----------------------------------------------------------
$(OBJ_DIR) $(BIN_DIR):
	mkdir -p $@

# ---- Run  ------------------------------------------------------------------
.PHONY: run
run: all
	$(BIN_DIR)/director_main

# ---- Clean -----------------------------------------------------------------
.PHONY: clean
clean:
	$(RM) -r $(OBJ_DIR)


.PHONY: distclean
distclean: clean
	$(RM) -r $(BIN_DIR)

# ---------------------------------------------------------------------------
