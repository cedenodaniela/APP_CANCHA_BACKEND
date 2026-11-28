package com.canchapp.reservas.domain;

import com.canchapp.shared.domain.ExcepcionNegocio;

/**
 * RN-11: una reserva EN_VERIFICACION no se puede cancelar. El caso de uso de cancelación (RF-12)
 * además avisa al administrador para que verifique el pago; el cliente debe esperar.
 */
public class ReservaEnVerificacionException extends ExcepcionNegocio {

    public ReservaEnVerificacionException() {
        super("RESERVA_EN_VERIFICACION",
                "Tu pago está en verificación y la reserva no se puede cancelar todavía. Avisamos al administrador para que lo revise.",
                Tipo.CONFLICTO);
    }
}
