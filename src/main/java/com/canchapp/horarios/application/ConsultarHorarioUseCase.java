package com.canchapp.horarios.application;

import com.canchapp.horarios.domain.HorarioRepository;
import com.canchapp.horarios.domain.HorarioSemanal;

/** Devuelve el horario de atención del complejo (RF-05). */
public class ConsultarHorarioUseCase {

    private final HorarioRepository horarios;

    public ConsultarHorarioUseCase(HorarioRepository horarios) {
        this.horarios = horarios;
    }

    public HorarioSemanal ejecutar() {
        return horarios.obtener();
    }
}
