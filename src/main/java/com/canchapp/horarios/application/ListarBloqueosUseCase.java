package com.canchapp.horarios.application;

import com.canchapp.horarios.domain.Bloqueo;
import com.canchapp.horarios.domain.BloqueoRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/** Devuelve los bloqueos de hoy en adelante (RF-05). Los pasados ya no afectan las reservas. */
public class ListarBloqueosUseCase {

    private final BloqueoRepository bloqueos;
    private final Clock reloj;

    public ListarBloqueosUseCase(BloqueoRepository bloqueos, Clock reloj) {
        this.bloqueos = bloqueos;
        this.reloj = reloj;
    }

    public List<Bloqueo> ejecutar() {
        return bloqueos.findDesde(LocalDate.now(reloj));
    }
}
