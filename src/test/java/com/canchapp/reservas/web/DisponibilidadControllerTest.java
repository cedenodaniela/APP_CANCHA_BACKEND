package com.canchapp.reservas.web;

import com.canchapp.canchas.domain.CanchaNoEncontradaException;
import com.canchapp.reservas.application.ConsultarDisponibilidadUseCase;
import com.canchapp.reservas.domain.ReservaInvalidaException;
import com.canchapp.shared.infrastructure.seguridad.ConfiguracionSeguridad;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DisponibilidadController.class)
@Import(ConfiguracionSeguridad.class)
@TestPropertySource(properties = "canchapp.seguridad.jwt-secreto=clave-de-prueba-de-al-menos-32-caracteres!!")
@DisplayName("API de disponibilidad (RF-06)")
class DisponibilidadControllerTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 28);

    @Autowired
    private MockMvc mvc;

    @MockBean private ConsultarDisponibilidadUseCase consultarDisponibilidad;

    private static RequestPostProcessor con(String rol) {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    @Test
    @DisplayName("dado un cliente con sesión, cuando consulta la disponibilidad, entonces recibe los bloques del día con su precio")
    void clienteConsultaDisponibilidad() throws Exception {
        when(consultarDisponibilidad.ejecutar(eq(1L), eq(HOY))).thenReturn(List.of(
                new ConsultarDisponibilidadUseCase.BloqueDisponible(1L, HOY, "10:00", "10:30", false, BigDecimal.TEN)));

        mvc.perform(get("/api/v1/disponibilidad").param("canchaId", "1").param("fecha", "2026-11-28").with(con("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].canchaId").value(1))
                .andExpect(jsonPath("$[0].fecha").value("2026-11-28"))
                .andExpect(jsonPath("$[0].inicio").value("10:00"))
                .andExpect(jsonPath("$[0].fin").value("10:30"))
                .andExpect(jsonPath("$[0].ocupado").value(false));
    }

    @Test
    @DisplayName("dado un cliente, cuando la fecha no tiene el formato AAAA-MM-DD, entonces recibe 400")
    void fechaInvalida() throws Exception {
        mvc.perform(get("/api/v1/disponibilidad").param("canchaId", "1").param("fecha", "28-11-2026").with(con("CLIENTE")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("RESERVA_INVALIDA"));
    }

    @Test
    @DisplayName("dado una fecha fuera de la anticipación, cuando se consulta, entonces recibe 400")
    void fechaFueraDeAnticipacion() throws Exception {
        when(consultarDisponibilidad.ejecutar(eq(1L), eq(HOY.minusDays(4)))).thenThrow(
                ReservaInvalidaException.datos("Solo se puede consultar disponibilidad de hoy hasta 3 días después."));

        mvc.perform(get("/api/v1/disponibilidad").param("canchaId", "1").param("fecha", "2026-11-24").with(con("CLIENTE")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("RESERVA_INVALIDA"));
    }

    @Test
    @DisplayName("dado una cancha que no existe, cuando se consulta, entonces recibe 404")
    void canchaInexistente() throws Exception {
        when(consultarDisponibilidad.ejecutar(eq(9L), eq(HOY))).thenThrow(new CanchaNoEncontradaException());

        mvc.perform(get("/api/v1/disponibilidad").param("canchaId", "9").param("fecha", "2026-11-28").with(con("CLIENTE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("CANCHA_NO_ENCONTRADA"));
    }

    @Test
    @DisplayName("dado un cliente sin sesión, cuando consulta, entonces recibe 401")
    void sinSesion() throws Exception {
        mvc.perform(get("/api/v1/disponibilidad").param("canchaId", "1").param("fecha", "2026-11-28"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"));
    }
}
