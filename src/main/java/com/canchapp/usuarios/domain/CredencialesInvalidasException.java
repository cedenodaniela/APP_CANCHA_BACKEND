package com.canchapp.usuarios.domain;

import com.canchapp.shared.domain.ExcepcionNegocio;

/**
 * RF-01: con credenciales inválidas se muestra «Correo o contraseña incorrectos»,
 * sin indicar cuál falló. Se usa también para cuentas inactivas, para no revelar que existen.
 */
public class CredencialesInvalidasException extends ExcepcionNegocio {

    public static final String MENSAJE = "Correo o contraseña incorrectos";

    public CredencialesInvalidasException() {
        super("CREDENCIALES_INVALIDAS", MENSAJE, Tipo.NO_AUTENTICADO);
    }
}
