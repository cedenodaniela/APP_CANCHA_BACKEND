package com.canchapp.usuarios.domain;

import com.canchapp.shared.domain.ExcepcionNegocio;

/**
 * RF-01: «un correo ya registrado muestra un mensaje de error».
 */
public class CorreoYaRegistradoException extends ExcepcionNegocio {

    public CorreoYaRegistradoException() {
        super("CORREO_YA_REGISTRADO", "Ya existe una cuenta con ese correo.", Tipo.CONFLICTO);
    }
}
