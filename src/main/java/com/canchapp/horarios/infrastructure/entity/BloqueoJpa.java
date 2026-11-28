package com.canchapp.horarios.infrastructure.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/** Repositorio Spring Data de {@link BloqueoEntity}. */
public interface BloqueoJpa extends JpaRepository<BloqueoEntity, Long> {

    List<BloqueoEntity> findByFechaGreaterThanEqualOrderByFechaAscInicioMinutosAsc(LocalDate desde);
}
