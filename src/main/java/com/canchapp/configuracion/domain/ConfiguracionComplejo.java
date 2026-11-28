package com.canchapp.configuracion.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Configuración única del complejo (RF-03): sus datos y su política de reservas.
 * La versión 1 tiene un solo complejo (DERCAS §1.2), así que existe una sola configuración.
 *
 * @param actualizadaEn cuándo se guardó por última vez
 */
public record ConfiguracionComplejo(Complejo complejo, PoliticaReserva politica, Instant actualizadaEn) {

    public ConfiguracionComplejo {
        Objects.requireNonNull(complejo, "complejo");
        Objects.requireNonNull(politica, "politica");
    }
}
