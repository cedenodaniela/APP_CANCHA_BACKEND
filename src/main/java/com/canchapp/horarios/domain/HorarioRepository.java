package com.canchapp.horarios.domain;

/** Puerto de persistencia del horario de atención (RF-05). La migración V5 siembra los 7 días. */
public interface HorarioRepository {

    HorarioSemanal obtener();

    HorarioSemanal guardar(HorarioSemanal horario);
}
