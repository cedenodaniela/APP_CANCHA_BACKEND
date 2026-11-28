package com.canchapp.configuracion.application;

import com.canchapp.configuracion.application.ActualizarConfiguracionUseCase.DatosConfiguracion;
import com.canchapp.configuracion.domain.ConfiguracionComplejo;
import com.canchapp.configuracion.domain.ConfiguracionInvalidaException;
import com.canchapp.configuracion.domain.ConfiguracionRepository;
import com.canchapp.configuracion.domain.TipoCuenta;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Configuración del complejo · casos de uso (RF-03)")
class ConfiguracionUseCasesTest {

    private static final Instant AHORA = Instant.parse("2026-11-27T15:00:00Z");

    private ConfiguracionRepository configuraciones;
    private ActualizarConfiguracionUseCase actualizar;

    @BeforeEach
    void preparar() {
        configuraciones = mock(ConfiguracionRepository.class);
        when(configuraciones.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        actualizar = new ActualizarConfiguracionUseCase(configuraciones,
                Clock.fixed(AHORA, ZoneId.of("America/Guayaquil")));
    }

    private static DatosConfiguracion datos(Integer diasAnticipacion) {
        return new DatosConfiguracion("Complejo Deportivo Calceta", "Calceta, Manabí", "0969908865",
                "Banco Pichincha", "ahorros", "2201234567", "Complejo Deportivo Calceta",
                20, 4, 2, diasAnticipacion, false);
    }

    @Test
    @DisplayName("dados datos válidos, cuando el administrador guarda, entonces se guarda todo con la hora del cambio")
    void guardaLaConfiguracion() {
        ConfiguracionComplejo guardada = actualizar.ejecutar(datos(5));

        assertThat(guardada.complejo().tipoCuenta()).isEqualTo(TipoCuenta.AHORROS);
        assertThat(guardada.politica().minutosExpiracion()).isEqualTo(20);
        assertThat(guardada.politica().diasAnticipacion()).isEqualTo(5);
        assertThat(guardada.politica().limitarTrasInasistencia()).isFalse();
        assertThat(guardada.actualizadaEn()).isEqualTo(AHORA);
    }

    @Test
    @DisplayName("dado un valor fuera de rango, cuando el administrador guarda, entonces no se guarda nada")
    void noGuardaAMedias() {
        assertThatThrownBy(() -> actualizar.ejecutar(datos(0)))
                .isInstanceOf(ConfiguracionInvalidaException.class);
        verify(configuraciones, never()).guardar(any());
    }

    @Test
    @DisplayName("dada la configuración guardada, cuando se consulta, entonces se devuelve la del repositorio")
    void consulta() {
        ConfiguracionComplejo guardada = actualizar.ejecutar(datos(3));
        when(configuraciones.obtener()).thenReturn(guardada);

        assertThat(new ConsultarConfiguracionUseCase(configuraciones).ejecutar()).isEqualTo(guardada);
    }
}
