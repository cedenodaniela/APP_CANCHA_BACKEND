package com.canchapp.usuarios.application;

import com.canchapp.usuarios.domain.CodificadorContrasena;
import com.canchapp.usuarios.domain.DatosInvalidosException;
import com.canchapp.usuarios.domain.EnlaceRecuperacionInvalidoException;
import com.canchapp.usuarios.domain.EnviadorCorreoRecuperacion;
import com.canchapp.usuarios.domain.GeneradorToken;
import com.canchapp.usuarios.domain.Rol;
import com.canchapp.usuarios.domain.TokenRecuperacion;
import com.canchapp.usuarios.domain.TokenRecuperacionRepository;
import com.canchapp.usuarios.domain.Usuario;
import com.canchapp.usuarios.domain.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("Recuperación de contraseña (RF-02)")
class RecuperacionContrasenaUseCasesTest {

    private static final Instant AHORA = Instant.parse("2026-11-25T15:00:00Z");
    private static final String URL = "canchapp://restablecer-contrasena";

    private UsuarioRepository usuarios;
    private TokenRecuperacionRepository tokens;
    private EnviadorCorreoRecuperacion correo;
    private CodificadorContrasena codificador;
    private Clock reloj;

    @BeforeEach
    void preparar() {
        usuarios = mock(UsuarioRepository.class);
        tokens = mock(TokenRecuperacionRepository.class);
        correo = mock(EnviadorCorreoRecuperacion.class);
        codificador = mock(CodificadorContrasena.class);
        reloj = Clock.fixed(AHORA, ZoneId.of("America/Guayaquil"));
        when(tokens.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private static Usuario ana(boolean activa) {
        return Usuario.reconstituir(7L, "Ana", "Pérez", "ana@correo.com", null, "hash-viejo",
                Rol.CLIENTE, activa, AHORA, AHORA);
    }

    @Nested
    @DisplayName("Solicitar el enlace")
    class Solicitar {

        private SolicitarRecuperacionUseCase casoDeUso;

        @BeforeEach
        void preparar() {
            GeneradorToken generador = () -> "token-aleatorio";
            casoDeUso = new SolicitarRecuperacionUseCase(usuarios, tokens, generador, correo, reloj, URL);
        }

        @Test
        @DisplayName("dado un correo registrado, cuando pide recuperar, entonces guarda solo el hash y envía el enlace con el token")
        void enviaElEnlace() {
            when(usuarios.findByEmail("ana@correo.com")).thenReturn(Optional.of(ana(true)));

            casoDeUso.ejecutar(" Ana@Correo.com ");

            ArgumentCaptor<TokenRecuperacion> guardado = ArgumentCaptor.forClass(TokenRecuperacion.class);
            verify(tokens).anularPendientes(7L, AHORA);
            verify(tokens).save(guardado.capture());
            assertThat(guardado.getValue().getHashToken()).isEqualTo(TokenRecuperacion.hash("token-aleatorio"));
            assertThat(guardado.getValue().getVenceEn()).isEqualTo(AHORA.plus(TokenRecuperacion.VIGENCIA));
            verify(correo).enviar("ana@correo.com", "Ana", URL + "?token=token-aleatorio");
        }

        @Test
        @DisplayName("dado un correo que no existe, cuando pide recuperar, entonces termina igual y no envía nada")
        void correoInexistente() {
            when(usuarios.findByEmail("nadie@correo.com")).thenReturn(Optional.empty());

            casoDeUso.ejecutar("nadie@correo.com");

            verifyNoInteractions(tokens, correo);
        }

        @Test
        @DisplayName("dada una cuenta desactivada, cuando pide recuperar, entonces no envía nada")
        void cuentaDesactivada() {
            when(usuarios.findByEmail("ana@correo.com")).thenReturn(Optional.of(ana(false)));

            casoDeUso.ejecutar("ana@correo.com");

            verifyNoInteractions(tokens, correo);
        }

        @Test
        @DisplayName("dado un texto que no es un correo, cuando pide recuperar, entonces lo rechaza sin buscar cuentas")
        void formatoInvalido() {
            assertThatThrownBy(() -> casoDeUso.ejecutar("ana.correo.com"))
                    .isInstanceOf(DatosInvalidosException.class);
            verifyNoInteractions(usuarios, tokens, correo);
        }
    }

    @Nested
    @DisplayName("Restablecer la contraseña")
    class Restablecer {

        private RestablecerContrasenaUseCase casoDeUso;
        private TokenRecuperacion vigente;

        @BeforeEach
        void preparar() {
            casoDeUso = new RestablecerContrasenaUseCase(usuarios, tokens, codificador, reloj);
            vigente = TokenRecuperacion.reconstituir(3L, 7L, TokenRecuperacion.hash("token"),
                    AHORA.minusSeconds(600), AHORA.plusSeconds(1200), null);
            when(codificador.codificar("nueva-clave")).thenReturn("hash-nuevo");
            when(usuarios.save(any())).thenAnswer(inv -> inv.getArgument(0));
        }

        @Test
        @DisplayName("dado un enlace vigente, cuando crea la contraseña, entonces usa el enlace y guarda la contraseña codificada")
        void cambiaLaContrasena() {
            when(tokens.findByHashToken(TokenRecuperacion.hash("token"))).thenReturn(Optional.of(vigente));
            when(tokens.marcarUsado(3L, AHORA)).thenReturn(true);
            when(usuarios.findById(7L)).thenReturn(Optional.of(ana(true)));

            casoDeUso.ejecutar("token", "nueva-clave");

            ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
            verify(usuarios).save(guardado.capture());
            assertThat(guardado.getValue().getHashContrasena()).isEqualTo("hash-nuevo");
            assertThat(guardado.getValue().getId()).isEqualTo(7L);
        }

        @Test
        @DisplayName("dado un enlace que otra solicitud acaba de usar, cuando crea la contraseña, entonces lo rechaza")
        void unSoloUsoAunqueLleguenDos() {
            when(tokens.findByHashToken(anyString())).thenReturn(Optional.of(vigente));
            when(tokens.marcarUsado(3L, AHORA)).thenReturn(false);

            assertThatThrownBy(() -> casoDeUso.ejecutar("token", "nueva-clave"))
                    .isInstanceOf(EnlaceRecuperacionInvalidoException.class);
            verify(usuarios, never()).save(any());
        }

        @Test
        @DisplayName("dado un enlace vencido, cuando crea la contraseña, entonces lo rechaza sin usarlo")
        void enlaceVencido() {
            TokenRecuperacion vencido = TokenRecuperacion.reconstituir(3L, 7L, TokenRecuperacion.hash("token"),
                    AHORA.minusSeconds(3600), AHORA.minusSeconds(1800), null);
            when(tokens.findByHashToken(anyString())).thenReturn(Optional.of(vencido));

            assertThatThrownBy(() -> casoDeUso.ejecutar("token", "nueva-clave"))
                    .isInstanceOf(EnlaceRecuperacionInvalidoException.class);
            verify(tokens, never()).marcarUsado(any(), any());
            verify(usuarios, never()).save(any());
        }

        @Test
        @DisplayName("dado un token desconocido, cuando crea la contraseña, entonces lo rechaza")
        void tokenDesconocido() {
            when(tokens.findByHashToken(anyString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> casoDeUso.ejecutar("inventado", "nueva-clave"))
                    .isInstanceOf(EnlaceRecuperacionInvalidoException.class);
        }

        @Test
        @DisplayName("dada una contraseña corta, cuando crea la contraseña, entonces aplica la regla del registro y no gasta el enlace")
        void contrasenaCorta() {
            assertThatThrownBy(() -> casoDeUso.ejecutar("token", "1234567"))
                    .isInstanceOf(DatosInvalidosException.class);
            verifyNoInteractions(tokens);
        }
    }
}
