package com.canchapp.reservas.domain;

import com.canchapp.shared.domain.ExcepcionNegocio;

/**
 * Una regla de la reserva no se cumple: bloques inválidos (RN-01), una transición de estado que no
 * existe, una cancelación fuera de plazo (RN-09) o un rechazo sin motivo (RN-06).
 */
public class ReservaInvalidaException extends ExcepcionNegocio {

    public ReservaInvalidaException(String codigo, String mensaje, Tipo tipo) {
        super(codigo, mensaje, tipo);
    }

    /** Datos de la reserva que no cumplen una regla. */
    public static ReservaInvalidaException datos(String mensaje) {
        return new ReservaInvalidaException("RESERVA_INVALIDA", mensaje, Tipo.DATOS_INVALIDOS);
    }

    /** La reserva no puede pasar a ese estado desde el que tiene. */
    public static ReservaInvalidaException transicion(EstadoReserva actual, String accion) {
        return new ReservaInvalidaException("TRANSICION_INVALIDA",
                "No se puede " + accion + " una reserva en estado " + actual + ".", Tipo.CONFLICTO);
    }
}
