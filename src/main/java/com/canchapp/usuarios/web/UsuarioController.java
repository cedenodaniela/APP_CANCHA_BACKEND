package com.canchapp.usuarios.web;

import com.canchapp.usuarios.application.RegistrarUsuarioUseCase;
import com.canchapp.usuarios.application.RegistrarUsuarioUseCase.DatosRegistro;
import com.canchapp.usuarios.domain.Usuario;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Registro público de cuentas de cliente (RF-01).
 * <p>
 * Los datos llegan en el cuerpo JSON, nunca en la URL: una contraseña en la URL queda
 * guardada en los registros del servidor y del navegador.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    /** Cuerpo de {@code POST /api/v1/usuarios}. */
    public record RegistroSolicitud(String nombre, String apellido, String email, String telefono, String password) {
    }

    /** Datos públicos de la cuenta. Nunca incluye la contraseña ni su hash. */
    public record UsuarioRespuesta(Long id, String nombre, String apellido, String email, String telefono, String rol) {

        static UsuarioRespuesta de(Usuario u) {
            return new UsuarioRespuesta(u.getId(), u.getNombre(), u.getApellido(), u.getEmail(), u.getTelefono(),
                    u.getRol().name());
        }
    }

    private final RegistrarUsuarioUseCase registrar;

    public UsuarioController(RegistrarUsuarioUseCase registrar) {
        this.registrar = registrar;
    }

    /**
     * Crea una cuenta {@code CLIENTE}.
     *
     * @return 201 con la cuenta creada; 400 si faltan datos o la contraseña es corta;
     *         409 si el correo ya está registrado
     */
    @PostMapping
    public ResponseEntity<UsuarioRespuesta> registrar(@RequestBody RegistroSolicitud solicitud) {
        Usuario creado = registrar.ejecutar(new DatosRegistro(solicitud.nombre(), solicitud.apellido(),
                solicitud.email(), solicitud.telefono(), solicitud.password()));
        return ResponseEntity.created(URI.create("/api/v1/usuarios/" + creado.getId()))
                .body(UsuarioRespuesta.de(creado));
    }
}
