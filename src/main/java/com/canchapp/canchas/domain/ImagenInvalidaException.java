package com.canchapp.canchas.domain;

import com.canchapp.shared.domain.ExcepcionNegocio;

/** RF-04: la foto no es JPG ni PNG, está vacía, pesa más de 5 MB o no se puede leer. */
public class ImagenInvalidaException extends ExcepcionNegocio {

    public ImagenInvalidaException(String mensaje) {
        super("IMAGEN_INVALIDA", mensaje, Tipo.DATOS_INVALIDOS);
    }
}
