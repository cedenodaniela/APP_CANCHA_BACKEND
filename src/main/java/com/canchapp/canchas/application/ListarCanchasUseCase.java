package com.canchapp.canchas.application;

import com.canchapp.canchas.domain.Cancha;
import com.canchapp.canchas.domain.CanchaRepository;

import java.util.List;

/**
 * Lista las canchas (RF-04). El administrador ve todas para gestionarlas; el cliente solo las
 * activas, porque una cancha inactiva no se ofrece (RF-04, RF-06).
 */
public class ListarCanchasUseCase {

    private final CanchaRepository canchas;

    public ListarCanchasUseCase(CanchaRepository canchas) {
        this.canchas = canchas;
    }

    public List<Cancha> ejecutar(boolean incluirInactivas) {
        return incluirInactivas ? canchas.findAll() : canchas.findActivas();
    }
}
