package com.canchapp.usuarios.domain;

/** Puerto que genera tokens aleatorios e impredecibles para los enlaces de recuperación (RF-02). */
public interface GeneradorToken {

    /** Devuelve un token nuevo, seguro para ir en una URL. */
    String generar();
}
