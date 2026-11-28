package com.canchapp.shared.web;

import com.canchapp.canchas.application.CambiarEstadoCanchaUseCase;
import com.canchapp.canchas.application.CrearCanchaUseCase;
import com.canchapp.canchas.application.EditarCanchaUseCase;
import com.canchapp.canchas.application.ListarCanchasUseCase;
import com.canchapp.canchas.application.ObtenerCanchaUseCase;
import com.canchapp.canchas.application.ObtenerFotoCanchaUseCase;
import com.canchapp.canchas.application.SubirFotoCanchaUseCase;
import com.canchapp.canchas.web.CanchaController;
import com.canchapp.configuracion.application.ActualizarConfiguracionUseCase;
import com.canchapp.configuracion.application.ConsultarConfiguracionUseCase;
import com.canchapp.configuracion.web.ConfiguracionController;
import com.canchapp.horarios.application.ActualizarHorarioUseCase;
import com.canchapp.horarios.application.ConsultarHorarioUseCase;
import com.canchapp.horarios.application.CrearBloqueoUseCase;
import com.canchapp.horarios.application.EliminarBloqueoUseCase;
import com.canchapp.horarios.application.EliminarTarifaUseCase;
import com.canchapp.horarios.application.GuardarTarifaUseCase;
import com.canchapp.horarios.application.ListarBloqueosUseCase;
import com.canchapp.horarios.application.ListarTarifasUseCase;
import com.canchapp.horarios.web.BloqueoController;
import com.canchapp.horarios.web.HorarioController;
import com.canchapp.horarios.web.TarifaController;
import com.canchapp.shared.infrastructure.seguridad.ConfiguracionSeguridad;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cierre de la iteración 1 (PLANIFICACION §3): un {@code CLIENTE} recibe 403 en cada endpoint del
 * administrador, y sin sesión todos responden 401 (RNF-02).
 * <p>
 * Si se agrega un endpoint de administrador, se agrega aquí: así un {@code @PreAuthorize} olvidado
 * no pasa desapercibido.
 */
@WebMvcTest(controllers = {ConfiguracionController.class, CanchaController.class, HorarioController.class,
        TarifaController.class, BloqueoController.class})
@Import(ConfiguracionSeguridad.class)
@TestPropertySource(properties = "canchapp.seguridad.jwt-secreto=clave-de-prueba-de-al-menos-32-caracteres!!")
@DisplayName("Permisos de los endpoints del administrador (RNF-02)")
class PermisosAdministradorTest {

    @Autowired
    private MockMvc mvc;

    @MockBean private ConsultarConfiguracionUseCase consultarConfiguracion;
    @MockBean private ActualizarConfiguracionUseCase actualizarConfiguracion;
    @MockBean private ListarCanchasUseCase listarCanchas;
    @MockBean private ObtenerCanchaUseCase obtenerCancha;
    @MockBean private CrearCanchaUseCase crearCancha;
    @MockBean private EditarCanchaUseCase editarCancha;
    @MockBean private CambiarEstadoCanchaUseCase cambiarEstadoCancha;
    @MockBean private SubirFotoCanchaUseCase subirFotoCancha;
    @MockBean private ObtenerFotoCanchaUseCase obtenerFotoCancha;
    @MockBean private ConsultarHorarioUseCase consultarHorario;
    @MockBean private ActualizarHorarioUseCase actualizarHorario;
    @MockBean private ListarTarifasUseCase listarTarifas;
    @MockBean private GuardarTarifaUseCase guardarTarifa;
    @MockBean private EliminarTarifaUseCase eliminarTarifa;
    @MockBean private ListarBloqueosUseCase listarBloqueos;
    @MockBean private CrearBloqueoUseCase crearBloqueo;
    @MockBean private EliminarBloqueoUseCase eliminarBloqueo;

    /** Cada endpoint que solo puede usar el administrador. */
    static Stream<String> endpointsDeAdministrador() {
        return Stream.of(
                "PUT /api/v1/configuracion",
                "POST /api/v1/canchas",
                "PUT /api/v1/canchas/1",
                "PATCH /api/v1/canchas/1/estado",
                "PUT /api/v1/canchas/1/foto",
                "PUT /api/v1/horario",
                "POST /api/v1/tarifas",
                "PUT /api/v1/tarifas/1",
                "DELETE /api/v1/tarifas/1",
                "GET /api/v1/bloqueos",
                "POST /api/v1/bloqueos",
                "DELETE /api/v1/bloqueos/1");
    }

    private static MockHttpServletRequestBuilder solicitud(String endpoint) {
        String[] partes = endpoint.split(" ");
        if (partes[1].endsWith("/foto")) {
            return multipart(HttpMethod.PUT, partes[1])
                    .file(new MockMultipartFile("foto", "f.jpg", "image/jpeg", new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}));
        }
        return request(HttpMethod.valueOf(partes[0]), partes[1]).contentType(MediaType.APPLICATION_JSON).content("{}");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpointsDeAdministrador")
    @DisplayName("dado un cliente, cuando usa un endpoint del administrador, entonces recibe 403 SIN_PERMISO")
    void clienteRecibe403(String endpoint) throws Exception {
        mvc.perform(solicitud(endpoint).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("SIN_PERMISO"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpointsDeAdministrador")
    @DisplayName("dado que no hay sesión, cuando usa un endpoint del administrador, entonces recibe 401 NO_AUTENTICADO")
    void sinSesionRecibe401(String endpoint) throws Exception {
        mvc.perform(solicitud(endpoint))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"));
    }
}
