package com.canchapp.canchas.domain;

/** Puerto que convierte una foto JPG o PNG a WebP (RF-04). */
public interface ConvertidorWebp {

    /** @throws ImagenInvalidaException si los bytes no se pueden leer como imagen */
    byte[] convertir(byte[] imagen);
}
