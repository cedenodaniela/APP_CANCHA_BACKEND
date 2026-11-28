package com.canchapp.horarios.domain;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Horario de atención del complejo, un {@link HorarioDia} por cada día de la semana (RF-05).
 * Aplica a todas las canchas: el DERCAS lo define para el complejo (RN-02b).
 */
public final class HorarioSemanal {

    private final Map<DayOfWeek, HorarioDia> dias;

    private HorarioSemanal(Map<DayOfWeek, HorarioDia> dias) {
        this.dias = dias;
    }

    /** @throws HorarioInvalidoException si falta algún día o hay uno repetido */
    public static HorarioSemanal de(List<HorarioDia> dias) {
        if (dias == null) {
            throw new HorarioInvalidoException("Indica el horario de los 7 días de la semana.");
        }
        EnumMap<DayOfWeek, HorarioDia> porDia = new EnumMap<>(DayOfWeek.class);
        for (HorarioDia d : dias) {
            if (porDia.put(d.dia(), d) != null) {
                throw new HorarioInvalidoException("El " + DiasSemana.nombre(d.dia()).toLowerCase() + " aparece dos veces.");
            }
        }
        if (porDia.size() != 7) {
            throw new HorarioInvalidoException("Indica el horario de los 7 días de la semana.");
        }
        return new HorarioSemanal(porDia);
    }

    public HorarioDia del(DayOfWeek dia) {
        return dias.get(dia);
    }

    /** De lunes a domingo. */
    public List<HorarioDia> dias() {
        return Arrays.stream(DayOfWeek.values()).map(dias::get).sorted(Comparator.comparing(HorarioDia::dia)).toList();
    }
}
