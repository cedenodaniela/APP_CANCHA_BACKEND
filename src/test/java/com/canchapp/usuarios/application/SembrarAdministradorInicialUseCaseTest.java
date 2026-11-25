package com.canchapp.usuarios.application;

import com.canchapp.usuarios.application.SembrarAdministradorInicialUseCase.DatosAdministrador;
import com.canchapp.usuarios.domain.CodificadorContrasena;
import com.canchapp.usuarios.domain.CorreoYaRegistradoException;
import com.canchapp.usuarios.domain.DatosInvalidosException;
import com.canchapp.usuarios.domain.Rol;
import com.canchapp.usuarios.domain.Usuario;
import com.canchapp.usuarios.domain.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("SembrarAdministradorInicialUseCase (RF-18)")
class SembrarAdministradorInicialUseCaseTest {

    private UsuarioRepository usuarios;
    private SembrarAdministradorInicialUseCase casoDeUso;

    @BeforeEach
    void preparar() {
        usuarios = mock(UsuarioRepository.class);
        CodificadorContrasena codificador = mock(CodificadorContrasena.class);
        when(codificador.codificar("secreta123")).thenReturn("HASH");
        when(usuarios.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Clock reloj = Clock.fixed(Instant.parse("2026-11-24T15:00:00Z"), ZoneId.of("America/Guayaquil"));
        casoDeUso = new SembrarAdministradorInicialUseCase(usuarios, codificador, reloj);
    }

    private static DatosAdministrador datos(String email, String contrasena) {
        return new DatosAdministrador("Administrador", "Complejo", email, contrasena);
    }

    @Test
    @DisplayName("dado que no hay administradores, cuando arranca, entonces crea un ADMINISTRADOR activo con la contraseña codificada")
    void creaElPrimerAdministrador() {
        Optional<Usuario> creado = casoDeUso.ejecutar(datos(" Admin@CanchApp.local ", "secreta123"));

        assertThat(creado).hasValueSatisfying(admin -> {
            assertThat(admin.getRol()).isEqualTo(Rol.ADMINISTRADOR);
            assertThat(admin.getEmail()).isEqualTo("admin@canchapp.local");
            assertThat(admin.getHashContrasena()).isEqualTo("HASH");
            assertThat(admin.puedeIniciarSesion()).isTrue();
        });
    }

    @Test
    @DisplayName("dado que ya hay un administrador, cuando arranca otra vez, entonces no crea ni cambia nada")
    void esIdempotente() {
        when(usuarios.existsByRol(Rol.ADMINISTRADOR)).thenReturn(true);

        assertThat(casoDeUso.ejecutar(datos("admin@canchapp.local", "secreta123"))).isEmpty();
        verify(usuarios, never()).save(any());
    }

    @Test
    @DisplayName("dado un correo que ya es de un cliente, cuando arranca, entonces no lo convierte en administrador")
    void noConvierteUnCliente() {
        when(usuarios.existsByEmail("ana@correo.com")).thenReturn(true);

        assertThatThrownBy(() -> casoDeUso.ejecutar(datos("ana@correo.com", "secreta123")))
                .isInstanceOf(CorreoYaRegistradoException.class);
        verify(usuarios, never()).save(any());
    }

    @Test
    @DisplayName("dada una contraseña corta, cuando arranca, entonces no siembra la cuenta")
    void contrasenaCorta() {
        assertThatThrownBy(() -> casoDeUso.ejecutar(datos("admin@canchapp.local", "1234567")))
                .isInstanceOf(DatosInvalidosException.class);
        verify(usuarios, never()).save(any());
    }
}
