package com.canchapp.horarios.infrastructure;

import com.canchapp.horarios.domain.Bloqueo;
import com.canchapp.horarios.domain.BloqueoRepository;
import com.canchapp.horarios.domain.HoraDelDia;
import com.canchapp.horarios.infrastructure.entity.BloqueoEntity;
import com.canchapp.horarios.infrastructure.entity.BloqueoJpa;
import com.canchapp.shared.infrastructure.ConfiguracionTiempo;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Implementación JPA de {@link BloqueoRepository} (RF-05). */
@Repository
public class BloqueoJpaAdapter implements BloqueoRepository {

    private final BloqueoJpa jpa;

    public BloqueoJpaAdapter(BloqueoJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Bloqueo> findDesde(LocalDate desde) {
        return jpa.findByFechaGreaterThanEqualOrderByFechaAscInicioMinutosAsc(desde).stream()
                .map(BloqueoJpaAdapter::aDominio).toList();
    }

    @Override
    public Optional<Bloqueo> findById(Long id) {
        return jpa.findById(id).map(BloqueoJpaAdapter::aDominio);
    }

    @Override
    public Bloqueo save(Bloqueo bloqueo) {
        return aDominio(jpa.save(aEntidad(bloqueo)));
    }

    @Override
    public void delete(Long id) {
        jpa.deleteById(id);
    }

    static Bloqueo aDominio(BloqueoEntity e) {
        return new Bloqueo(e.getId(), e.getCanchaId(), e.getFecha(), new HoraDelDia(e.getInicioMinutos()),
                new HoraDelDia(e.getFinMinutos()), e.getMotivo(),
                e.getFechaCreacion() == null ? null
                        : e.getFechaCreacion().atZone(ConfiguracionTiempo.ZONA_COMPLEJO).toInstant());
    }

    static BloqueoEntity aEntidad(Bloqueo b) {
        BloqueoEntity e = new BloqueoEntity();
        e.setId(b.id());
        e.setCanchaId(b.canchaId());
        e.setFecha(b.fecha());
        e.setInicioMinutos(b.inicio().minutos());
        e.setFinMinutos(b.fin().minutos());
        e.setMotivo(b.motivo());
        e.setFechaCreacion(b.creadoEn() == null ? null
                : LocalDateTime.ofInstant(b.creadoEn(), ConfiguracionTiempo.ZONA_COMPLEJO));
        return e;
    }
}
