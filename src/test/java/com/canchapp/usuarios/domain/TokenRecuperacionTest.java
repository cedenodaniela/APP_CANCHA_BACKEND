package com.canchapp.usuarios.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TokenRecuperacion (RF-02)")
class TokenRecuperacionTest {

    private static final Instant AHORA = Instant.parse("2026-11-25T15:00:00Z");

    @Test
    @DisplayName("dado un enlace nuevo, cuando se emite, entonces guarda el hash y no el token, y vence a los 30 minutos")
    void guardaSoloElHash() {
        TokenRecuperacion token = TokenRecuperacion.emitir(7L, "token-en-claro", AHORA);

        assertThat(token.getHashToken()).hasSize(64).isNotEqualTo("token-en-claro")
                .isEqualTo(TokenRecuperacion.hash("token-en-claro"));
        assertThat(token.getVenceEn()).isEqualTo(AHORA.plusSeconds(30 * 60));
        assertThat(token.getUsadoEn()).isNull();
    }

    @Test
    @DisplayName("dado un enlace de hace 29 minutos, cuando se verifica, entonces todavía sirve")
    void vigenteAntesDeLos30Minutos() {
        TokenRecuperacion token = TokenRecuperacion.emitir(7L, "t", AHORA);

        assertThatCode(() -> token.verificarVigente(AHORA.plusSeconds(29 * 60))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("dado un enlace de hace 30 minutos, cuando se verifica, entonces ya venció")
    void vencidoALos30Minutos() {
        TokenRecuperacion token = TokenRecuperacion.emitir(7L, "t", AHORA);

        assertThatThrownBy(() -> token.verificarVigente(AHORA.plusSeconds(30 * 60)))
                .isInstanceOf(EnlaceRecuperacionInvalidoException.class);
    }

    @Test
    @DisplayName("dado un enlace ya usado, cuando se verifica, entonces no sirve otra vez")
    void unSoloUso() {
        TokenRecuperacion usado = TokenRecuperacion.reconstituir(1L, 7L, TokenRecuperacion.hash("t"), AHORA,
                AHORA.plusSeconds(1800), AHORA.plusSeconds(60));

        assertThatThrownBy(() -> usado.verificarVigente(AHORA.plusSeconds(120)))
                .isInstanceOf(EnlaceRecuperacionInvalidoException.class)
                .hasMessage("El enlace no es válido o ya venció. Solicita uno nuevo.");
    }

    @Test
    @DisplayName("dado un usuario, cuando cambia la contraseña, entonces conserva todo lo demás")
    void cambiarContrasenaConservaLaCuenta() {
        Usuario ana = Usuario.registrarCliente("Ana", "Pérez", "ana@correo.com", null, "hash-viejo", AHORA);

        Usuario cambiada = ana.cambiarContrasena("hash-nuevo", AHORA.plusSeconds(60));

        assertThat(cambiada.getHashContrasena()).isEqualTo("hash-nuevo");
        assertThat(cambiada.getEmail()).isEqualTo("ana@correo.com");
        assertThat(cambiada.getRol()).isEqualTo(Rol.CLIENTE);
        assertThat(cambiada.getFechaCreacion()).isEqualTo(AHORA);
        assertThat(cambiada.getFechaActualizacion()).isEqualTo(AHORA.plusSeconds(60));
    }
}
