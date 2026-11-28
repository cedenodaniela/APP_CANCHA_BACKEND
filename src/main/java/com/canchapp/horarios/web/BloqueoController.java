package com.canchapp.horarios.web;

import com.canchapp.horarios.application.CrearBloqueoUseCase;
import com.canchapp.horarios.application.CrearBloqueoUseCase.DatosBloqueo;
import com.canchapp.horarios.application.EliminarBloqueoUseCase;
import com.canchapp.horarios.application.ListarBloqueosUseCase;
import com.canchapp.horarios.domain.Bloqueo;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Bloqueos de horario (RF-05), solo del administrador. El cliente no los consulta: los ve como
 * bloques no disponibles en la consulta de disponibilidad (RF-06).
 */
@RestController
@RequestMapping("/api/v1/bloqueos")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class BloqueoController {

    /** Un bloqueo. {@code canchaId} es {@code null} si bloquea todo el complejo; la fecha es «AAAA-MM-DD». */
    public record BloqueoDto(Long id, Long canchaId, LocalDate fecha, String inicio, String fin, String motivo) {
    }

    /**
     * Cuerpo de {@code POST}.
     *
     * @param confirmar el administrador ya vio cuántas reservas activas hay en ese horario y quiere bloquearlo igual
     */
    public record SolicitudBloqueo(Long canchaId, LocalDate fecha, String inicio, String fin, String motivo,
                                   Boolean confirmar) {
    }

    private final ListarBloqueosUseCase listar;
    private final CrearBloqueoUseCase crear;
    private final EliminarBloqueoUseCase eliminar;

    public BloqueoController(ListarBloqueosUseCase listar, CrearBloqueoUseCase crear, EliminarBloqueoUseCase eliminar) {
        this.listar = listar;
        this.crear = crear;
        this.eliminar = eliminar;
    }

    /** @return los bloqueos de hoy en adelante; 403 si no es administrador */
    @GetMapping
    public List<BloqueoDto> listar() {
        return listar.ejecutar().stream().map(BloqueoController::aDto).toList();
    }

    /**
     * @return 201; 400 {@code HORARIO_INVALIDO}; 409 {@code RESERVAS_ACTIVAS} con cuántas hay si no se confirmó;
     *         403 si no es administrador
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BloqueoDto crear(@RequestBody SolicitudBloqueo s) {
        return aDto(crear.ejecutar(new DatosBloqueo(s.canchaId(), s.fecha(), s.inicio(), s.fin(), s.motivo()),
                Boolean.TRUE.equals(s.confirmar())));
    }

    /** @return 204; 404; 403 si no es administrador */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        eliminar.ejecutar(id);
    }

    static BloqueoDto aDto(Bloqueo b) {
        return new BloqueoDto(b.id(), b.canchaId(), b.fecha(), b.inicio().texto(), b.fin().texto(), b.motivo());
    }
}
