package com.canchapp.horarios.web;

import com.canchapp.horarios.application.ActualizarHorarioUseCase;
import com.canchapp.horarios.application.ConsultarHorarioUseCase;
import com.canchapp.horarios.application.CrearBloqueoUseCase;
import com.canchapp.horarios.application.EliminarBloqueoUseCase;
import com.canchapp.horarios.application.EliminarTarifaUseCase;
import com.canchapp.horarios.application.GuardarTarifaUseCase;
import com.canchapp.horarios.application.ListarBloqueosUseCase;
import com.canchapp.horarios.application.ListarTarifasUseCase;
import com.canchapp.horarios.domain.Bloqueo;
import com.canchapp.horarios.domain.HoraDelDia;
import com.canchapp.horarios.domain.HorarioDia;
import com.canchapp.horarios.domain.HorarioSemanal;
import com.canchapp.horarios.domain.ReservasEnBloqueoException;
import com.canchapp.horarios.domain.Tarifa;
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
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {HorarioController.class, TarifaController.class, BloqueoController.class})
@Import(ConfiguracionSeguridad.class)
@TestPropertySource(properties = "canchapp.seguridad.jwt-secreto=clave-de-prueba-de-al-menos-32-caracteres!!")
@DisplayName("API de horario, tarifas y bloqueos (RF-05)")
class HorariosControllerTest {

    private static final String TARIFA = "{\"dias\":[\"LUNES\"],\"inicio\":\"10:00\",\"fin\":\"18:00\",\"precioHora\":\"20.00\"}";
    private static final String BLOQUEO = "{\"canchaId\":1,\"fecha\":\"2026-12-01\",\"inicio\":\"10:00\",\"fin\":\"12:00\",\"motivo\":\"Mantenimiento\"}";

    @Autowired
    private MockMvc mvc;

    @MockBean private ConsultarHorarioUseCase consultarHorario;
    @MockBean private ActualizarHorarioUseCase actualizarHorario;
    @MockBean private ListarTarifasUseCase listarTarifas;
    @MockBean private GuardarTarifaUseCase guardarTarifa;
    @MockBean private EliminarTarifaUseCase eliminarTarifa;
    @MockBean private ListarBloqueosUseCase listarBloqueos;
    @MockBean private CrearBloqueoUseCase crearBloqueo;
    @MockBean private EliminarBloqueoUseCase eliminarBloqueo;

    private static RequestPostProcessor con(String rol) {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    private static HoraDelDia h(String texto) {
        return HoraDelDia.de(texto, "x");
    }

    private static HorarioSemanal semana() {
        return HorarioSemanal.de(Arrays.stream(DayOfWeek.values())
                .map(d -> new HorarioDia(d, true, h("10:00"), h("24:00"))).toList());
    }

    @Test
    @DisplayName("dado un cliente, cuando consulta el horario y las tarifas, entonces los ve con 24:00 y el precio como texto")
    void clienteConsulta() throws Exception {
        when(consultarHorario.ejecutar()).thenReturn(semana());
        when(listarTarifas.ejecutar()).thenReturn(List.of(
                Tarifa.de(2L, EnumSet.allOf(DayOfWeek.class), h("18:00"), h("24:00"), new BigDecimal("25"))));

        mvc.perform(get("/api/v1/horario").with(con("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dias[0].dia").value("LUNES"))
                .andExpect(jsonPath("$.dias[6].cierre").value("24:00"));
        mvc.perform(get("/api/v1/tarifas").with(con("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].precioHora").value("25.00"))
                .andExpect(jsonPath("$[0].dias.length()").value(7));
    }

    @Test
    @DisplayName("dado un cliente, cuando intenta cambiar horario, tarifas o bloqueos, entonces recibe 403 en cada uno")
    void clienteRecibe403() throws Exception {
        List<RequestBuilder> deAdministrador = List.of(
                put("/api/v1/horario").contentType(MediaType.APPLICATION_JSON).content("{\"dias\":[]}").with(con("CLIENTE")),
                post("/api/v1/tarifas").contentType(MediaType.APPLICATION_JSON).content(TARIFA).with(con("CLIENTE")),
                put("/api/v1/tarifas/1").contentType(MediaType.APPLICATION_JSON).content(TARIFA).with(con("CLIENTE")),
                delete("/api/v1/tarifas/1").with(con("CLIENTE")),
                get("/api/v1/bloqueos").with(con("CLIENTE")),
                post("/api/v1/bloqueos").contentType(MediaType.APPLICATION_JSON).content(BLOQUEO).with(con("CLIENTE")),
                delete("/api/v1/bloqueos/1").with(con("CLIENTE")));

        for (RequestBuilder solicitud : deAdministrador) {
            mvc.perform(solicitud).andExpect(status().isForbidden()).andExpect(jsonPath("$.codigo").value("SIN_PERMISO"));
        }
        verifyNoInteractions(actualizarHorario, guardarTarifa, eliminarTarifa, listarBloqueos, crearBloqueo, eliminarBloqueo);
    }

    @Test
    @DisplayName("dado un administrador, cuando crea una tarifa, entonces recibe 201")
    void administradorCreaTarifa() throws Exception {
        when(guardarTarifa.ejecutar(eq(null), any()))
                .thenReturn(Tarifa.de(5L, EnumSet.of(DayOfWeek.MONDAY), h("10:00"), h("18:00"), new BigDecimal("20")));

        mvc.perform(post("/api/v1/tarifas").with(con("ADMINISTRADOR")).contentType(MediaType.APPLICATION_JSON).content(TARIFA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.dias[0]").value("LUNES"));
    }

    @Test
    @DisplayName("dado un horario con reservas activas, cuando bloquea sin confirmar, entonces recibe 409 con cuántas hay")
    void bloqueoPideConfirmacion() throws Exception {
        when(crearBloqueo.ejecutar(any(), eq(false))).thenThrow(new ReservasEnBloqueoException(2));

        mvc.perform(post("/api/v1/bloqueos").with(con("ADMINISTRADOR")).contentType(MediaType.APPLICATION_JSON).content(BLOQUEO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("RESERVAS_ACTIVAS"));
    }

    @Test
    @DisplayName("dado un administrador, cuando confirma el bloqueo, entonces recibe 201 con la fecha")
    void bloqueoConfirmado() throws Exception {
        when(crearBloqueo.ejecutar(any(), eq(true))).thenReturn(
                new Bloqueo(4L, 1L, LocalDate.of(2026, 12, 1), h("10:00"), h("12:00"), "Mantenimiento", null));

        mvc.perform(post("/api/v1/bloqueos").with(con("ADMINISTRADOR")).contentType(MediaType.APPLICATION_JSON)
                        .content(BLOQUEO.replace("}", ",\"confirmar\":true}")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fecha").value("2026-12-01"))
                .andExpect(jsonPath("$.fin").value("12:00"));
    }
}
