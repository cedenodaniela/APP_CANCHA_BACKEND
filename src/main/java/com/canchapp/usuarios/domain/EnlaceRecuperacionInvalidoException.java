package com.canchapp.usuarios.domain;

import com.canchapp.shared.domain.ExcepcionNegocio;

/**
 * RF-02: el enlace de recuperación no existe, ya se usó o ya venció. Los tres casos dan el
 * mismo mensaje, para no revelar cuál fue.
 */
public class EnlaceRecuperacionInvalidoException extends ExcepcionNegocio {

    public EnlaceRecuperacionInvalidoException() {
        super("ENLACE_INVALIDO", "El enlace no es válido o ya venció. Solicita uno nuevo.", Tipo.DATOS_INVALIDOS);
    }
}
