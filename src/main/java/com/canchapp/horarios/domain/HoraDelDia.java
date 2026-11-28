package com.canchapp.horarios.domain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hora del día en minutos desde las 00:00, de 00:00 a 24:00.
 * <p>
 * {@link java.time.LocalTime} no admite «24:00», y el complejo cierra justo a esa hora (RN-02b:
 * último bloque 23:30–24:00). Solo se aceptan horas en punto o y media, porque las reservas se
 * hacen en bloques de 30 minutos (RN-01).
 *
 * @param minutos de 0 a 1440, múltiplo de 30
 * @see "RN-01 · bloques de 30 minutos"
 * @see "RN-02b · horario hasta las 24:00"
 */
public record HoraDelDia(int minutos) implements Comparable<HoraDelDia> {

    /** RN-01: duración de un bloque. */
    public static final int MINUTOS_BLOQUE = 30;
    public static final int MINUTOS_DIA = 24 * 60;

    private static final Pattern FORMATO = Pattern.compile("^(\\d{1,2}):(\\d{2})$");

    public HoraDelDia {
        if (minutos < 0 || minutos > MINUTOS_DIA) {
            throw new HorarioInvalidoException("La hora debe estar entre 00:00 y 24:00.");
        }
        if (minutos % MINUTOS_BLOQUE != 0) {
            throw new HorarioInvalidoException("Usa horas en punto o y media: las reservas son por bloques de 30 minutos.");
        }
    }

    /**
     * @param texto «HH:mm», por ejemplo «10:00», «17:30» o «24:00»
     * @throws HorarioInvalidoException si falta, no tiene ese formato o no cae en un bloque de 30 minutos
     */
    public static HoraDelDia de(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new HorarioInvalidoException("Ingresa " + campo + ".");
        }
        Matcher m = FORMATO.matcher(texto.trim());
        if (!m.matches() || Integer.parseInt(m.group(2)) >= 60) {
            throw new HorarioInvalidoException("Escribe " + campo + " como HH:mm, por ejemplo 18:30.");
        }
        return new HoraDelDia(Integer.parseInt(m.group(1)) * 60 + Integer.parseInt(m.group(2)));
    }

    /** «HH:mm»; las 24:00 se escriben «24:00». */
    public String texto() {
        return String.format("%02d:%02d", minutos / 60, minutos % 60);
    }

    public boolean esAntesDe(HoraDelDia otra) {
        return minutos < otra.minutos;
    }

    @Override
    public int compareTo(HoraDelDia otra) {
        return Integer.compare(minutos, otra.minutos);
    }

    @Override
    public String toString() {
        return texto();
    }
}
