package com.canchapp.horarios.domain;

import java.time.DayOfWeek;
import java.util.Objects;

/**
 * Horario de atención de un día de la semana (RF-05). Un día cerrado no tiene horas.
 *
 * @see "RN-02b · todos los días de 10:00 a 24:00 (valor inicial, configurable)"
 */
public record HorarioDia(DayOfWeek dia, boolean abierto, HoraDelDia apertura, HoraDelDia cierre) {

    public HorarioDia {
        Objects.requireNonNull(dia, "dia");
        if (abierto) {
            if (apertura == null || cierre == null) {
                throw new HorarioInvalidoException("Indica la hora de apertura y de cierre del " + DiasSemana.nombre(dia).toLowerCase() + ".");
            }
            if (!apertura.esAntesDe(cierre)) {
                throw new HorarioInvalidoException("El " + DiasSemana.nombre(dia).toLowerCase()
                        + " la apertura debe ser antes del cierre.");
            }
        } else {
            apertura = null;
            cierre = null;
        }
    }

    public static HorarioDia cerrado(DayOfWeek dia) {
        return new HorarioDia(dia, false, null, null);
    }
}
