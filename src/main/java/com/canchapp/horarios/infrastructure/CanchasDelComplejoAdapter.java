package com.canchapp.horarios.infrastructure;

import com.canchapp.canchas.application.ObtenerCanchaUseCase;
import com.canchapp.canchas.domain.CanchaNoEncontradaException;
import com.canchapp.horarios.domain.CanchasDelComplejo;
import org.springframework.stereotype.Component;

/**
 * Comprueba que una cancha existe usando el caso de uso del módulo {@code canchas}: un módulo no
 * toca la infraestructura de otro (AGENTS.md §4). También se puede bloquear una cancha inactiva.
 */
@Component
public class CanchasDelComplejoAdapter implements CanchasDelComplejo {

    private final ObtenerCanchaUseCase obtenerCancha;

    public CanchasDelComplejoAdapter(ObtenerCanchaUseCase obtenerCancha) {
        this.obtenerCancha = obtenerCancha;
    }

    @Override
    public boolean existe(Long canchaId) {
        try {
            obtenerCancha.ejecutar(canchaId, true);
            return true;
        } catch (CanchaNoEncontradaException e) {
            return false;
        }
    }
}
