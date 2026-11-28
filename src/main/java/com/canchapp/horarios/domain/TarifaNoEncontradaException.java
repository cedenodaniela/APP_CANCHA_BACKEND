package com.canchapp.horarios.domain;

import com.canchapp.shared.domain.ExcepcionNegocio;

/** La tarifa pedida no existe (404). */
public class TarifaNoEncontradaException extends ExcepcionNegocio {

    public TarifaNoEncontradaException() {
        super("TARIFA_NO_ENCONTRADA", "La tarifa no existe.", Tipo.NO_ENCONTRADO);
    }
}
