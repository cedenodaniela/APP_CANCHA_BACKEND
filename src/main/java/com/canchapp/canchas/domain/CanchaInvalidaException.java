package com.canchapp.canchas.domain;

import com.canchapp.shared.domain.ExcepcionNegocio;

/** RF-04: falta un dato de la cancha o una medida no es válida. */
public class CanchaInvalidaException extends ExcepcionNegocio {

    public CanchaInvalidaException(String mensaje) {
        super("CANCHA_INVALIDA", mensaje, Tipo.DATOS_INVALIDOS);
    }
}
