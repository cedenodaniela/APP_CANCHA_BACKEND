package com.canchapp.usuarios.web;

import com.canchapp.shared.infrastructure.seguridad.ConfiguracionSeguridad;
import com.canchapp.usuarios.application.RestablecerContrasenaUseCase;
import com.canchapp.usuarios.application.SolicitarRecuperacionUseCase;
import com.canchapp.usuarios.domain.EnlaceRecuperacionInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RecuperacionController.class)
@Import(ConfiguracionSeguridad.class)
@TestPropertySource(properties = "canchapp.seguridad.jwt-secreto=clave-de-prueba-de-al-menos-32-caracteres!!")
@DisplayName("API de recuperación de contraseña (RF-02)")
class RecuperacionControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private SolicitarRecuperacionUseCase solicitar;

    @MockBean
    private RestablecerContrasenaUseCase restablecer;

    @Test
    @DisplayName("dado cualquier correo, cuando pide recuperar sin token, entonces recibe 202 con el mismo mensaje")
    void mismoMensajeSiempre() throws Exception {
        for (String email : new String[] {"ana@correo.com", "nadie@correo.com"}) {
            mvc.perform(post("/api/v1/auth/recuperacion").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"" + email + "\"}"))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.mensaje").value(RecuperacionController.MENSAJE_ENVIADO));
            verify(solicitar).ejecutar(email);
        }
    }

    @Test
    @DisplayName("dado un enlace vigente, cuando crea la contraseña sin token de sesión, entonces recibe 204")
    void restablece() throws Exception {
        mvc.perform(post("/api/v1/auth/recuperacion/restablecer").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"abc\",\"password\":\"nueva-clave\"}"))
                .andExpect(status().isNoContent());
        verify(restablecer).ejecutar("abc", "nueva-clave");
    }

    @Test
    @DisplayName("dado un enlace vencido o usado, cuando crea la contraseña, entonces recibe 400 con ENLACE_INVALIDO")
    void enlaceInvalido() throws Exception {
        doThrow(new EnlaceRecuperacionInvalidoException()).when(restablecer).ejecutar("viejo", "nueva-clave");

        mvc.perform(post("/api/v1/auth/recuperacion/restablecer").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"viejo\",\"password\":\"nueva-clave\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ENLACE_INVALIDO"))
                .andExpect(jsonPath("$.mensaje").value("El enlace no es válido o ya venció. Solicita uno nuevo."));
    }
}
