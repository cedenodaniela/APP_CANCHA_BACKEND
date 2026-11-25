package com.canchapp.usuarios.application;

import com.canchapp.usuarios.domain.CodificadorContrasena;
import com.canchapp.usuarios.domain.CredencialesInvalidasException;
import com.canchapp.usuarios.domain.DatosInvalidosException;
import com.canchapp.usuarios.domain.EmisorToken;
import com.canchapp.usuarios.domain.EmisorToken.TokenEmitido;
import com.canchapp.usuarios.domain.Usuario;
import com.canchapp.usuarios.domain.UsuarioRepository;

/**
 * Inicio de sesión con correo y contraseña (RF-01).
 * <p>
 * Ante cualquier fallo (correo inexistente, contraseña errónea, cuenta inactiva o sin
 * contraseña) lanza la misma {@link CredencialesInvalidasException}, para no revelar
 * cuál de los datos falló ni si la cuenta existe.
 *
 * @see "RF-01: «Correo o contraseña incorrectos», sin indicar cuál falló"
 * @see "RNF-02: el token vence a las 24 h"
 */
public class IniciarSesionUseCase {

    /** Token emitido y usuario autenticado. */
    public record Sesion(TokenEmitido token, Usuario usuario) {
    }

    private final UsuarioRepository usuarios;
    private final CodificadorContrasena codificador;
    private final EmisorToken emisor;

    public IniciarSesionUseCase(UsuarioRepository usuarios, CodificadorContrasena codificador, EmisorToken emisor) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.emisor = emisor;
    }

    public Sesion ejecutar(String email, String contrasena) {
        if (contrasena == null || contrasena.isEmpty()) {
            throw new CredencialesInvalidasException();
        }
        String correo;
        try {
            correo = Usuario.normalizarCorreo(email);
        } catch (DatosInvalidosException formatoInvalido) {
            throw new CredencialesInvalidasException();
        }

        Usuario usuario = usuarios.findByEmail(correo)
                .filter(Usuario::puedeIniciarSesion)
                .filter(u -> codificador.coincide(contrasena, u.getHashContrasena()))
                .orElseThrow(CredencialesInvalidasException::new);

        return new Sesion(emisor.emitir(usuario), usuario);
    }
}
