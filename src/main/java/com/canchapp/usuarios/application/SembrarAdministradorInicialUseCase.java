package com.canchapp.usuarios.application;

import com.canchapp.usuarios.domain.CodificadorContrasena;
import com.canchapp.usuarios.domain.CorreoYaRegistradoException;
import com.canchapp.usuarios.domain.Rol;
import com.canchapp.usuarios.domain.Usuario;
import com.canchapp.usuarios.domain.UsuarioRepository;

import java.time.Clock;
import java.util.Optional;

/**
 * Crea la primera cuenta de administrador al instalar el sistema.
 * <p>
 * El registro público solo crea clientes (RF-01) y las demás cuentas de administrador se
 * crean desde el panel (RF-22), que a su vez exige un administrador con sesión. Sin esta
 * siembra nadie podría entrar al panel (RF-18).
 * <p>
 * Es idempotente: si ya existe <b>cualquier</b> administrador no hace nada, así que se puede
 * ejecutar en cada arranque sin duplicar cuentas ni cambiar contraseñas.
 *
 * @see "RF-18 · Acceso al panel web"
 */
public class SembrarAdministradorInicialUseCase {

    /** Datos de la cuenta, que llegan de variables de entorno, nunca de Git. */
    public record DatosAdministrador(String nombre, String apellido, String email, String contrasena) {
    }

    private final UsuarioRepository usuarios;
    private final CodificadorContrasena codificador;
    private final Clock reloj;

    public SembrarAdministradorInicialUseCase(UsuarioRepository usuarios, CodificadorContrasena codificador,
                                              Clock reloj) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.reloj = reloj;
    }

    /**
     * @return la cuenta creada, o vacío si ya había un administrador
     * @throws com.canchapp.usuarios.domain.DatosInvalidosException si faltan datos o la contraseña es corta
     * @throws CorreoYaRegistradoException si el correo ya pertenece a una cuenta de cliente
     */
    public Optional<Usuario> ejecutar(DatosAdministrador datos) {
        if (usuarios.existsByRol(Rol.ADMINISTRADOR)) {
            return Optional.empty();
        }

        String email = Usuario.normalizarCorreo(datos.email());
        Usuario.validarContrasena(datos.contrasena());
        if (usuarios.existsByEmail(email)) {
            throw new CorreoYaRegistradoException();
        }

        Usuario administrador = Usuario.crearAdministradorInicial(datos.nombre(), datos.apellido(), email,
                codificador.codificar(datos.contrasena()), reloj.instant());
        return Optional.of(usuarios.save(administrador));
    }
}
