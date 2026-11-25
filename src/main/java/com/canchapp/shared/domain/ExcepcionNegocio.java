package com.canchapp.shared.domain;

/**
 * Excepción base de las reglas de negocio de CanchApp.
 * <p>
 * Lleva un {@code codigo} estable para los clientes y un {@code mensaje} en español
 * para el usuario. La capa web la traduce al cuerpo uniforme {@code {codigo, mensaje}}
 * (AGENTS.md §6), según su {@link Tipo}, sin que el dominio conozca HTTP.
 */
public abstract class ExcepcionNegocio extends RuntimeException {

    /** Clase de error, independiente del protocolo. */
    public enum Tipo {
        DATOS_INVALIDOS,
        NO_AUTENTICADO,
        CONFLICTO
    }

    private final String codigo;
    private final Tipo tipo;

    protected ExcepcionNegocio(String codigo, String mensaje, Tipo tipo) {
        super(mensaje);
        this.codigo = codigo;
        this.tipo = tipo;
    }

    public String getCodigo() {
        return codigo;
    }

    public Tipo getTipo() {
        return tipo;
    }
}
