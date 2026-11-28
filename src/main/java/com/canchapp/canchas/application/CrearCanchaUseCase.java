package com.canchapp.canchas.application;

import com.canchapp.canchas.domain.Cancha;
import com.canchapp.canchas.domain.CanchaRepository;
import com.canchapp.canchas.domain.NombreCanchaRepetidoException;

import java.time.Clock;

/** Alta de una cancha (RF-04). Nace activa y sin foto; la foto se sube aparte. */
public class CrearCanchaUseCase {

    /** Datos del formulario de la cancha, sin validar todavía. */
    public record DatosCancha(String nombre, String tipoCesped, Integer anchoMetros, Integer largoMetros) {
    }

    private final CanchaRepository canchas;
    private final Clock reloj;

    public CrearCanchaUseCase(CanchaRepository canchas, Clock reloj) {
        this.canchas = canchas;
        this.reloj = reloj;
    }

    /**
     * @throws com.canchapp.canchas.domain.CanchaInvalidaException si falta un dato o una medida no es válida
     * @throws NombreCanchaRepetidoException si otra cancha ya usa ese nombre
     */
    public Cancha ejecutar(DatosCancha datos) {
        Cancha nueva = Cancha.nueva(datos.nombre(), datos.tipoCesped(), datos.anchoMetros(), datos.largoMetros(),
                reloj.instant());
        if (canchas.existeNombre(nueva.getNombre(), null)) {
            throw new NombreCanchaRepetidoException();
        }
        return canchas.save(nueva);
    }
}
