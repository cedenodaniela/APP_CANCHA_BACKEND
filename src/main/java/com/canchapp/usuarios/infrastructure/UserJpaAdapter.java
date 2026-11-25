package com.canchapp.usuarios.infrastructure;

import com.canchapp.usuarios.domain.CorreoYaRegistradoException;
import com.canchapp.usuarios.domain.Rol;
import com.canchapp.usuarios.domain.Usuario;
import com.canchapp.usuarios.domain.UsuarioRepository;
import com.canchapp.usuarios.infrastructure.entity.UserEntity;
import com.canchapp.usuarios.infrastructure.entity.UserEntityRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

/**
 * Implementación JPA del puerto {@link UsuarioRepository}.
 * <p>
 * Solo mapea entre {@link UserEntity} y {@link Usuario}. <b>No</b> codifica la contraseña:
 * el dominio ya entrega el hash. Si la codificara aquí, cada actualización de un usuario
 * volvería a codificar su hash y la cuenta quedaría sin acceso.
 */
@Repository
public class UserJpaAdapter implements UsuarioRepository {

    /** Las columnas TIMESTAMP se guardan en hora local de Ecuador (AGENTS.md §6). */
    private static final ZoneId ZONA = ZoneId.of("America/Guayaquil");

    private final UserEntityRepository jpa;

    public UserJpaAdapter(UserEntityRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return jpa.findByEmail(email).map(UserJpaAdapter::aDominio);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmail(email);
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return jpa.findById(id).map(UserJpaAdapter::aDominio);
    }

    /**
     * @throws CorreoYaRegistradoException si otra solicitud registró el mismo correo justo antes
     *         (la restricción {@code uq_usuario_email} lo impide en la base de datos)
     */
    @Override
    public Usuario save(Usuario usuario) {
        try {
            return aDominio(jpa.saveAndFlush(aEntidad(usuario)));
        } catch (DataIntegrityViolationException e) {
            throw new CorreoYaRegistradoException();
        }
    }

    static Usuario aDominio(UserEntity e) {
        return Usuario.reconstituir(e.getId(), e.getNombre(), e.getApellido(), e.getEmail(), e.getTelefono(),
                e.getPassword(), Rol.valueOf(e.getRol()), e.isActivo(),
                aInstante(e.getFechaCreacion()), aInstante(e.getFechaActualizacion()));
    }

    static UserEntity aEntidad(Usuario u) {
        UserEntity e = new UserEntity();
        e.setId(u.getId());
        e.setNombre(u.getNombre());
        e.setApellido(u.getApellido());
        e.setEmail(u.getEmail());
        e.setPassword(u.getHashContrasena());
        e.setRol(u.getRol().name());
        e.setTelefono(u.getTelefono());
        e.setFechaCreacion(aFechaLocal(u.getFechaCreacion()));
        e.setFechaActualizacion(aFechaLocal(u.getFechaActualizacion()));
        e.setActivo(u.isActivo());
        return e;
    }

    private static Instant aInstante(LocalDateTime fecha) {
        return fecha == null ? null : fecha.atZone(ZONA).toInstant();
    }

    private static LocalDateTime aFechaLocal(Instant instante) {
        return instante == null ? null : LocalDateTime.ofInstant(instante, ZONA);
    }
}
