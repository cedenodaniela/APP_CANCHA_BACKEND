package com.canchapp.usuarios.application;

import com.canchapp.usuarios.domain.CodificadorContrasena;
import com.canchapp.usuarios.domain.EnlaceRecuperacionInvalidoException;
import com.canchapp.usuarios.domain.TokenRecuperacion;
import com.canchapp.usuarios.domain.TokenRecuperacionRepository;
import com.canchapp.usuarios.domain.Usuario;
import com.canchapp.usuarios.domain.UsuarioRepository;

import java.time.Clock;
import java.time.Instant;

/**
 * Segundo paso de RF-02: cambiar la contraseña con el token del enlace.
 * <p>
 * El enlace se marca como usado antes de cambiar la contraseña y de forma atómica, así sirve
 * una sola vez aunque se abra dos veces al mismo tiempo. La capa web ejecuta este caso de uso
 * en una transacción, para que ambos cambios se guarden juntos o ninguno.
 *
 * @see "RF-02 · Recuperación de contraseña"
 */
public class RestablecerContrasenaUseCase {

    private final UsuarioRepository usuarios;
    private final TokenRecuperacionRepository tokens;
    private final CodificadorContrasena codificador;
    private final Clock reloj;

    public RestablecerContrasenaUseCase(UsuarioRepository usuarios, TokenRecuperacionRepository tokens,
                                        CodificadorContrasena codificador, Clock reloj) {
        this.usuarios = usuarios;
        this.tokens = tokens;
        this.codificador = codificador;
        this.reloj = reloj;
    }

    /**
     * @throws com.canchapp.usuarios.domain.DatosInvalidosException si la contraseña no cumple las
     *         reglas del registro (RF-02: «la nueva contraseña cumple las mismas reglas»)
     * @throws EnlaceRecuperacionInvalidoException si el enlace no existe, ya se usó o ya venció
     */
    public void ejecutar(String token, String nuevaContrasena) {
        Usuario.validarContrasena(nuevaContrasena);
        if (token == null || token.isBlank()) {
            throw new EnlaceRecuperacionInvalidoException();
        }

        Instant ahora = reloj.instant();
        TokenRecuperacion enlace = tokens.findByHashToken(TokenRecuperacion.hash(token))
                .orElseThrow(EnlaceRecuperacionInvalidoException::new);
        enlace.verificarVigente(ahora);
        if (!tokens.marcarUsado(enlace.getId(), ahora)) {
            throw new EnlaceRecuperacionInvalidoException();
        }

        Usuario usuario = usuarios.findById(enlace.getUsuarioId())
                .orElseThrow(EnlaceRecuperacionInvalidoException::new);
        usuarios.save(usuario.cambiarContrasena(codificador.codificar(nuevaContrasena), ahora));
    }
}
