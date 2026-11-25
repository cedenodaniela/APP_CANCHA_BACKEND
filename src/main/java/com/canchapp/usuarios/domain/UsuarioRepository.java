package com.canchapp.usuarios.domain;

import java.util.Optional;

/**
 * Puerto de persistencia de {@link Usuario}. La implementación JPA vive en infraestructura.
 * <p>
 * Guarda el usuario tal como llega: <b>no</b> codifica contraseñas (ver {@link CodificadorContrasena}).
 */
public interface UsuarioRepository {

    /** Busca por correo ya normalizado (ver {@link Usuario#normalizarCorreo(String)}). */
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    /** Indica si ya existe al menos una cuenta con el rol dado (se usa para sembrar el primer administrador). */
    boolean existsByRol(Rol rol);

    Optional<Usuario> findById(Long id);

    /** Inserta o actualiza y devuelve el usuario con su id. */
    Usuario save(Usuario usuario);
  
}
