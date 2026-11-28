package com.canchapp.horarios.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Rango de horas de una fecha en que no se puede reservar (RF-05): mantenimiento, un evento, un
 * feriado. Puede ser de una cancha o, sin cancha, de todo el complejo.
 *
 * @param canchaId {@code null} si bloquea todas las canchas
 * @see "RF-05: un bloqueo deja esos bloques no disponibles para el cliente"
 */
public record Bloqueo(Long id, Long canchaId, LocalDate fecha, HoraDelDia inicio, HoraDelDia fin, String motivo,
                      Instant creadoEn) {

    public static final int LARGO_MAXIMO_MOTIVO = 200;

    public Bloqueo {
        if (fecha == null) {
            throw new HorarioInvalidoException("Elige la fecha del bloqueo.");
        }
        Objects.requireNonNull(inicio, "inicio");
        Objects.requireNonNull(fin, "fin");
        if (!inicio.esAntesDe(fin)) {
            throw new HorarioInvalidoException("La hora de inicio del bloqueo debe ser antes de la hora de fin.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new HorarioInvalidoException("Escribe el motivo del bloqueo.");
        }
        motivo = motivo.trim();
        if (motivo.length() > LARGO_MAXIMO_MOTIVO) {
            throw new HorarioInvalidoException("El motivo admite hasta " + LARGO_MAXIMO_MOTIVO + " caracteres.");
        }
    }

    /**
     * Crea un bloqueo nuevo. No se bloquean fechas pasadas: ya no hay nada que reservar.
     *
     * @param hoy la fecha de hoy en America/Guayaquil
     */
    public static Bloqueo nuevo(Long canchaId, LocalDate fecha, HoraDelDia inicio, HoraDelDia fin, String motivo,
                                LocalDate hoy, Instant ahora) {
        if (fecha != null && fecha.isBefore(hoy)) {
            throw new HorarioInvalidoException("La fecha del bloqueo no puede ser anterior a hoy.");
        }
        return new Bloqueo(null, canchaId, fecha, inicio, fin, motivo, ahora);
    }

    public boolean esDeTodoElComplejo() {
        return canchaId == null;
    }
}
