package com.canchapp.horarios.infrastructure.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Repositorio Spring Data de {@link TarifaEntity}. */
public interface TarifaJpa extends JpaRepository<TarifaEntity, Long> {

    List<TarifaEntity> findAllByOrderByInicioMinutosAsc();
}
