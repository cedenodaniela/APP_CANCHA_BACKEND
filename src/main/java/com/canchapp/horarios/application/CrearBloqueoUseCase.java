package com.canchapp.horarios.application;

import com.canchapp.horarios.domain.Bloqueo;
import com.canchapp.horarios.domain.BloqueoRepository;
import com.canchapp.horarios.domain.CanchaDelBloqueoNoExisteException;
import com.canchapp.horarios.domain.CanchasDelComplejo;
import com.canchapp.horarios.domain.HoraDelDia;
import com.canchapp.horarios.domain.HorarioInvalidoException;
import com.canchapp.horarios.domain.ReservasActivasEnHorario;
import com.canchapp.horarios.domain.ReservasEnBloqueoException;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Bloquea un rango de horas de una fecha, en una cancha o en todo el complejo (RF-05).
 * <p>
 * Si el rango tiene reservas activas, no guarda: avisa cuántas son y espera que el administrador
 * confirme. Las reservas existentes no se cancelan; el DERCAS no dice qué pasa con ellas.
 *
 * @see "RF-05: si el bloqueo cae sobre una reserva activa, el sistema avisa antes de guardarlo"
 */
public class CrearBloqueoUseCase {

    /** Un bloqueo tal como llega del formulario. {@code canchaId} vacío bloquea todas las canchas. */
    public record DatosBloqueo(Long canchaId, LocalDate fecha, String inicio, String fin, String motivo) {
    }

    private final BloqueoRepository bloqueos;
    private final CanchasDelComplejo canchas;
    private final ReservasActivasEnHorario reservas;
    private final Clock reloj;

    public CrearBloqueoUseCase(BloqueoRepository bloqueos, CanchasDelComplejo canchas,
                               ReservasActivasEnHorario reservas, Clock reloj) {
        this.bloqueos = bloqueos;
        this.canchas = canchas;
        this.reservas = reservas;
        this.reloj = reloj;
    }

    /**
     * @param confirmado el administrador ya vio cuántas reservas activas hay y quiere bloquear igual
     * @throws ReservasEnBloqueoException si hay reservas activas y no se confirmó
     */
    public Bloqueo ejecutar(DatosBloqueo datos, boolean confirmado) {
        if (datos.fecha() == null) {
            throw new HorarioInvalidoException("Elige la fecha del bloqueo.");
        }
        Bloqueo nuevo = Bloqueo.nuevo(datos.canchaId(), datos.fecha(), HoraDelDia.de(datos.inicio(), "la hora de inicio"),
                HoraDelDia.de(datos.fin(), "la hora de fin"), datos.motivo(), LocalDate.now(reloj), reloj.instant());
        if (datos.canchaId() != null && !canchas.existe(datos.canchaId())) {
            throw new CanchaDelBloqueoNoExisteException();
        }
        int activas = reservas.contar(nuevo.canchaId(), nuevo.fecha(), nuevo.inicio(), nuevo.fin());
        if (activas > 0 && !confirmado) {
            throw new ReservasEnBloqueoException(activas);
        }
        return bloqueos.save(nuevo);
    }
}
