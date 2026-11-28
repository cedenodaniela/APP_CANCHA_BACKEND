package com.canchapp.canchas.domain;

import com.canchapp.shared.domain.ExcepcionNegocio;

/**
 * RF-04: «al desactivar una cancha con reservas activas, el sistema muestra cuántas reservas hay y
 * pide confirmación». Los clientes reconocen el código {@code RESERVAS_ACTIVAS}, muestran el mensaje
 * y, si la persona confirma, repiten la solicitud con {@code confirmar = true}.
 */
public class ReservasActivasException extends ExcepcionNegocio {

    private final int reservasActivas;

    public ReservasActivasException(int reservasActivas) {
        super("RESERVAS_ACTIVAS", mensaje(reservasActivas), Tipo.CONFLICTO);
        this.reservasActivas = reservasActivas;
    }

    private static String mensaje(int reservas) {
        String cuantas = reservas == 1 ? "1 reserva activa" : reservas + " reservas activas";
        return "La cancha tiene " + cuantas + ". Confirma si quieres desactivarla de todos modos.";
    }

    public int getReservasActivas() {
        return reservasActivas;
    }
}
