package com.canchapp.canchas.infrastructure.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Repositorio Spring Data de {@link CanchaEntity}. */
public interface CanchaEntityRepository extends JpaRepository<CanchaEntity, Long> {

    List<CanchaEntity> findAllByOrderByNombreAsc();

    List<CanchaEntity> findByActivaTrueOrderByNombreAsc();

    /** Coincide con el índice único {@code uq_cancha_nombre} sobre {@code LOWER(nombre)}. */
    @Query("select count(c) > 0 from CanchaEntity c "
            + "where lower(c.nombre) = lower(:nombre) and (:excluirId is null or c.id <> :excluirId)")
    boolean existeNombre(@Param("nombre") String nombre, @Param("excluirId") Long excluirId);
}
