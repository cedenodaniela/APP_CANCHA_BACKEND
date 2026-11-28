package com.canchapp.horarios.infrastructure;

import com.canchapp.horarios.domain.DiasSemana;
import com.canchapp.horarios.domain.HoraDelDia;
import com.canchapp.horarios.domain.Tarifa;
import com.canchapp.horarios.domain.TarifaRepository;
import com.canchapp.horarios.infrastructure.entity.TarifaEntity;
import com.canchapp.horarios.infrastructure.entity.TarifaJpa;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Implementación JPA de {@link TarifaRepository} (RF-05). Los días van como bits (lunes = 1 … domingo = 64). */
@Repository
public class TarifaJpaAdapter implements TarifaRepository {

    private final TarifaJpa jpa;

    public TarifaJpaAdapter(TarifaJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Tarifa> findAll() {
        return jpa.findAllByOrderByInicioMinutosAsc().stream().map(TarifaJpaAdapter::aDominio).toList();
    }

    @Override
    public Optional<Tarifa> findById(Long id) {
        return jpa.findById(id).map(TarifaJpaAdapter::aDominio);
    }

    @Override
    public Tarifa save(Tarifa tarifa) {
        return aDominio(jpa.save(aEntidad(tarifa)));
    }

    @Override
    public void delete(Long id) {
        jpa.deleteById(id);
    }

    static Tarifa aDominio(TarifaEntity e) {
        return Tarifa.de(e.getId(), DiasSemana.deBits(e.getDiasSemana()), new HoraDelDia(e.getInicioMinutos()),
                new HoraDelDia(e.getFinMinutos()), e.getPrecioHora());
    }

    static TarifaEntity aEntidad(Tarifa t) {
        TarifaEntity e = new TarifaEntity();
        e.setId(t.getId());
        e.setDiasSemana((short) DiasSemana.aBits(t.getDias()));
        e.setInicioMinutos(t.getInicio().minutos());
        e.setFinMinutos(t.getFin().minutos());
        e.setPrecioHora(t.getPrecioHora());
        return e;
    }
}
