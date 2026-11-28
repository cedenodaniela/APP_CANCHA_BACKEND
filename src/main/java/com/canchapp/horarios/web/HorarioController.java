package com.canchapp.horarios.web;

import com.canchapp.horarios.application.ActualizarHorarioUseCase;
import com.canchapp.horarios.application.ActualizarHorarioUseCase.DatosDia;
import com.canchapp.horarios.application.ConsultarHorarioUseCase;
import com.canchapp.horarios.domain.DiasSemana;
import com.canchapp.horarios.domain.HorarioDia;
import com.canchapp.horarios.domain.HorarioSemanal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Horario de atención del complejo (RF-05). Cualquier usuario con sesión lo consulta; solo el
 * administrador lo cambia.
 */
@RestController
@RequestMapping("/api/v1/horario")
public class HorarioController {

    /**
     * Un día. {@code dia} va de «LUNES» a «DOMINGO»; las horas son «HH:mm» y pueden llegar a «24:00».
     * Un día cerrado no lleva horas.
     */
    public record DiaDto(String dia, boolean abierto, String apertura, String cierre) {
    }

    /** Cuerpo de {@code PUT} y respuesta: los 7 días, de lunes a domingo. */
    public record HorarioDto(List<DiaDto> dias) {
    }

    /** Lo que llega en el {@code PUT}: {@code abierto} puede faltar si el día está cerrado. */
    public record SolicitudDia(String dia, Boolean abierto, String apertura, String cierre) {
    }

    public record SolicitudHorario(List<SolicitudDia> dias) {
    }

    private final ConsultarHorarioUseCase consultar;
    private final ActualizarHorarioUseCase actualizar;

    public HorarioController(ConsultarHorarioUseCase consultar, ActualizarHorarioUseCase actualizar) {
        this.consultar = consultar;
        this.actualizar = actualizar;
    }

    @GetMapping
    public HorarioDto consultar() {
        return aDto(consultar.ejecutar());
    }

    /** @return 200; 400 {@code HORARIO_INVALIDO} si falta un día o una hora no es válida; 403 si no es administrador */
    @PutMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public HorarioDto actualizar(@RequestBody SolicitudHorario solicitud) {
        List<DatosDia> dias = solicitud.dias() == null ? null : solicitud.dias().stream()
                .map(d -> new DatosDia(d.dia(), d.abierto(), d.apertura(), d.cierre()))
                .toList();
        return aDto(actualizar.ejecutar(dias));
    }

    static HorarioDto aDto(HorarioSemanal semana) {
        return new HorarioDto(semana.dias().stream().map(HorarioController::aDto).toList());
    }

    static DiaDto aDto(HorarioDia d) {
        return new DiaDto(DiasSemana.nombre(d.dia()), d.abierto(),
                d.abierto() ? d.apertura().texto() : null, d.abierto() ? d.cierre().texto() : null);
    }
}
