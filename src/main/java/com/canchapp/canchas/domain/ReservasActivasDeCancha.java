package com.canchapp.canchas.domain;

/**
 * Puerto para saber cuántas reservas activas tiene una cancha, antes de desactivarla (RF-04).
 * <p>
 * Lo implementa el módulo {@code reservas}. Hasta que exista (RF-07, iteración 2) se usa una
 * implementación que responde 0.
 */
public interface ReservasActivasDeCancha {

    /** Reservas en PENDIENTE_COMPROBANTE, EN_VERIFICACION, CONFIRMADA o PAGADA de esa cancha. */
    int contar(Long canchaId);
}
