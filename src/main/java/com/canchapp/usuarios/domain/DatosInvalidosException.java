package com.canchapp.usuarios.domain;

import com.canchapp.shared.domain.ExcepcionNegocio;

/** Los datos de la cuenta no cumplen las reglas de RF-01. */
public class DatosInvalidosException extends ExcepcionNegocio {

    public DatosInvalidosException(String mensaje) {
        super("DATOS_INVALIDOS", mensaje, Tipo.DATOS_INVALIDOS);
    }
}
