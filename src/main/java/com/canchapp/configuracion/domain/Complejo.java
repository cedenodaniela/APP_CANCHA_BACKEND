package com.canchapp.configuracion.domain;

import java.util.Locale;

/**
 * Datos generales y bancarios del complejo (RF-03). Los datos bancarios se muestran al cliente
 * cuando paga por transferencia (RF-08).
 *
 * @param tipoCuenta tipo de la cuenta bancaria
 */
public record Complejo(String nombre, String direccion, String telefono,
                       String banco, TipoCuenta tipoCuenta, String numeroCuenta, String titularCuenta) {

    /** Valida y limpia los datos: ningún campo puede quedar vacío. */
    public Complejo {
        nombre = requerido(nombre, "el nombre del complejo", 120);
        direccion = requerido(direccion, "la dirección", 200);
        telefono = requerido(telefono, "el teléfono", 20);
        banco = requerido(banco, "el banco", 100);
        if (tipoCuenta == null) {
            throw new ConfiguracionInvalidaException("Elige el tipo de cuenta: ahorros o corriente.");
        }
        numeroCuenta = requerido(numeroCuenta, "el número de cuenta", 30);
        titularCuenta = requerido(titularCuenta, "el titular de la cuenta", 120);
    }

    /**
     * Convierte el texto que envían los clientes en {@link TipoCuenta}.
     *
     * @throws ConfiguracionInvalidaException si no es «AHORROS» ni «CORRIENTE»
     */
    public static TipoCuenta tipoCuenta(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return TipoCuenta.valueOf(texto.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ConfiguracionInvalidaException("Elige el tipo de cuenta: ahorros o corriente.");
        }
    }

    private static String requerido(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) {
            throw new ConfiguracionInvalidaException("Ingresa " + campo + ".");
        }
        String limpio = valor.trim();
        if (limpio.length() > maximo) {
            throw new ConfiguracionInvalidaException(
                    "El campo " + campo + " admite hasta " + maximo + " caracteres.");
        }
        return limpio;
    }
}
