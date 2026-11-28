package com.canchapp.reservas.domain;

/** Formas de pago de una reserva (AGENTS.md §5). No hay pagos con tarjeta (DERCAS §1.2). */
public enum MetodoPago {
    /** El cliente sube el comprobante y el administrador lo verifica (RN-05, RN-06). */
    TRANSFERENCIA,
    /** Paga en el complejo; como máximo una reserva activa así por cliente (RN-07). */
    EFECTIVO,
    /** Se paga con el saldo a favor de cancelaciones anteriores (RN-10). */
    SALDO_FAVOR
}
