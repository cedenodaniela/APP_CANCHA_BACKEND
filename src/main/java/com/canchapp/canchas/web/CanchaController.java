package com.canchapp.canchas.web;

import com.canchapp.canchas.application.CambiarEstadoCanchaUseCase;
import com.canchapp.canchas.application.CrearCanchaUseCase;
import com.canchapp.canchas.application.CrearCanchaUseCase.DatosCancha;
import com.canchapp.canchas.application.EditarCanchaUseCase;
import com.canchapp.canchas.application.ListarCanchasUseCase;
import com.canchapp.canchas.application.ObtenerCanchaUseCase;
import com.canchapp.canchas.application.ObtenerFotoCanchaUseCase;
import com.canchapp.canchas.application.SubirFotoCanchaUseCase;
import com.canchapp.canchas.domain.Cancha;
import com.canchapp.canchas.domain.CanchaInvalidaException;
import com.canchapp.shared.web.ErrorRespuesta;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * Canchas del complejo (RF-04).
 * <p>
 * Cualquier usuario con sesión las consulta; el cliente solo ve las activas. Crear, editar, cambiar
 * la foto y activar o desactivar es solo del administrador. La foto nunca tiene una URL pública: se
 * sirve por {@code GET /canchas/{id}/foto}, que exige sesión.
 */
@RestController
@RequestMapping("/api/v1/canchas")
public class CanchaController {

    private static final String ROL_ADMINISTRADOR = "ROLE_ADMINISTRADOR";
    private static final MediaType WEBP = MediaType.parseMediaType("image/webp");

    /** Cuerpo de alta y edición. */
    public record SolicitudCancha(String nombre, String tipoCesped, Integer anchoMetros, Integer largoMetros) {
    }

    /**
     * Cuerpo de {@code PATCH /canchas/{id}/estado}.
     *
     * @param confirmar el administrador ya vio cuántas reservas activas tiene y quiere desactivarla igual
     */
    public record SolicitudEstado(Boolean activa, Boolean confirmar) {
    }

    /**
     * Una cancha.
     *
     * @param fotoVersion cambia cada vez que cambia la foto; los clientes la agregan como {@code ?v=} a la
     *                    URL de la foto para no mostrar una vieja de su caché. Es {@code null} si no tiene foto.
     */
    public record CanchaRespuesta(Long id, String nombre, String tipoCesped, int anchoMetros, int largoMetros,
                                  boolean activa, Long fotoVersion) {
    }

    private final ListarCanchasUseCase listar;
    private final ObtenerCanchaUseCase obtener;
    private final CrearCanchaUseCase crear;
    private final EditarCanchaUseCase editar;
    private final CambiarEstadoCanchaUseCase cambiarEstado;
    private final SubirFotoCanchaUseCase subirFoto;
    private final ObtenerFotoCanchaUseCase obtenerFoto;

    public CanchaController(ListarCanchasUseCase listar, ObtenerCanchaUseCase obtener, CrearCanchaUseCase crear,
                            EditarCanchaUseCase editar, CambiarEstadoCanchaUseCase cambiarEstado,
                            SubirFotoCanchaUseCase subirFoto, ObtenerFotoCanchaUseCase obtenerFoto) {
        this.listar = listar;
        this.obtener = obtener;
        this.crear = crear;
        this.editar = editar;
        this.cambiarEstado = cambiarEstado;
        this.subirFoto = subirFoto;
        this.obtenerFoto = obtenerFoto;
    }

    /** @return el administrador recibe todas; el cliente, solo las activas (RF-04) */
    @GetMapping
    public List<CanchaRespuesta> listar(Authentication sesion) {
        return listar.ejecutar(esAdministrador(sesion)).stream().map(CanchaController::aRespuesta).toList();
    }

    /** @return 200; 404 si no existe o, para un cliente, si está inactiva */
    @GetMapping("/{id}")
    public CanchaRespuesta obtener(@PathVariable Long id, Authentication sesion) {
        return aRespuesta(obtener.ejecutar(id, esAdministrador(sesion)));
    }

    /** @return 201; 400 {@code CANCHA_INVALIDA}; 409 {@code CANCHA_REPETIDA}; 403 si no es administrador */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public CanchaRespuesta crear(@RequestBody SolicitudCancha solicitud) {
        return aRespuesta(crear.ejecutar(aDatos(solicitud)));
    }

    /** @return 200; 400; 404; 409 {@code CANCHA_REPETIDA}; 403 si no es administrador */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public CanchaRespuesta editar(@PathVariable Long id, @RequestBody SolicitudCancha solicitud) {
        return aRespuesta(editar.ejecutar(id, aDatos(solicitud)));
    }

    /**
     * Activa o desactiva la cancha.
     *
     * @return 200; 409 {@code RESERVAS_ACTIVAS} con cuántas hay si se desactiva sin confirmar; 403 si no es administrador
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public CanchaRespuesta cambiarEstado(@PathVariable Long id, @RequestBody SolicitudEstado solicitud) {
        if (solicitud.activa() == null) {
            throw new CanchaInvalidaException("Indica si la cancha queda activa o inactiva.");
        }
        return aRespuesta(cambiarEstado.ejecutar(id, solicitud.activa(), Boolean.TRUE.equals(solicitud.confirmar())));
    }

    /**
     * Sube o reemplaza la foto, en la parte {@code foto} de un formulario multipart.
     *
     * @return 200 con la cancha; 400 {@code IMAGEN_INVALIDA} o {@code ARCHIVO_MUY_GRANDE}; 403 si no es administrador
     */
    @PutMapping(value = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public CanchaRespuesta subirFoto(@PathVariable Long id, @RequestPart("foto") MultipartFile foto) throws IOException {
        return aRespuesta(subirFoto.ejecutar(id, foto.getBytes()));
    }

    /** @return 200 con la imagen WebP; 404 si la cancha no existe o no tiene foto */
    @GetMapping("/{id}/foto")
    public ResponseEntity<?> foto(@PathVariable Long id, Authentication sesion) {
        return obtenerFoto.ejecutar(id, esAdministrador(sesion))
                .<ResponseEntity<?>>map(bytes -> ResponseEntity.ok()
                        .contentType(WEBP)
                        .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePrivate())
                        .body(bytes))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new ErrorRespuesta("FOTO_NO_ENCONTRADA", "La cancha todavía no tiene foto.")));
    }

    private static boolean esAdministrador(Authentication sesion) {
        return sesion != null && sesion.getAuthorities().stream()
                .anyMatch(a -> ROL_ADMINISTRADOR.equals(a.getAuthority()));
    }

    private static DatosCancha aDatos(SolicitudCancha s) {
        return new DatosCancha(s.nombre(), s.tipoCesped(), s.anchoMetros(), s.largoMetros());
    }

    static CanchaRespuesta aRespuesta(Cancha c) {
        Long version = c.tieneFoto() && c.getActualizadaEn() != null ? c.getActualizadaEn().toEpochMilli() : null;
        return new CanchaRespuesta(c.getId(), c.getNombre(), c.getTipoCesped(), c.getAnchoMetros(), c.getLargoMetros(),
                c.isActiva(), version);
    }
}
