package com.canchapp.usuarios.web;

import com.canchapp.usuarios.application.IniciarSesionUseCase;
import com.canchapp.usuarios.application.IniciarSesionUseCase.Sesion;
import com.canchapp.usuarios.infrastructure.JwtEmisorToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Inicio de sesión con JWT (RF-01).
 * <p>
 * Contrato que consumen la app Android ({@code APP_CANCHA_MOVIL}, {@code auth/data/AuthApi.kt})
 * y, después, el panel web (RF-18).
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    /** Cuerpo de {@code POST /api/v1/auth/login}. */
    public record LoginSolicitud(String email, String password) {
    }

    /** Usuario autenticado, lo mínimo para que el cliente decida a qué pantalla ir. */
    public record UsuarioSesion(Long id, String nombre, String rol) {
    }

    /** Token, su vencimiento y el usuario. */
    public record LoginRespuesta(String token, Instant expiraEn, UsuarioSesion usuario) {
    }

    private final IniciarSesionUseCase iniciarSesion;

    public AuthController(IniciarSesionUseCase iniciarSesion) {
        this.iniciarSesion = iniciarSesion;
    }

    /**
     * @return 200 con el token; 401 con «Correo o contraseña incorrectos» ante cualquier fallo
     */
    @PostMapping("/login")
    public LoginRespuesta login(@RequestBody LoginSolicitud solicitud) {
        Sesion sesion = iniciarSesion.ejecutar(solicitud.email(), solicitud.password());
        var usuario = sesion.usuario();
        return new LoginRespuesta(sesion.token().valor(), sesion.token().expiraEn(),
                new UsuarioSesion(usuario.getId(), usuario.getNombre(), usuario.getRol().name()));
    }

    /**
     * Devuelve el usuario del token. Los clientes lo usan para validar la sesión antes de
     * mostrar una pantalla protegida (AGENTS.md §6).
     *
     * @return 200 con el usuario; 401 si el token falta, es inválido o venció
     */
    @GetMapping("/sesion")
    public UsuarioSesion sesion(@AuthenticationPrincipal Jwt jwt) {
        return new UsuarioSesion(Long.valueOf(jwt.getSubject()),
                jwt.getClaimAsString(JwtEmisorToken.CLAIM_NOMBRE),
                jwt.getClaimAsString(JwtEmisorToken.CLAIM_ROL));
    }
}
