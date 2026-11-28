package com.canchapp.shared.infrastructure;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Datos del contrato OpenAPI que publica springdoc en {@code /v3/api-docs} (AGENTS.md §4 y §6).
 * <p>
 * Ese contrato es el que validan APP_CANCHA_MOVIL y APP_CANCHA_WEB: si un DTO cambia, se nota en
 * el Pull Request. Todos los endpoints usan el JWT de RF-01 salvo los públicos de acceso.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "CanchApp · API",
                version = "v1",
                description = "API REST del sistema de reservas del Complejo Deportivo Calceta. "
                        + "Los errores tienen siempre el cuerpo {codigo, mensaje}."),
        security = @SecurityRequirement(name = ConfiguracionOpenApi.ESQUEMA_JWT))
@SecurityScheme(
        name = ConfiguracionOpenApi.ESQUEMA_JWT,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Token de POST /api/v1/auth/login. Vence a las 24 h (RNF-02).")
public class ConfiguracionOpenApi {

    public static final String ESQUEMA_JWT = "jwt";
}
