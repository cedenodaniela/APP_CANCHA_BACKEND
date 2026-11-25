package com.canchapp.usuarios.web;

import com.canchapp.usuarios.application.RestablecerContrasenaUseCase;
import com.canchapp.usuarios.application.SolicitarRecuperacionUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Recuperación de contraseña (RF-02). Los dos endpoints son públicos: quien los usa no puede
 * iniciar sesión.
 */
@RestController
@RequestMapping("/api/v1/auth/recuperacion")
public class RecuperacionController {

    /** Mismo texto exista o no la cuenta (RF-02). */
    public static final String MENSAJE_ENVIADO =
            "Si el correo está registrado, te enviamos un enlace para crear una contraseña nueva. Revisa tu bandeja de entrada.";

    /** Cuerpo de {@code POST /api/v1/auth/recuperacion}. */
    public record SolicitudRecuperacion(String email) {
    }

    /** Cuerpo de {@code POST /api/v1/auth/recuperacion/restablecer}. */
    public record SolicitudRestablecer(String token, String password) {
    }

    /** Respuesta con el texto que muestran los clientes. */
    public record RespuestaRecuperacion(String mensaje) {
    }

    private final SolicitarRecuperacionUseCase solicitar;
    private final RestablecerContrasenaUseCase restablecer;

    public RecuperacionController(SolicitarRecuperacionUseCase solicitar, RestablecerContrasenaUseCase restablecer) {
        this.solicitar = solicitar;
        this.restablecer = restablecer;
    }

    /**
     * Envía el enlace de recuperación al correo, si pertenece a una cuenta activa.
     *
     * @return 202 con el mismo mensaje exista o no la cuenta; 400 si el texto no es un correo
     */
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public RespuestaRecuperacion solicitar(@RequestBody SolicitudRecuperacion solicitud) {
        solicitar.ejecutar(solicitud.email());
        return new RespuestaRecuperacion(MENSAJE_ENVIADO);
    }

    /**
     * Cambia la contraseña con el token del enlace. Marcar el enlace como usado y guardar la
     * contraseña van en la misma transacción.
     *
     * @return 204; 400 si el enlace no sirve o la contraseña no cumple las reglas del registro
     */
    @PostMapping("/restablecer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void restablecer(@RequestBody SolicitudRestablecer solicitud) {
        restablecer.ejecutar(solicitud.token(), solicitud.password());
    }
}
