package com.canchapp.horarios.application;

import com.canchapp.horarios.domain.TarifaNoEncontradaException;
import com.canchapp.horarios.domain.TarifaRepository;

/** Borra una tarifa (RF-05). Las reservas ya creadas conservan el total que se calculó al reservar. */
public class EliminarTarifaUseCase {

    private final TarifaRepository tarifas;

    public EliminarTarifaUseCase(TarifaRepository tarifas) {
        this.tarifas = tarifas;
    }

    /** @throws TarifaNoEncontradaException si no existe */
    public void ejecutar(Long id) {
        if (tarifas.findById(id).isEmpty()) {
            throw new TarifaNoEncontradaException();
        }
        tarifas.delete(id);
    }
}
