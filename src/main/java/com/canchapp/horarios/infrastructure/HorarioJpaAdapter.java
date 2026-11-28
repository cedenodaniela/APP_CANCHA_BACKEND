package com.canchapp.horarios.infrastructure;

import com.canchapp.horarios.domain.HoraDelDia;
import com.canchapp.horarios.domain.HorarioDia;
import com.canchapp.horarios.domain.HorarioRepository;
import com.canchapp.horarios.domain.HorarioSemanal;
import com.canchapp.horarios.infrastructure.entity.HorarioAtencionEntity;
import com.canchapp.horarios.infrastructure.entity.HorarioAtencionJpa;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;

/** Implementación JPA de {@link HorarioRepository} (RF-05). Las horas van en minutos desde las 00:00. */
@Repository
public class HorarioJpaAdapter implements HorarioRepository {

    private final HorarioAtencionJpa jpa;

    public HorarioJpaAdapter(HorarioAtencionJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public HorarioSemanal obtener() {
        return HorarioSemanal.de(jpa.findAll().stream().map(HorarioJpaAdapter::aDominio).toList());
    }

    @Override
    @Transactional
    public HorarioSemanal guardar(HorarioSemanal semana) {
        jpa.saveAll(semana.dias().stream().map(HorarioJpaAdapter::aEntidad).toList());
        return obtener();
    }

    static HorarioDia aDominio(HorarioAtencionEntity e) {
        DayOfWeek dia = DayOfWeek.of(e.getDiaSemana());
        return Boolean.TRUE.equals(e.getAbierto())
                ? new HorarioDia(dia, true, new HoraDelDia(e.getAperturaMinutos()), new HoraDelDia(e.getCierreMinutos()))
                : HorarioDia.cerrado(dia);
    }

    static HorarioAtencionEntity aEntidad(HorarioDia d) {
        HorarioAtencionEntity e = new HorarioAtencionEntity();
        e.setDiaSemana((short) d.dia().getValue());
        e.setAbierto(d.abierto());
        e.setAperturaMinutos(d.abierto() ? d.apertura().minutos() : null);
        e.setCierreMinutos(d.abierto() ? d.cierre().minutos() : null);
        return e;
    }
}
