package com.canchapp.usuarios.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Usuario (RF-01)")
class UsuarioTest {

    private static final Instant AHORA = Instant.parse("2026-11-24T15:00:00Z");

    @Test
    @DisplayName("dado un registro público, cuando se crea la cuenta, entonces es CLIENTE, activa y con correo normalizado")
    void registroPublicoCreaCliente() {
        Usuario u = Usuario.registrarCliente(" Ana ", "Pérez", " Ana@Correo.COM ", " ", "hash", AHORA);

        assertThat(u.getRol()).isEqualTo(Rol.CLIENTE);
        assertThat(u.isActivo()).isTrue();
        assertThat(u.getEmail()).isEqualTo("ana@correo.com");
        assertThat(u.getNombre()).isEqualTo("Ana");
        assertThat(u.getTelefono()).isNull();
        assertThat(u.getHashContrasena()).isEqualTo("hash");
        assertThat(u.puedeIniciarSesion()).isTrue();
    }

    @Test
    @DisplayName("dada una contraseña de 7 caracteres, cuando se valida, entonces se rechaza")
    void contrasenaCortaSeRechaza() {
        assertThatThrownBy(() -> Usuario.validarContrasena("1234567"))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessageContaining("8 caracteres");
    }

    @Test
    @DisplayName("dada una contraseña de 8 caracteres, cuando se valida, entonces se acepta")
    void contrasenaDeOchoSeAcepta() {
        Usuario.validarContrasena("12345678");
    }

    @Test
    @DisplayName("dado un correo sin formato válido, cuando se normaliza, entonces se rechaza")
    void correoInvalidoSeRechaza() {
        assertThatThrownBy(() -> Usuario.normalizarCorreo("no-es-correo"))
                .isInstanceOf(DatosInvalidosException.class);
    }

    @Test
    @DisplayName("dado un nombre vacío, cuando se registra, entonces se rechaza")
    void nombreObligatorio() {
        assertThatThrownBy(() -> Usuario.registrarCliente(" ", "Pérez", "ana@correo.com", null, "hash", AHORA))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessageContaining("nombre");
    }

    @Test
    @DisplayName("dada una cuenta inactiva, cuando se consulta, entonces no puede iniciar sesión")
    void cuentaInactivaNoInicia() {
        Usuario u = Usuario.reconstituir(1L, "Ana", "Pérez", "ana@correo.com", null, "hash",
                Rol.CLIENTE, false, AHORA, AHORA);

        assertThat(u.puedeIniciarSesion()).isFalse();
    }
}
