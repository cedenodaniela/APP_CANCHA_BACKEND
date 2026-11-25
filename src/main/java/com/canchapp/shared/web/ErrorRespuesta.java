package com.canchapp.shared.web;

/**
 * Cuerpo uniforme de todos los errores de la API (AGENTS.md §6).
 *
 * @param codigo  identificador estable que los clientes pueden interpretar
 * @param mensaje texto en español para mostrar al usuario
 */
public record ErrorRespuesta(String codigo, String mensaje) {
}
