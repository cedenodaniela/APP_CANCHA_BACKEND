package com.canchapp.usuarios.infrastructure.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio Spring Data de {@link UserEntity}. Las consultas se derivan del nombre del método.
 */
public interface UserEntityRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
