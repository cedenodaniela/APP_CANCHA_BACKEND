package com.canchapp.horarios.application;

import com.canchapp.horarios.domain.DiasSemana;
import com.canchapp.horarios.domain.HoraDelDia;
import com.canchapp.horarios.domain.HorarioDia;
import com.canchapp.horarios.domain.HorarioRepository;
import com.canchapp.horarios.domain.HorarioSemanal;

import java.util.List;

/**
 * El administrador cambia el horario de apertura y cierre de cada día (RF-05). Se reemplaza la
 * semana completa: si un día no es válido, no se guarda ninguno.
 *
 * @see "RF-05: el administrador define el horario de apertura y cierre por día de la semana"
 */
public class ActualizarHorarioUseCase {

    /** Un día tal como llega del formulario. Las horas son «HH:mm». */
    public record DatosDia(String dia, Boolean abierto, String apertura, String cierre) {
    }

    private final HorarioRepository horarios;

    public ActualizarHorarioUseCase(HorarioRepository horarios) {
        this.horarios = horarios;
    }

    /** @throws com.canchapp.horarios.domain.HorarioInvalidoException si un día falta, se repite o no es válido */
    public HorarioSemanal ejecutar(List<DatosDia> dias) {
        if (dias == null) {
            return horarios.guardar(HorarioSemanal.de(null));
        }
        List<HorarioDia> semana = dias.stream().map(d -> {
            boolean abierto = Boolean.TRUE.equals(d.abierto());
            return new HorarioDia(DiasSemana.dia(d.dia()), abierto,
                    abierto ? HoraDelDia.de(d.apertura(), "la hora de apertura") : null,
                    abierto ? HoraDelDia.de(d.cierre(), "la hora de cierre") : null);
        }).toList();
        return horarios.guardar(HorarioSemanal.de(semana));
    }
}
