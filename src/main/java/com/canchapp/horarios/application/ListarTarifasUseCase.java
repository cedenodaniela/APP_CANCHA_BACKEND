package com.canchapp.horarios.application;

import com.canchapp.horarios.domain.Tarifa;
import com.canchapp.horarios.domain.TarifaRepository;

import java.util.List;

/** Devuelve las tarifas por franja (RF-05). */
public class ListarTarifasUseCase {

    private final TarifaRepository tarifas;

    public ListarTarifasUseCase(TarifaRepository tarifas) {
        this.tarifas = tarifas;
    }

    public List<Tarifa> ejecutar() {
        return tarifas.findAll();
    }
}
