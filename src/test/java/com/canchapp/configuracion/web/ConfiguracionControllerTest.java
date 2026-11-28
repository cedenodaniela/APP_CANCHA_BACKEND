package com.canchapp.configuracion.web;

import com.canchapp.configuracion.application.ActualizarConfiguracionUseCase;
import com.canchapp.configuracion.application.ConsultarConfiguracionUseCase;
import com.canchapp.configuracion.domain.Complejo;
import com.canchapp.configuracion.domain.ConfiguracionComplejo;
import com.canchapp.configuracion.domain.ConfiguracionInvalidaException;
import com.canchapp.configuracion.domain.PoliticaReserva;
import com.canchapp.configuracion.domain.TipoCuenta;
import com.canchapp.shared.infrastructure.seguridad.ConfiguracionSeguridad;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ConfiguracionController.class)
@Import(ConfiguracionSeguridad.class)
@TestPropertySource(properties = "canchapp.seguridad.jwt-secreto=clave-de-prueba-de-al-menos-32-caracteres!!")
@DisplayName("API de configuración del complejo (RF-03)")
class ConfiguracionControllerTest {

    private static final String CUERPO = """
            {"complejo":{"nombre":"Complejo Deportivo Calceta","direccion":"Calceta, Manabí","telefono":"0969908865"},
             "datosBancarios":{"banco":"Banco Pichincha","tipoCuenta":"AHORROS","numeroCuenta":"2201234567",
                               "titular":"Complejo Deportivo Calceta"},
             "politica":{"minutosExpiracion":15,"horasPlazoCancelacion":3,"maxReservasEfectivo":1,
                         "diasAnticipacion":3,"limitarTrasInasistencia":true}}
            """;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ConsultarConfiguracionUseCase consultar;

    @MockBean
    private ActualizarConfiguracionUseCase actualizar;

    private static RequestPostProcessor con(String rol) {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    private static ConfiguracionComplejo configuracion() {
        return new ConfiguracionComplejo(
                new Complejo("Complejo Deportivo Calceta", "Calceta, Manabí", "0969908865",
                        "Banco Pichincha", TipoCuenta.AHORROS, "2201234567", "Complejo Deportivo Calceta"),
                new PoliticaReserva(15, 3, 1, 3, true),
                Instant.parse("2026-11-27T15:00:00Z"));
    }

    @Test
    @DisplayName("dado un cliente con sesión, cuando consulta, entonces ve los datos bancarios para pagar")
    void clienteConsulta() throws Exception {
        when(consultar.ejecutar()).thenReturn(configuracion());

        mvc.perform(get("/api/v1/configuracion").with(con("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.complejo.nombre").value("Complejo Deportivo Calceta"))
                .andExpect(jsonPath("$.datosBancarios.numeroCuenta").value("2201234567"))
                .andExpect(jsonPath("$.datosBancarios.tipoCuenta").value("AHORROS"))
                .andExpect(jsonPath("$.politica.minutosExpiracion").value(15))
                .andExpect(jsonPath("$.politica.limitarTrasInasistencia").value(true));
    }

    @Test
    @DisplayName("dado que no hay sesión, cuando consulta, entonces recibe 401")
    void sinSesion() throws Exception {
        mvc.perform(get("/api/v1/configuracion"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"));
    }

    @Test
    @DisplayName("dado un administrador, cuando guarda la configuración, entonces recibe 200 con lo guardado")
    void administradorGuarda() throws Exception {
        when(actualizar.ejecutar(any())).thenReturn(configuracion());

        mvc.perform(put("/api/v1/configuracion").with(con("ADMINISTRADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.politica.diasAnticipacion").value(3));
    }

    @Test
    @DisplayName("dado un cliente, cuando intenta cambiar la configuración, entonces recibe 403 y no cambia nada")
    void clienteNoPuedeCambiar() throws Exception {
        mvc.perform(put("/api/v1/configuracion").with(con("CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("SIN_PERMISO"));
        verifyNoInteractions(actualizar);
    }

    @Test
    @DisplayName("dado un valor fuera de rango, cuando el administrador guarda, entonces recibe 400 con el motivo")
    void fueraDeRango() throws Exception {
        when(actualizar.ejecutar(any()))
                .thenThrow(new ConfiguracionInvalidaException("Los días de anticipación deben ser al menos 1."));

        mvc.perform(put("/api/v1/configuracion").with(con("ADMINISTRADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO.replace("\"diasAnticipacion\":3", "\"diasAnticipacion\":0")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("CONFIGURACION_INVALIDA"))
                .andExpect(jsonPath("$.mensaje").value("Los días de anticipación deben ser al menos 1."));
    }
}
