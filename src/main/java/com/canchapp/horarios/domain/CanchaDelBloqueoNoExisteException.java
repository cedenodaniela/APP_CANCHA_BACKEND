package com.canchapp.horarios.domain;

import com.canchapp.shared.domain.ExcepcionNegocio;

/** El bloqueo apunta a una cancha que no existe. */
public class CanchaDelBloqueoNoExisteException extends ExcepcionNegocio {

    public CanchaDelBloqueoNoExisteException() {
        super("HORARIO_INVALIDO", "La cancha elegida no existe.", Tipo.DATOS_INVALIDOS);
    }
}
