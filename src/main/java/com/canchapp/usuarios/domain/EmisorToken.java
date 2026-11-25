package com.canchapp.usuarios.domain;

import java.time.Instant;

/**
 * Puerto que emite el token de sesión (JWT en infraestructura).
 *
 * @see "RF-01: la sesión se mantiene con un token JWT"
 * @see "RNF-02: el token vence a las 24 h"
 */
public interface EmisorToken {

    /** Token firmado y su momento de vencimiento. */
    record TokenEmitido(String valor, Instant expiraEn) {
    }

    TokenEmitido emitir(Usuario usuario);
}
