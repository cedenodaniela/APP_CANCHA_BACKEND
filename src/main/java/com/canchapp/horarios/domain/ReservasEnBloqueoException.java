package com.canchapp.horarios.domain;

import com.canchapp.shared.domain.ExcepcionNegocio;

/**
 * RF-05: «si el bloqueo cae sobre una reserva activa, el sistema avisa antes de guardarlo». Los
 * clientes reconocen el código {@code RESERVAS_ACTIVAS}, muestran el mensaje y, si la persona
 * confirma, repiten la solicitud con {@code confirmar = true}.
 */
public class ReservasEnBloqueoException extends ExcepcionNegocio {

    private final int reservasActivas;

    public ReservasEnBloqueoException(int reservasActivas) {
        super("RESERVAS_ACTIVAS", mensaje(reservasActivas), Tipo.CONFLICTO);
        this.reservasActivas = reservasActivas;
    }

    private static String mensaje(int reservas) {
        String cuantas = reservas == 1 ? "1 reserva activa" : reservas + " reservas activas";
        return "Ese horario tiene " + cuantas + ". Confirma si quieres bloquearlo de todos modos.";
    }

    public int getReservasActivas() {
        return reservasActivas;
    }
}
