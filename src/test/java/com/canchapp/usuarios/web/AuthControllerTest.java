package com.canchapp.usuarios.web;

import com.canchapp.shared.infrastructure.seguridad.ConfiguracionSeguridad;
import com.canchapp.usuarios.application.IniciarSesionUseCase;
import com.canchapp.usuarios.application.IniciarSesionUseCase.Sesion;
import com.canchapp.usuarios.application.RegistrarUsuarioUseCase;
import com.canchapp.usuarios.domain.CorreoYaRegistradoException;
import com.canchapp.usuarios.domain.CredencialesInvalidasException;
import com.canchapp.usuarios.domain.EmisorToken.TokenEmitido;
import com.canchapp.usuarios.domain.Rol;
import com.canchapp.usuarios.domain.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, UsuarioController.class})
@Import(ConfiguracionSeguridad.class)
@TestPropertySource(properties = "canchapp.seguridad.jwt-secreto=clave-de-prueba-de-al-menos-32-caracteres!!")
@DisplayName("API de autenticación y registro (RF-01)")
class AuthControllerTest {

    private static final Instant AHORA = Instant.parse("2026-11-24T15:00:00Z");

    @Autowired
    private MockMvc mvc;

    @MockBean
    private IniciarSesionUseCase iniciarSesion;

    @MockBean
    private RegistrarUsuarioUseCase registrar;

    private static Usuario cliente() {
        return Usuario.reconstituir(7L, "Ana", "Pérez", "ana@correo.com", "0999999999", "$2a$10$HASH",
                Rol.CLIENTE, true, AHORA, AHORA);
    }

    @Test
    @DisplayName("dadas credenciales válidas, cuando hace login sin token, entonces recibe 200 con token y rol")
    void loginCorrecto() throws Exception {
        when(iniciarSesion.ejecutar("ana@correo.com", "secreta123"))
                .thenReturn(new Sesion(new TokenEmitido("jwt-firmado", AHORA.plusSeconds(86_400)), cliente()));

        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@correo.com\",\"password\":\"secreta123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-firmado"))
                .andExpect(jsonPath("$.usuario.id").value(7))
                .andExpect(jsonPath("$.usuario.rol").value("CLIENTE"));
    }

    @Test
    @DisplayName("dadas credenciales inválidas, cuando hace login, entonces recibe 401 con «Correo o contraseña incorrectos»")
    void loginIncorrecto() throws Exception {
        when(iniciarSesion.ejecutar(any(), any())).thenThrow(new CredencialesInvalidasException());

        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@correo.com\",\"password\":\"mala\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("CREDENCIALES_INVALIDAS"))
                .andExpect(jsonPath("$.mensaje").value("Correo o contraseña incorrectos"));
    }

    @Test
    @DisplayName("dado un registro válido, cuando se envía sin token, entonces recibe 201 y la respuesta no trae la contraseña")
    void registroPublicoSinContrasenaEnRespuesta() throws Exception {
        when(registrar.ejecutar(any())).thenReturn(cliente());

        mvc.perform(post("/api/v1/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ana","apellido":"Pérez","email":"ana@correo.com",
                                 "telefono":"0999999999","password":"secreta123"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.hashContrasena").doesNotExist())
                .andExpect(content().string(not(containsString("$2a$10$HASH"))));
    }

    @Test
    @DisplayName("dado un correo ya registrado, cuando se registra, entonces recibe 409 con mensaje en español")
    void registroDuplicado() throws Exception {
        when(registrar.ejecutar(any())).thenThrow(new CorreoYaRegistradoException());

        mvc.perform(post("/api/v1/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana\",\"apellido\":\"Pérez\",\"email\":\"ana@correo.com\",\"password\":\"secreta123\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CORREO_YA_REGISTRADO"));
    }

    @Test
    @DisplayName("dado que no hay token, cuando consulta la sesión, entonces recibe 401 con el cuerpo uniforme")
    void sesionSinToken() throws Exception {
        mvc.perform(get("/api/v1/auth/sesion"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"));
    }

    @Test
    @DisplayName("dado un token válido, cuando consulta la sesión, entonces recibe su usuario")
    void sesionConToken() throws Exception {
        mvc.perform(get("/api/v1/auth/sesion")
                        .with(jwt().jwt(j -> j.subject("7").claim("rol", "CLIENTE").claim("nombre", "Ana"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
    }

    @Test
    @DisplayName("dado un JSON mal formado, cuando hace login, entonces recibe 400 con el cuerpo uniforme")
    void jsonMalFormado() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{malo"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));
    }
}
