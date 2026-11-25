package com.canchapp.usuarios.application;

import com.canchapp.usuarios.application.RegistrarUsuarioUseCase.DatosRegistro;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("RegistrarUsuarioUseCase (RF-01)")
class RegistrarUsuarioUseCaseTest {

    private UsuarioRepository usuarios;
    private CodificadorContrasena codificador;
    private RegistrarUsuarioUseCase casoDeUso;

    @BeforeEach
    void preparar() {
        usuarios = mock(UsuarioRepository.class);
        codificador = mock(CodificadorContrasena.class);
        when(codificador.codificar("secreta123")).thenReturn("HASH");
        when(usuarios.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Clock reloj = Clock.fixed(Instant.parse("2026-11-24T15:00:00Z"), ZoneId.of("America/Guayaquil"));
        casoDeUso = new RegistrarUsuarioUseCase(usuarios, codificador, reloj);
    }

    private DatosRegistro datos(String email, String contrasena) {
        return new DatosRegistro("Ana", "Pérez", email, "0999999999", contrasena);
    }

    @Test
    @DisplayName("dados datos válidos, cuando se registra, entonces guarda un CLIENTE con la contraseña codificada una sola vez")
    void registraCliente() {
        Usuario creado = casoDeUso.ejecutar(datos("Ana@Correo.com", "secreta123"));

        assertThat(creado.getRol()).isEqualTo(Rol.CLIENTE);
        assertThat(creado.getEmail()).isEqualTo("ana@correo.com");
        assertThat(creado.getHashContrasena()).isEqualTo("HASH");
        verify(codificador, times(1)).codificar("secreta123");
    }

    @Test
    @DisplayName("dado un correo ya registrado, cuando se registra, entonces lanza CORREO_YA_REGISTRADO y no guarda")
    void correoDuplicado() {
        when(usuarios.existsByEmail("ana@correo.com")).thenReturn(true);

        assertThatThrownBy(() -> casoDeUso.ejecutar(datos("ana@correo.com", "secreta123")))
                .isInstanceOf(CorreoYaRegistradoException.class);
        verify(usuarios, never()).save(any());
    }

    @Test
    @DisplayName("dada una contraseña corta, cuando se registra, entonces se rechaza sin codificarla")
    void contrasenaCorta() {
        assertThatThrownBy(() -> casoDeUso.ejecutar(datos("ana@correo.com", "corta")))
                .isInstanceOf(DatosInvalidosException.class);
        verify(codificador, never()).codificar(any());
        verify(usuarios, never()).save(any());
    }
}
