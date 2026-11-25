package com.canchapp.shared.web;

import com.canchapp.shared.domain.ExcepcionNegocio;
import com.canchapp.shared.infrastructure.seguridad.ConfiguracionSeguridad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce las excepciones a respuestas HTTP con el cuerpo {@link ErrorRespuesta}.
 * <p>
 * Así ningún error llega al cliente como un 500 con la traza de Java, y la app y el panel
 * pueden mostrar siempre el {@code mensaje} que envía el backend.
 */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger LOG = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(ExcepcionNegocio.class)
    public ResponseEntity<ErrorRespuesta> negocio(ExcepcionNegocio ex) {
        HttpStatus estado = switch (ex.getTipo()) {
            case DATOS_INVALIDOS -> HttpStatus.BAD_REQUEST;
            case NO_AUTENTICADO -> HttpStatus.UNAUTHORIZED;
            case CONFLICTO -> HttpStatus.CONFLICT;
        };
        return ResponseEntity.status(estado).body(new ErrorRespuesta(ex.getCodigo(), ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> cuerpoIlegible(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorRespuesta("SOLICITUD_INVALIDA", "La solicitud no tiene el formato esperado."));
    }

    /** Un {@code @PreAuthorize} que no se cumple: rol sin permiso (RNF-02). */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorRespuesta> sinPermiso(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorRespuesta("SIN_PERMISO", ConfiguracionSeguridad.MENSAJE_SIN_PERMISO));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> inesperado(Exception ex) {
        LOG.error("Error no controlado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorRespuesta("ERROR_INTERNO", "Ocurrió un error inesperado. Inténtalo de nuevo."));
    }
}
