package com.canchapp.horarios.domain;

import java.time.DayOfWeek;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Nombres de los días en la API («LUNES» … «DOMINGO») y su conversión a {@link DayOfWeek}.
 */
public final class DiasSemana {

    private static final List<String> NOMBRES =
            List.of("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO");

    private DiasSemana() {
    }

    public static String nombre(DayOfWeek dia) {
        return NOMBRES.get(dia.getValue() - 1);
    }

    /** @throws HorarioInvalidoException si no es un día de la semana */
    public static DayOfWeek dia(String nombre) {
        String limpio = nombre == null ? "" : nombre.trim().toUpperCase(Locale.ROOT)
                .replace('É', 'E').replace('Á', 'A');
        int i = NOMBRES.indexOf(limpio);
        if (i < 0) {
            throw new HorarioInvalidoException("«" + nombre + "» no es un día de la semana.");
        }
        return DayOfWeek.of(i + 1);
    }

    /** @throws HorarioInvalidoException si la lista está vacía o tiene un día que no existe */
    public static Set<DayOfWeek> dias(Collection<String> nombres) {
        if (nombres == null || nombres.isEmpty()) {
            throw new HorarioInvalidoException("Elige al menos un día.");
        }
        EnumSet<DayOfWeek> dias = EnumSet.noneOf(DayOfWeek.class);
        nombres.forEach(n -> dias.add(dia(n)));
        return dias;
    }

    /** Los días como bits (lunes = 1, martes = 2, … domingo = 64), para guardarlos en una columna. */
    public static int aBits(Set<DayOfWeek> dias) {
        return dias.stream().mapToInt(d -> 1 << (d.getValue() - 1)).sum();
    }

    public static Set<DayOfWeek> deBits(int bits) {
        EnumSet<DayOfWeek> dias = EnumSet.noneOf(DayOfWeek.class);
        for (DayOfWeek d : DayOfWeek.values()) {
            if ((bits & (1 << (d.getValue() - 1))) != 0) {
                dias.add(d);
            }
        }
        return dias;
    }
}
