package com.canchapp.canchas.application;

import com.canchapp.canchas.domain.Cancha;
import com.canchapp.canchas.domain.CanchaNoEncontradaException;
import com.canchapp.canchas.domain.CanchaRepository;
import com.canchapp.canchas.domain.ReservasActivasDeCancha;
import com.canchapp.canchas.domain.ReservasActivasException;

import java.time.Clock;

/**
 * Activa o desactiva una cancha (RF-04).
 * <p>
 * Si al desactivarla tiene reservas activas, no la desactiva: avisa cuántas son y espera que el
 * administrador confirme. Las reservas existentes no se tocan; el DERCAS no dice qué pasa con ellas.
 *
 * @see "RF-04: al desactivar una cancha con reservas activas, el sistema muestra cuántas reservas hay y pide confirmación"
 */
public class CambiarEstadoCanchaUseCase {

    private final CanchaRepository canchas;
    private final ReservasActivasDeCancha reservas;
    private final Clock reloj;

    public CambiarEstadoCanchaUseCase(CanchaRepository canchas, ReservasActivasDeCancha reservas, Clock reloj) {
        this.canchas = canchas;
        this.reservas = reservas;
        this.reloj = reloj;
    }

    /**
     * @param confirmado el administrador ya vio cuántas reservas activas hay y quiere seguir
     * @throws ReservasActivasException si se desactiva sin confirmar y hay reservas activas
     */
    public Cancha ejecutar(Long id, boolean activa, boolean confirmado) {
        Cancha actual = canchas.findById(id).orElseThrow(CanchaNoEncontradaException::new);
        if (actual.isActiva() == activa) {
            return actual;
        }
        if (activa) {
            return canchas.save(actual.activar(reloj.instant()));
        }
        int activas = reservas.contar(id);
        if (activas > 0 && !confirmado) {
            throw new ReservasActivasException(activas);
        }
        return canchas.save(actual.desactivar(reloj.instant()));
    }
}
