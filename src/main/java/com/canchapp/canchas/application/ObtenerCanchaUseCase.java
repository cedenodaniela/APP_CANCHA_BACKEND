package com.canchapp.canchas.application;

import com.canchapp.canchas.domain.Cancha;
import com.canchapp.canchas.domain.CanchaNoEncontradaException;
import com.canchapp.canchas.domain.CanchaRepository;

/** Devuelve una cancha (RF-04). Un cliente no ve las inactivas: para él no existen. */
public class ObtenerCanchaUseCase {

    private final CanchaRepository canchas;

    public ObtenerCanchaUseCase(CanchaRepository canchas) {
        this.canchas = canchas;
    }

    /** @throws CanchaNoEncontradaException si no existe, o si está inactiva y no se incluyen */
    public Cancha ejecutar(Long id, boolean incluirInactivas) {
        return canchas.findById(id)
                .filter(c -> incluirInactivas || c.isActiva())
                .orElseThrow(CanchaNoEncontradaException::new);
    }
}
