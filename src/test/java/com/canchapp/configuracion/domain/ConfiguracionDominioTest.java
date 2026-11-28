package com.canchapp.configuracion.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Configuración del complejo · dominio (RF-03)")
class ConfiguracionDominioTest {

    @Test
    @DisplayName("dados los valores del DERCAS, cuando se crea la política, entonces la acepta")
    void politicaDelDercas() {
        PoliticaReserva politica = new PoliticaReserva(15, 3, 1, 3, true);

        assertThat(politica.minutosExpiracion()).isEqualTo(15);
        assertThat(politica.diasAnticipacion()).isEqualTo(3);
    }

    @Test
    @DisplayName("dados los mínimos permitidos, cuando se crea la política, entonces la acepta")
    void politicaEnElLimite() {
        PoliticaReserva politica = new PoliticaReserva(1, 0, 0, 1, false);

        assertThat(politica.maxReservasEfectivo()).isZero();
    }

    @ParameterizedTest(name = "{5}")
    @CsvSource({
            "15, 3, 1, 0, true, 0 días de anticipación",
            "-5, 3, 1, 3, true, expiración negativa",
            "0, 3, 1, 3, true, 0 minutos de expiración",
            "15, -1, 1, 3, true, plazo de cancelación negativo",
            "15, 3, -1, 3, true, máximo en efectivo negativo",
    })
    @DisplayName("dado un valor fuera de rango, cuando se crea la política, entonces la rechaza")
    void politicaFueraDeRango(int minutos, int horas, int efectivo, int dias, boolean limitar, String caso) {
        assertThatThrownBy(() -> new PoliticaReserva(minutos, horas, efectivo, dias, limitar))
                .isInstanceOf(ConfiguracionInvalidaException.class);
    }

    @Test
    @DisplayName("dado un parámetro vacío, cuando se crea la política, entonces explica cuál falta")
    void politicaIncompleta() {
        assertThatThrownBy(() -> new PoliticaReserva(15, 3, 1, null, true))
                .isInstanceOf(ConfiguracionInvalidaException.class)
                .hasMessage("Los días de anticipación deben ser al menos 1.");
    }

    @Test
    @DisplayName("dados datos con espacios, cuando se crea el complejo, entonces los limpia")
    void complejoLimpio() {
        Complejo complejo = new Complejo(" Complejo Deportivo Calceta ", "Calceta, Manabí", "0969908865",
                "Banco Pichincha", TipoCuenta.AHORROS, " 2201234567 ", "Complejo Deportivo Calceta");

        assertThat(complejo.nombre()).isEqualTo("Complejo Deportivo Calceta");
        assertThat(complejo.numeroCuenta()).isEqualTo("2201234567");
    }

    @Test
    @DisplayName("dado un dato vacío, cuando se crea el complejo, entonces explica cuál falta")
    void complejoIncompleto() {
        assertThatThrownBy(() -> new Complejo("Complejo", "Calceta", "0969908865",
                "  ", TipoCuenta.AHORROS, "2201234567", "Titular"))
                .isInstanceOf(ConfiguracionInvalidaException.class)
                .hasMessage("Ingresa el banco.");
    }

    @Test
    @DisplayName("dado un tipo de cuenta desconocido, cuando se convierte, entonces lo rechaza")
    void tipoCuentaDesconocido() {
        assertThat(Complejo.tipoCuenta("corriente")).isEqualTo(TipoCuenta.CORRIENTE);
        assertThatThrownBy(() -> Complejo.tipoCuenta("PLAZO_FIJO"))
                .isInstanceOf(ConfiguracionInvalidaException.class);
    }
}
