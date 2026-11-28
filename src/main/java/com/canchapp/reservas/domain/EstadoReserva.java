package com.canchapp.reservas.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Estados de una reserva (AGENTS.md §5, DERCAS §7.2, Figura 7).
 *
 * <pre>
 * PENDIENTE_COMPROBANTE → EN_VERIFICACION → PAGADA → COMPLETADA
 * PENDIENTE_COMPROBANTE → EXPIRADA            (pasa el tiempo para subir el comprobante)
 * EN_VERIFICACION       → RECHAZADA           (el administrador rechaza)
 * CONFIRMADA (efectivo) → PAGADA | NO_ASISTIO
 * PENDIENTE_COMPROBANTE | CONFIRMADA | PAGADA → CANCELADA   (dentro del plazo)
 * </pre>
 */
public enum EstadoReserva {
    PENDIENTE_COMPROBANTE,
    EN_VERIFICACION,
    CONFIRMADA,
    PAGADA,
    COMPLETADA,
    EXPIRADA,
    RECHAZADA,
    NO_ASISTIO,
    CANCELADA;

    /** Estados que ocupan el horario: nadie más puede reservar esos bloques (RN-04). */
    private static final Set<EstadoReserva> ACTIVOS = EnumSet.of(PENDIENTE_COMPROBANTE, EN_VERIFICACION, CONFIRMADA, PAGADA);

    /** @see "RN-04 · solo se bloquea el horario en PENDIENTE_COMPROBANTE, EN_VERIFICACION, CONFIRMADA y PAGADA" */
    public boolean bloqueaHorario() {
        return ACTIVOS.contains(this);
    }
}
