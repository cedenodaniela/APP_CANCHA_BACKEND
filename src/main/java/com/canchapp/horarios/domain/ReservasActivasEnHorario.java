package com.canchapp.horarios.domain;

import java.time.LocalDate;

/**
 * Puerto para saber cuántas reservas activas caen en un rango antes de bloquearlo (RF-05).
 * <p>
 * Lo implementa el módulo {@code reservas}. Hasta que exista (RF-07, iteración 2) se usa una
 * implementación que responde 0.
 */
public interface ReservasActivasEnHorario {

    /** @param canchaId {@code null} para contar en todas las canchas */
    int contar(Long canchaId, LocalDate fecha, HoraDelDia inicio, HoraDelDia fin);
}
