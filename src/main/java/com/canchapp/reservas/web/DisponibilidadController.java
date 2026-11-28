package com.canchapp.reservas.web;

import com.canchapp.reservas.application.ConsultarDisponibilidadUseCase;
import com.canchapp.reservas.domain.ReservaInvalidaException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Disponibilidad de canchas para una fecha (RF-06).
 * <p>
 * Devuelve los bloques de 30 minutos del día con su estado (libre u ocupado) y su precio, para que
 * el cliente consulte y elija su reserva sin recalcular nada (AGENTS.md §6: la UI no calcula reglas).
 * El cálculo de ocupación y precios vive en el caso de uso, no en el controlador.
 */
@RestController
@RequestMapping("/api/v1/disponibilidad")
public class DisponibilidadController {

    /** Un bloque de disponibilidad en el contrato de la API. El precio va con escala 2 (RN-03). */
    public record BloqueDisponible(Long canchaId, LocalDate fecha, String inicio, String fin, boolean ocupado,
                                   BigDecimal precio) {
    }

    private final ConsultarDisponibilidadUseCase consultar;

    public DisponibilidadController(ConsultarDisponibilidadUseCase consultar) {
        this.consultar = consultar;
    }

    /**
     * Bloques de disponibilidad de una cancha (o de las activas) para una fecha.
     *
     * @param canchaId {@code null} para las canchas activas; si se envía debe existir
     * @param fecha    «AAAA-MM-DD»; desde hoy hasta hoy + {@code diasAnticipacion} (RN-02)
     * @return 200 con los bloques del día; 400 si falta la fecha o está fuera de anticipación;
     *         404 si la cancha no existe
     */
    @GetMapping
    public List<BloqueDisponible> consultar(@RequestParam(required = false) String canchaId,
                                            @RequestParam(required = false) String fecha) {
        Long id = null;
        if (canchaId != null && !canchaId.isBlank()) {
            try {
                id = Long.valueOf(canchaId.trim());
            } catch (NumberFormatException e) {
                throw ReservaInvalidaException.datos("La cancha no es válida.");
            }
        }
        LocalDate fechaConsulta = null;
        if (fecha != null && !fecha.isBlank()) {
            try {
                fechaConsulta = LocalDate.parse(fecha.trim());
            } catch (DateTimeParseException e) {
                throw ReservaInvalidaException.datos("La fecha debe tener el formato AAAA-MM-DD.");
            }
        }
        return consultar.ejecutar(id, fechaConsulta).stream()
                .map(bloque -> new BloqueDisponible(bloque.canchaId(), bloque.fecha(), bloque.inicio(),
                        bloque.fin(), bloque.ocupado(), bloque.precio()))
                .toList();
    }
}
