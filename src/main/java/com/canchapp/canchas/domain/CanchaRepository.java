package com.canchapp.canchas.domain;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia de {@link Cancha} (RF-04). */
public interface CanchaRepository {

    /** Todas, ordenadas por nombre. Las ve el administrador. */
    List<Cancha> findAll();

    /** Solo las activas, ordenadas por nombre. Las ve el cliente. */
    List<Cancha> findActivas();

    Optional<Cancha> findById(Long id);

    /** Si otra cancha (distinta de {@code excluirId}) ya usa ese nombre, sin distinguir mayúsculas. */
    boolean existeNombre(String nombre, Long excluirId);

    Cancha save(Cancha cancha);
}
