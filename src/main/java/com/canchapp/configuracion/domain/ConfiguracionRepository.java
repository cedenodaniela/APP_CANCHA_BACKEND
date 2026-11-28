package com.canchapp.configuracion.domain;

/**
 * Puerto de persistencia de la configuración única del complejo (RF-03).
 * La migración V3 deja sembrada la fila inicial, así que siempre existe.
 */
public interface ConfiguracionRepository {

    ConfiguracionComplejo obtener();

    ConfiguracionComplejo guardar(ConfiguracionComplejo configuracion);
}
