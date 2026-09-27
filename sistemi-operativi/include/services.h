/**
 * @file services.h
 * @brief Defines the service types and their average processing times.
 *
 * This file contains an enumeration of the various services provided by the post office
 * and a constant array that stores the average time required to process each service.
 * NUM_SERVICES, represents the total number of services defined,
 * ensuring that the TIME_AVERAGE_SERVICES array is correctly sized.
 *
 *
 */

#ifndef SISTEMI_OPERATIVI_LAB2024_2025_SERVICES_H
#define SISTEMI_OPERATIVI_LAB2024_2025_SERVICES_H

typedef enum {
    SERVIZIO_INVIO_RITIRO_PACCHI,
    SERVIZIO_INVIO_RITIRO_LETTERE,
    SERVIZIO_PRELIEVI_VERSAMENTI_BANCOPOSTA,
    SERVIZIO_PAGAMENTO_BOLLETTINI,
    SERVIZIO_ACQUISTO_PRODOTTI_FINANZIARI,
    SERVIZIO_ACQUISTO_OROLOGI_BRACCIALETTI,
    NUM_SERVICES
} service_t;

/**
 * @brief Average processing times for each service type.
 *
 * The TIME_AVERAGE_SERVICES array contains the average processing time
 * for each service type, where each index corresponds to the value of the service_t enum.
 */
static const int TIME_AVERAGE_SERVICES[NUM_SERVICES] = {10, 8, 6, 8, 20, 20};

#endif //SISTEMI_OPERATIVI_LAB2024_2025_SERVICES_H
