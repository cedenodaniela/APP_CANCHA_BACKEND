package com.canchapp.canchas.domain;

import com.canchapp.shared.domain.ExcepcionNegocio;

/** La cancha pedida no existe (404). */
public class CanchaNoEncontradaException extends ExcepcionNegocio {

    public CanchaNoEncontradaException() {
        super("CANCHA_NO_ENCONTRADA", "La cancha no existe.", Tipo.NO_ENCONTRADO);
    }
}
