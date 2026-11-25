package com.canchapp.usuarios.web;

import com.canchapp.shared.infrastructure.seguridad.ConfiguracionSeguridad;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EnlaceRecuperacionController.class)
@Import(ConfiguracionSeguridad.class)
@TestPropertySource(properties = "canchapp.seguridad.jwt-secreto=clave-de-prueba-de-al-menos-32-caracteres!!")
@DisplayName("Página del enlace de recuperación (RF-02)")
class EnlaceRecuperacionControllerTest {

    private static final String TOKEN = "Abc123_-Abc123_-Abc123_-Abc123_-Abc123_-Abc";

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("dado el enlace del correo, cuando se abre sin sesión, entonces la página abre la app con el token")
    void abreLaApp() throws Exception {
        mvc.perform(get("/recuperar-contrasena").param("token", TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString(
                        "intent://restablecer-contrasena?token=" + TOKEN
                                + "#Intent;scheme=canchapp;package=com.canchapp.movil;end")))
                .andExpect(content().string(containsString("canchapp://restablecer-contrasena?token=" + TOKEN)))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(header().string("Cache-Control", containsString("no-store")));
    }

    @Test
    @DisplayName("dado un token con caracteres extraños, cuando se abre, entonces responde 400 y no lo copia en la página")
    void tokenInvalido() throws Exception {
        mvc.perform(get("/recuperar-contrasena").param("token", "\"><script>alert(1)</script>"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(containsString("<script>"))))
                .andExpect(content().string(not(containsString("intent://"))));
    }

    @Test
    @DisplayName("dado un enlace sin token, cuando se abre, entonces explica que pida uno nuevo")
    void sinToken() throws Exception {
        mvc.perform(get("/recuperar-contrasena"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Pide uno nuevo")));
    }
}
