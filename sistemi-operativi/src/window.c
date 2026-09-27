#include "../include/window.h"

void window_init(window_t *w, int windowId) {
    if (!w) return;

    w->windowId = windowId;
    w->currentServiceType = -1;

    w->isOccupied = false;
    w->currentOperatorPid = -1;
}

void window_set_service(window_t *w, int service) {
    if (!w) return;
    w->currentServiceType = service;
}

bool window_occupy(window_t *w, pid_t operatorPid) {
    if (!w) return false;

    if (w->isOccupied) {
        return false;
    }
    w->isOccupied = true;
    w->currentOperatorPid = operatorPid;
    return true;
}

void window_free(window_t *w) {
    if (!w) return;
    w->isOccupied = false;
    w->currentOperatorPid = -1;
}
