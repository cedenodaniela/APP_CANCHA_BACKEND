package com.canchapp.horarios.web;

import com.canchapp.horarios.application.EliminarTarifaUseCase;
import com.canchapp.horarios.application.GuardarTarifaUseCase;
import com.canchapp.horarios.application.GuardarTarifaUseCase.DatosTarifa;
import com.canchapp.horarios.application.ListarTarifasUseCase;
import com.canchapp.horarios.domain.DiasSemana;
import com.canchapp.horarios.domain.Tarifa;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * Tarifas por franja (RF-05). Cualquier usuario con sesión las consulta; solo el administrador las
 * crea, cambia o borra.
 */
@RestController
@RequestMapping("/api/v1/tarifas")
public class TarifaController {

    /**
     * Una tarifa. {@code precioHora} va como texto con 2 decimales («25.00») para que ningún cliente
     * lo lea como un número con decimales binarios.
     */
    public record TarifaDto(Long id, List<String> dias, String inicio, String fin,
                            @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal precioHora) {
    }

    /** Cuerpo de alta y edición. {@code precioHora} se acepta como número o como texto. */
    public record SolicitudTarifa(List<String> dias, String inicio, String fin, BigDecimal precioHora) {
    }

    private final ListarTarifasUseCase listar;
    private final GuardarTarifaUseCase guardar;
    private final EliminarTarifaUseCase eliminar;

    public TarifaController(ListarTarifasUseCase listar, GuardarTarifaUseCase guardar, EliminarTarifaUseCase eliminar) {
        this.listar = listar;
        this.guardar = guardar;
        this.eliminar = eliminar;
    }

    @GetMapping
    public List<TarifaDto> listar() {
        return listar.ejecutar().stream().map(TarifaController::aDto).toList();
    }

    /** @return 201; 400 {@code HORARIO_INVALIDO}; 409 {@code TARIFA_SUPERPUESTA}; 403 si no es administrador */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public TarifaDto crear(@RequestBody SolicitudTarifa solicitud) {
        return aDto(guardar.ejecutar(null, aDatos(solicitud)));
    }

    /** @return 200; 400; 404; 409 {@code TARIFA_SUPERPUESTA}; 403 si no es administrador */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public TarifaDto editar(@PathVariable Long id, @RequestBody SolicitudTarifa solicitud) {
        return aDto(guardar.ejecutar(id, aDatos(solicitud)));
    }

    /** @return 204; 404; 403 si no es administrador */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void eliminar(@PathVariable Long id) {
        eliminar.ejecutar(id);
    }

    private static DatosTarifa aDatos(SolicitudTarifa s) {
        return new DatosTarifa(s.dias(), s.inicio(), s.fin(), s.precioHora());
    }

    static TarifaDto aDto(Tarifa t) {
        return new TarifaDto(t.getId(), t.getDias().stream().sorted().map(DiasSemana::nombre).toList(),
                t.getInicio().texto(), t.getFin().texto(), t.getPrecioHora());
    }
}
