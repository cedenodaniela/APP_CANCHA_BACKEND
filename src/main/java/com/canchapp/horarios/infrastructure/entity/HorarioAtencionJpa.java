package com.canchapp.horarios.infrastructure.entity;

import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio Spring Data de {@link HorarioAtencionEntity}. */
public interface HorarioAtencionJpa extends JpaRepository<HorarioAtencionEntity, Short> {
}
