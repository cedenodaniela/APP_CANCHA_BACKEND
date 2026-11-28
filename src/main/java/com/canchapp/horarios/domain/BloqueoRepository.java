package com.canchapp.horarios.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Puerto de persistencia de los bloqueos (RF-05). */
public interface BloqueoRepository {

    /** Bloqueos desde esa fecha, ordenados por fecha y hora. */
    List<Bloqueo> findDesde(LocalDate desde);

    Optional<Bloqueo> findById(Long id);

    Bloqueo save(Bloqueo bloqueo);

    void delete(Long id);
}
