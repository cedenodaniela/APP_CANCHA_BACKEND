package com.canchapp.canchas.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cancha · dominio (RF-04)")
class CanchaTest {

    private static final Instant AHORA = Instant.parse("2026-11-28T15:00:00Z");

    @Test
    @DisplayName("dada una cancha nueva, cuando se crea, entonces nace activa, sin foto y con los datos limpios")
    void nueva() {
        Cancha cancha = Cancha.nueva(" Cancha 3 ", "Sintético", 30, 50, AHORA);

        assertThat(cancha.getNombre()).isEqualTo("Cancha 3");
        assertThat(cancha.isActiva()).isTrue();
        assertThat(cancha.tieneFoto()).isFalse();
    }

    @Test
    @DisplayName("dada una medida de 0 metros, cuando se crea, entonces la rechaza")
    void medidaInvalida() {
        assertThatThrownBy(() -> Cancha.nueva("Cancha 3", "Sintético", 0, 50, AHORA))
                .isInstanceOf(CanchaInvalidaException.class)
                .hasMessage("El ancho de la cancha debe ser mayor que 0 metros.");
    }

    @Test
    @DisplayName("dado un nombre vacío, cuando se crea, entonces explica qué falta")
    void nombreVacio() {
        assertThatThrownBy(() -> Cancha.nueva("  ", "Sintético", 30, 50, AHORA))
                .isInstanceOf(CanchaInvalidaException.class)
                .hasMessage("Ingresa el nombre de la cancha.");
    }

    @Test
    @DisplayName("dada una cancha con foto, cuando se edita o se desactiva, entonces conserva la foto")
    void conservaLaFoto() {
        Cancha conFoto = Cancha.nueva("Cancha 1", "Sintético", 30, 50, AHORA).conFoto("a.webp", AHORA);

        Cancha editada = conFoto.editar("Cancha Uno", "Sintético", 30, 50, AHORA.plusSeconds(60)).desactivar(AHORA);

        assertThat(editada.getClaveFoto()).isEqualTo("a.webp");
        assertThat(editada.getNombre()).isEqualTo("Cancha Uno");
        assertThat(editada.isActiva()).isFalse();
    }

    @Test
    @DisplayName("dado 1 o varias reservas activas, cuando se avisa, entonces el mensaje dice cuántas")
    void mensajeReservas() {
        assertThat(new ReservasActivasException(1).getMessage()).startsWith("La cancha tiene 1 reserva activa.");
        assertThat(new ReservasActivasException(3).getMessage()).startsWith("La cancha tiene 3 reservas activas.");
    }
}
