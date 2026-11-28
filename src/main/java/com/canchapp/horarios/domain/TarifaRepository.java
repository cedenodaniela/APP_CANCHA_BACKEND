package com.canchapp.horarios.domain;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia de las tarifas (RF-05). */
public interface TarifaRepository {

    /** Ordenadas por hora de inicio. */
    List<Tarifa> findAll();

    Optional<Tarifa> findById(Long id);

    Tarifa save(Tarifa tarifa);

    void delete(Long id);
}
