package com.canchapp.usuarios.infrastructure.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

/** Repositorio Spring Data de {@link RecuperacionContrasenaEntity}. */
public interface RecuperacionContrasenaEntityRepository extends JpaRepository<RecuperacionContrasenaEntity, Long> {

    Optional<RecuperacionContrasenaEntity> findByHashToken(String hashToken);

    /** Marca el enlace como usado solo si nadie lo usó antes; devuelve las filas cambiadas (0 o 1). */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update RecuperacionContrasenaEntity r set r.fechaUso = :ahora where r.id = :id and r.fechaUso is null")
    int marcarUsado(@Param("id") Long id, @Param("ahora") LocalDateTime ahora);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update RecuperacionContrasenaEntity r set r.fechaUso = :ahora "
            + "where r.usuarioId = :usuarioId and r.fechaUso is null")
    int anularPendientes(@Param("usuarioId") Long usuarioId, @Param("ahora") LocalDateTime ahora);
}
