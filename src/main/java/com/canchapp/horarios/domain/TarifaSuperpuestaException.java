package com.canchapp.horarios.domain;

import com.canchapp.shared.domain.ExcepcionNegocio;

/** RF-05: «no se pueden guardar dos tarifas que se superpongan». */
public class TarifaSuperpuestaException extends ExcepcionNegocio {

    public TarifaSuperpuestaException(Tarifa otra) {
        super("TARIFA_SUPERPUESTA", "Se cruza con la tarifa de " + otra.getInicio() + " a " + otra.getFin()
                + " de $" + otra.getPrecioHora() + " por hora. Cambia los días o las horas.", Tipo.CONFLICTO);
    }
}
