package com.canchapp.horarios.domain;

import com.canchapp.shared.domain.ExcepcionNegocio;

/** El bloqueo pedido no existe (404). */
public class BloqueoNoEncontradoException extends ExcepcionNegocio {

    public BloqueoNoEncontradoException() {
        super("BLOQUEO_NO_ENCONTRADO", "El bloqueo no existe.", Tipo.NO_ENCONTRADO);
    }
}
