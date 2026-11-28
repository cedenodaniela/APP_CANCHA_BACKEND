package com.canchapp.canchas.web;

import com.canchapp.canchas.application.CambiarEstadoCanchaUseCase;
import com.canchapp.canchas.application.CrearCanchaUseCase;
import com.canchapp.canchas.application.EditarCanchaUseCase;
import com.canchapp.canchas.application.ListarCanchasUseCase;
import com.canchapp.canchas.application.ObtenerCanchaUseCase;
import com.canchapp.canchas.application.ObtenerFotoCanchaUseCase;
import com.canchapp.canchas.application.SubirFotoCanchaUseCase;
import com.canchapp.canchas.domain.Cancha;
import com.canchapp.canchas.domain.ReservasActivasException;
import com.canchapp.shared.infrastructure.seguridad.ConfiguracionSeguridad;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CanchaController.class)
@Import(ConfiguracionSeguridad.class)
@TestPropertySource(properties = "canchapp.seguridad.jwt-secreto=clave-de-prueba-de-al-menos-32-caracteres!!")
@DisplayName("API de canchas (RF-04)")
class CanchaControllerTest {

    private static final Instant AHORA = Instant.parse("2026-11-28T15:00:00Z");

    @Autowired
    private MockMvc mvc;

    @MockBean private ListarCanchasUseCase listar;
    @MockBean private ObtenerCanchaUseCase obtener;
    @MockBean private CrearCanchaUseCase crear;
    @MockBean private EditarCanchaUseCase editar;
    @MockBean private CambiarEstadoCanchaUseCase cambiarEstado;
    @MockBean private SubirFotoCanchaUseCase subirFoto;
    @MockBean private ObtenerFotoCanchaUseCase obtenerFoto;

    private static RequestPostProcessor con(String rol) {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    private static Cancha cancha(boolean activa, String foto) {
        return Cancha.reconstituir(1L, "Cancha 1", "Sintético", 30, 50, foto, activa, AHORA, AHORA);
    }

    @Test
    @DisplayName("dado un cliente, cuando lista las canchas, entonces solo pide las activas")
    void clienteListaActivas() throws Exception {
        when(listar.ejecutar(false)).thenReturn(List.of(cancha(true, null)));

        mvc.perform(get("/api/v1/canchas").with(con("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Cancha 1"))
                .andExpect(jsonPath("$[0].fotoVersion").doesNotExist());
    }

    @Test
    @DisplayName("dado un administrador, cuando lista las canchas, entonces recibe también las inactivas")
    void administradorListaTodas() throws Exception {
        when(listar.ejecutar(true)).thenReturn(List.of(cancha(true, "a.webp"), cancha(false, null)));

        mvc.perform(get("/api/v1/canchas").with(con("ADMINISTRADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fotoVersion").value(AHORA.toEpochMilli()))
                .andExpect(jsonPath("$[1].activa").value(false));
    }

    @Test
    @DisplayName("dado un cliente, cuando intenta crear una cancha, entonces recibe 403")
    void clienteNoCrea() throws Exception {
        mvc.perform(post("/api/v1/canchas").with(con("CLIENTE")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Cancha 3\",\"tipoCesped\":\"Sintético\",\"anchoMetros\":30,\"largoMetros\":50}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(crear);
    }

    @Test
    @DisplayName("dado un administrador, cuando crea una cancha, entonces recibe 201")
    void administradorCrea() throws Exception {
        when(crear.ejecutar(any())).thenReturn(cancha(true, null));

        mvc.perform(post("/api/v1/canchas").with(con("ADMINISTRADOR")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Cancha 1\",\"tipoCesped\":\"Sintético\",\"anchoMetros\":30,\"largoMetros\":50}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.activa").value(true));
    }

    @Test
    @DisplayName("dada una cancha con reservas activas, cuando la desactiva sin confirmar, entonces recibe 409 con cuántas hay")
    void desactivarPideConfirmacion() throws Exception {
        when(cambiarEstado.ejecutar(1L, false, false)).thenThrow(new ReservasActivasException(2));

        mvc.perform(patch("/api/v1/canchas/1/estado").with(con("ADMINISTRADOR")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activa\":false}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("RESERVAS_ACTIVAS"))
                .andExpect(jsonPath("$.mensaje").value("La cancha tiene 2 reservas activas. Confirma si quieres desactivarla de todos modos."));
    }

    @Test
    @DisplayName("dado un administrador, cuando sube una foto, entonces llega en la parte «foto» del multipart")
    void subirFoto() throws Exception {
        byte[] jpg = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1};
        when(subirFoto.ejecutar(eq(1L), any())).thenReturn(cancha(true, "a.webp"));

        mvc.perform(multipart("/api/v1/canchas/1/foto")
                        .file(new MockMultipartFile("foto", "cancha.jpg", "image/jpeg", jpg))
                        .with(r -> {
                            r.setMethod("PUT");
                            return r;
                        })
                        .with(con("ADMINISTRADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fotoVersion").value(AHORA.toEpochMilli()));
    }

    @Test
    @DisplayName("dado un cliente con sesión, cuando pide la foto, entonces la recibe como WebP; sin sesión, 401")
    void fotoAutenticada() throws Exception {
        when(obtenerFoto.ejecutar(1L, false)).thenReturn(Optional.of(new byte[] {1, 2}));

        mvc.perform(get("/api/v1/canchas/1/foto").with(con("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/webp"));
        mvc.perform(get("/api/v1/canchas/1/foto"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("dada una cancha sin foto, cuando se pide la foto, entonces recibe 404")
    void sinFoto() throws Exception {
        when(obtenerFoto.ejecutar(1L, true)).thenReturn(Optional.empty());

        mvc.perform(get("/api/v1/canchas/1/foto").with(con("ADMINISTRADOR")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("FOTO_NO_ENCONTRADA"));
    }
}
