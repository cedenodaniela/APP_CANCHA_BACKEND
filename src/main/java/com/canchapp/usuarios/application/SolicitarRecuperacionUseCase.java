package com.canchapp.usuarios.application;

import com.canchapp.usuarios.domain.EnviadorCorreoRecuperacion;
import com.canchapp.usuarios.domain.GeneradorToken;
import com.canchapp.usuarios.domain.TokenRecuperacion;
import com.canchapp.usuarios.domain.TokenRecuperacionRepository;
import com.canchapp.usuarios.domain.Usuario;
import com.canchapp.usuarios.domain.UsuarioRepository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;

/**
 * Primer paso de RF-02: enviar al correo registrado un enlace para restablecer la contraseña.
 * <p>
 * No devuelve nada y no lanza nada distinto exista o no la cuenta, para que la respuesta sea
 * siempre la misma y no revele qué correos están registrados.
 *
 * @see "RF-02 · Recuperación de contraseña"
 */
public class SolicitarRecuperacionUseCase {

    private final UsuarioRepository usuarios;
    private final TokenRecuperacionRepository tokens;
    private final GeneradorToken generador;
    private final EnviadorCorreoRecuperacion correo;
    private final Clock reloj;
    private final String urlEnlace;

    /**
     * @param urlEnlace dirección que abre la app en la pantalla de nueva contraseña; el token
     *                  se agrega como parámetro {@code token}
     */
    public SolicitarRecuperacionUseCase(UsuarioRepository usuarios, TokenRecuperacionRepository tokens,
                                        GeneradorToken generador, EnviadorCorreoRecuperacion correo,
                                        Clock reloj, String urlEnlace) {
        this.usuarios = usuarios;
        this.tokens = tokens;
        this.generador = generador;
        this.correo = correo;
        this.reloj = reloj;
        this.urlEnlace = urlEnlace;
    }

    /**
     * @throws com.canchapp.usuarios.domain.DatosInvalidosException solo si el texto no tiene
     *         formato de correo, lo que no dice nada sobre las cuentas registradas
     */
    public void ejecutar(String email) {
        String normalizado = Usuario.normalizarCorreo(email);
        usuarios.findByEmail(normalizado)
                .filter(Usuario::isActivo)
                .ifPresent(this::enviarEnlace);
    }

    private void enviarEnlace(Usuario usuario) {
        Instant ahora = reloj.instant();
        tokens.anularPendientes(usuario.getId(), ahora);
        String token = generador.generar();
        tokens.save(TokenRecuperacion.emitir(usuario.getId(), token, ahora));
        correo.enviar(usuario.getEmail(), usuario.getNombre(), enlace(token));
    }

    private String enlace(String token) {
        String separador = urlEnlace.contains("?") ? "&" : "?";
        return urlEnlace + separador + "token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }
}
