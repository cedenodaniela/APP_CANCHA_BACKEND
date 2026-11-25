package com.canchapp.usuarios.application;

import com.canchapp.usuarios.domain.CodificadorContrasena;
import com.canchapp.usuarios.domain.CorreoYaRegistradoException;
import com.canchapp.usuarios.domain.Usuario;
import com.canchapp.usuarios.domain.UsuarioRepository;

import java.time.Clock;

/**
 * Registro público de una cuenta (RF-01). Siempre crea un {@code CLIENTE}.
 * <p>
 * Valida la contraseña, la codifica <b>una sola vez</b> con {@link CodificadorContrasena}
 * y recién entonces guarda. Depende solo del dominio (AGENTS.md §4); el bean se declara
 * en {@code usuarios.infrastructure.ConfiguracionUsuarios}.
 *
 * @see "RF-01 · Registro e inicio de sesión"
 */
public class RegistrarUsuarioUseCase {

    /** Datos que llegan del formulario de registro. */
    public record DatosRegistro(String nombre, String apellido, String email, String telefono, String contrasena) {
    }

    private final UsuarioRepository usuarios;
    private final CodificadorContrasena codificador;
    private final Clock reloj;

    public RegistrarUsuarioUseCase(UsuarioRepository usuarios, CodificadorContrasena codificador, Clock reloj) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.reloj = reloj;
    }

    /**
     * @throws com.canchapp.usuarios.domain.DatosInvalidosException si faltan datos o la contraseña es corta
     * @throws CorreoYaRegistradoException si ya existe una cuenta con ese correo
     */
    public Usuario ejecutar(DatosRegistro datos) {
        String email = Usuario.normalizarCorreo(datos.email());
        Usuario.validarContrasena(datos.contrasena());

        if (usuarios.existsByEmail(email)) {
            throw new CorreoYaRegistradoException();
        }

        Usuario nuevo = Usuario.registrarCliente(datos.nombre(), datos.apellido(), email, datos.telefono(),
                codificador.codificar(datos.contrasena()), reloj.instant());
        return usuarios.save(nuevo);
    }
}
