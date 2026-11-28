package com.canchapp.canchas.domain;

import com.canchapp.shared.domain.ExcepcionNegocio;

/**
 * Ya existe otra cancha con ese nombre. El cliente elige la cancha por su nombre (RF-06), así que
 * dos iguales serían imposibles de distinguir.
 */
public class NombreCanchaRepetidoException extends ExcepcionNegocio {

    public NombreCanchaRepetidoException() {
        super("CANCHA_REPETIDA", "Ya existe una cancha con ese nombre.", Tipo.CONFLICTO);
    }
}
