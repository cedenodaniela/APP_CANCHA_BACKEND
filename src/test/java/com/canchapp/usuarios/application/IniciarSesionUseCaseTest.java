package com.canchapp.usuarios.application;

import com.canchapp.usuarios.application.IniciarSesionUseCase.Sesion;
import com.canchapp.usuarios.domain.CodificadorContrasena;
import com.canchapp.usuarios.domain.CredencialesInvalidasException;
import com.canchapp.usuarios.domain.EmisorToken;
import com.canchapp.usuarios.domain.EmisorToken.TokenEmitido;
import com.canchapp.usuarios.domain.Rol;
import com.canchapp.usuarios.domain.Usuario;
import com.canchapp.usuarios.domain.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("IniciarSesionUseCase (RF-01)")
class IniciarSesionUseCaseTest {

    private static final Instant AHORA = Instant.parse("2026-11-24T15:00:00Z");
    private static final TokenEmitido TOKEN = new TokenEmitido("jwt", AHORA.plusSeconds(86_400));

    private UsuarioRepository usuarios;
    private CodificadorContrasena codificador;
    private EmisorToken emisor;
    private IniciarSesionUseCase casoDeUso;

    @BeforeEach
    void preparar() {
        usuarios = mock(UsuarioRepository.class);
        codificador = mock(CodificadorContrasena.class);
        emisor = mock(EmisorToken.class);
        when(emisor.emitir(any())).thenReturn(TOKEN);
        when(codificador.coincide("secreta123", "HASH")).thenReturn(true);
        casoDeUso = new IniciarSesionUseCase(usuarios, codificador, emisor);
    }

    private static Usuario usuario(boolean activo) {
        return Usuario.reconstituir(7L, "Ana", "Pérez", "ana@correo.com", null, "HASH", Rol.CLIENTE, activo,
                AHORA, AHORA);
    }

    @Test
    @DisplayName("dadas credenciales válidas, cuando inicia sesión, entonces recibe el token y su usuario")
    void credencialesValidas() {
        when(usuarios.findByEmail("ana@correo.com")).thenReturn(Optional.of(usuario(true)));

        Sesion sesion = casoDeUso.ejecutar(" Ana@Correo.com ", "secreta123");

        assertThat(sesion.token()).isEqualTo(TOKEN);
        assertThat(sesion.usuario().getId()).isEqualTo(7L);
    }

    @ParameterizedTest(name = "[{index}] correo={0}, contraseña={1}, activo={2}")
    @CsvSource({
            "ana@correo.com,   incorrecta, true",   // contraseña errónea
            "otro@correo.com,  secreta123, true",   // correo inexistente
            "ana@correo.com,   secreta123, false",  // cuenta inactiva
            "no-es-correo,     secreta123, true",   // correo con formato inválido
    })
    @DisplayName("dado cualquier dato incorrecto, cuando inicia sesión, entonces el mensaje es el mismo y no revela cuál falló")
    void mismoMensajeAnteCualquierFallo(String correo, String contrasena, boolean activo) {
        when(usuarios.findByEmail("ana@correo.com")).thenReturn(Optional.of(usuario(activo)));

        assertThatThrownBy(() -> casoDeUso.ejecutar(correo, contrasena))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Correo o contraseña incorrectos");
        verify(emisor, never()).emitir(any());
    }
}
