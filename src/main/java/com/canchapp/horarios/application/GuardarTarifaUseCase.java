package com.canchapp.horarios.application;

import com.canchapp.horarios.domain.DiasSemana;
import com.canchapp.horarios.domain.HoraDelDia;
import com.canchapp.horarios.domain.Tarifa;
import com.canchapp.horarios.domain.TarifaNoEncontradaException;
import com.canchapp.horarios.domain.TarifaRepository;
import com.canchapp.horarios.domain.TarifaSuperpuestaException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Crea o edita una tarifa (RF-05) sin dejar dos tarifas que se superpongan: así cada bloque tiene
 * un solo precio (RN-03).
 *
 * @see "RF-05: no se pueden guardar dos tarifas que se superpongan"
 */
public class GuardarTarifaUseCase {

    /** Una tarifa tal como llega del formulario. Las horas son «HH:mm». */
    public record DatosTarifa(List<String> dias, String inicio, String fin, BigDecimal precioHora) {
    }

    private final TarifaRepository tarifas;

    public GuardarTarifaUseCase(TarifaRepository tarifas) {
        this.tarifas = tarifas;
    }

    /**
     * @param id {@code null} para crear una tarifa nueva
     * @throws TarifaSuperpuestaException si se cruza con otra tarifa
     * @throws TarifaNoEncontradaException si se edita una tarifa que no existe
     */
    public Tarifa ejecutar(Long id, DatosTarifa datos) {
        if (id != null && tarifas.findById(id).isEmpty()) {
            throw new TarifaNoEncontradaException();
        }
        Tarifa nueva = Tarifa.de(id, DiasSemana.dias(datos.dias()), HoraDelDia.de(datos.inicio(), "la hora de inicio"),
                HoraDelDia.de(datos.fin(), "la hora de fin"), datos.precioHora());
        for (Tarifa otra : tarifas.findAll()) {
            if (!Objects.equals(otra.getId(), id) && nueva.seSuperponeCon(otra)) {
                throw new TarifaSuperpuestaException(otra);
            }
        }
        return tarifas.save(nueva);
    }
}
