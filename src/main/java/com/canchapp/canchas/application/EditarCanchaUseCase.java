package com.canchapp.canchas.application;

import com.canchapp.canchas.application.CrearCanchaUseCase.DatosCancha;
import com.canchapp.canchas.domain.Cancha;
import com.canchapp.canchas.domain.CanchaNoEncontradaException;
import com.canchapp.canchas.domain.CanchaRepository;
import com.canchapp.canchas.domain.NombreCanchaRepetidoException;

import java.time.Clock;

/** Edición de los datos de una cancha (RF-04). La foto y el estado se cambian aparte. */
public class EditarCanchaUseCase {

    private final CanchaRepository canchas;
    private final Clock reloj;

    public EditarCanchaUseCase(CanchaRepository canchas, Clock reloj) {
        this.canchas = canchas;
        this.reloj = reloj;
    }

    /**
     * @throws CanchaNoEncontradaException si la cancha no existe
     * @throws NombreCanchaRepetidoException si otra cancha ya usa ese nombre
     */
    public Cancha ejecutar(Long id, DatosCancha datos) {
        Cancha actual = canchas.findById(id).orElseThrow(CanchaNoEncontradaException::new);
        Cancha editada = actual.editar(datos.nombre(), datos.tipoCesped(), datos.anchoMetros(), datos.largoMetros(),
                reloj.instant());
        if (canchas.existeNombre(editada.getNombre(), id)) {
            throw new NombreCanchaRepetidoException();
        }
        return canchas.save(editada);
    }
}
