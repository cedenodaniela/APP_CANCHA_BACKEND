package com.canchapp.horarios.application;

import com.canchapp.horarios.domain.BloqueoNoEncontradoException;
import com.canchapp.horarios.domain.BloqueoRepository;

/** Quita un bloqueo (RF-05): esos bloques vuelven a estar disponibles. */
public class EliminarBloqueoUseCase {

    private final BloqueoRepository bloqueos;

    public EliminarBloqueoUseCase(BloqueoRepository bloqueos) {
        this.bloqueos = bloqueos;
    }

    /** @throws BloqueoNoEncontradoException si no existe */
    public void ejecutar(Long id) {
        if (bloqueos.findById(id).isEmpty()) {
            throw new BloqueoNoEncontradoException();
        }
        bloqueos.delete(id);
    }
}
