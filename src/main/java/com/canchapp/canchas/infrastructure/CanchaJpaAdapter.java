package com.canchapp.canchas.infrastructure;

import com.canchapp.canchas.domain.Cancha;
import com.canchapp.canchas.domain.CanchaRepository;
import com.canchapp.canchas.domain.NombreCanchaRepetidoException;
import com.canchapp.canchas.infrastructure.entity.CanchaEntity;
import com.canchapp.canchas.infrastructure.entity.CanchaEntityRepository;
import com.canchapp.shared.infrastructure.ConfiguracionTiempo;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Implementación JPA del puerto {@link CanchaRepository} (RF-04). Fechas en hora local de Ecuador. */
@Repository
public class CanchaJpaAdapter implements CanchaRepository {

    private final CanchaEntityRepository jpa;

    public CanchaJpaAdapter(CanchaEntityRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Cancha> findAll() {
        return jpa.findAllByOrderByNombreAsc().stream().map(CanchaJpaAdapter::aDominio).toList();
    }

    @Override
    public List<Cancha> findActivas() {
        return jpa.findByActivaTrueOrderByNombreAsc().stream().map(CanchaJpaAdapter::aDominio).toList();
    }

    @Override
    public Optional<Cancha> findById(Long id) {
        return jpa.findById(id).map(CanchaJpaAdapter::aDominio);
    }

    @Override
    public boolean existeNombre(String nombre, Long excluirId) {
        return jpa.existeNombre(nombre, excluirId);
    }

    /**
     * @throws NombreCanchaRepetidoException si otra solicitud guardó el mismo nombre justo antes
     *         (lo impide el índice único {@code uq_cancha_nombre})
     */
    @Override
    public Cancha save(Cancha cancha) {
        try {
            return aDominio(jpa.saveAndFlush(aEntidad(cancha)));
        } catch (DataIntegrityViolationException e) {
            throw new NombreCanchaRepetidoException();
        }
    }

    static Cancha aDominio(CanchaEntity e) {
        return Cancha.reconstituir(e.getId(), e.getNombre(), e.getTipoCesped(), e.getAnchoMetros(), e.getLargoMetros(),
                e.getClaveFoto(), e.getActiva(), aInstante(e.getFechaCreacion()), aInstante(e.getFechaActualizacion()));
    }

    static CanchaEntity aEntidad(Cancha c) {
        CanchaEntity e = new CanchaEntity();
        e.setId(c.getId());
        e.setNombre(c.getNombre());
        e.setTipoCesped(c.getTipoCesped());
        e.setAnchoMetros(c.getAnchoMetros());
        e.setLargoMetros(c.getLargoMetros());
        e.setClaveFoto(c.getClaveFoto());
        e.setActiva(c.isActiva());
        e.setFechaCreacion(aFechaLocal(c.getCreadaEn()));
        e.setFechaActualizacion(aFechaLocal(c.getActualizadaEn()));
        return e;
    }

    private static Instant aInstante(LocalDateTime fecha) {
        return fecha == null ? null : fecha.atZone(ConfiguracionTiempo.ZONA_COMPLEJO).toInstant();
    }

    private static LocalDateTime aFechaLocal(Instant instante) {
        return instante == null ? null : LocalDateTime.ofInstant(instante, ConfiguracionTiempo.ZONA_COMPLEJO);
    }
}
