package com.canchapp.configuracion.domain;

/**
 * Parámetros de las reglas de reserva que configura el administrador (RF-03).
 * <p>
 * Ninguna regla escribe estos valores fijos en el código (AGENTS.md §5): los módulos de
 * reservas y pagos los leen de aquí, y un cambio aplica a las reservas nuevas.
 * <p>
 * Los mínimos salen del DERCAS («no se aceptan 0 días de anticipación ni un tiempo de expiración
 * negativo»). El DERCAS no fija máximos, así que no se imponen.
 *
 * @param minutosExpiracion         minutos para subir el comprobante de una transferencia
 * @param horasPlazoCancelacion     horas antes del inicio hasta las que se puede cancelar
 * @param maxReservasEfectivo       reservas en efectivo activas que puede tener un cliente
 * @param diasAnticipacion          días hacia adelante que se pueden reservar, contando desde hoy
 * @param limitarTrasInasistencia   si un cliente que no asistió queda limitado a pagar por transferencia
 * @see "RN-05 · minutosExpiracion"
 * @see "RN-09 · horasPlazoCancelacion"
 * @see "RN-07 · maxReservasEfectivo"
 * @see "RN-02 · diasAnticipacion"
 * @see "RN-08 · limitarTrasInasistencia"
 */
public record PoliticaReserva(Integer minutosExpiracion, Integer horasPlazoCancelacion, Integer maxReservasEfectivo,
                              Integer diasAnticipacion, Boolean limitarTrasInasistencia) {

    /** Valida cada parámetro con su mínimo. */
    public PoliticaReserva {
        minimo(minutosExpiracion, 1, "El tiempo para subir el comprobante debe ser de al menos 1 minuto.");
        minimo(horasPlazoCancelacion, 0, "El plazo de cancelación no puede ser negativo.");
        minimo(maxReservasEfectivo, 0, "El máximo de reservas en efectivo no puede ser negativo.");
        minimo(diasAnticipacion, 1, "Los días de anticipación deben ser al menos 1.");
        if (limitarTrasInasistencia == null) {
            throw new ConfiguracionInvalidaException("Indica si se limita al cliente tras una inasistencia.");
        }
    }

    private static void minimo(Integer valor, int minimo, String mensaje) {
        if (valor == null || valor < minimo) {
            throw new ConfiguracionInvalidaException(mensaje);
        }
    }
}
