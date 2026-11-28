package com.canchapp.horarios.domain;

import com.canchapp.shared.domain.ExcepcionNegocio;

/** RF-05: un dato del horario, de una tarifa o de un bloqueo falta o no es válido. */
public class HorarioInvalidoException extends ExcepcionNegocio {

    public HorarioInvalidoException(String mensaje) {
        super("HORARIO_INVALIDO", mensaje, Tipo.DATOS_INVALIDOS);
    }
}
