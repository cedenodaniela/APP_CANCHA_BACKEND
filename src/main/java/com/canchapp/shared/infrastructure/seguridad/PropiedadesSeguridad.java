package com.canchapp.shared.infrastructure.seguridad;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * Propiedades {@code canchapp.seguridad.*} de {@code application.properties}.
 *
 * @param jwtSecreto    clave HS256 (variable de entorno, nunca en Git); al menos 32 bytes
 * @param jwtVigencia   duración del token; 24 h según RNF-02
 * @param corsOrigenes  orígenes de los clientes que pueden llamar a la API
 */
@ConfigurationProperties(prefix = "canchapp.seguridad")
public record PropiedadesSeguridad(String jwtSecreto, Duration jwtVigencia, List<String> corsOrigenes) {

    /** HS256 exige una clave de al menos 256 bits. */
    public static final int BYTES_MINIMOS_SECRETO = 32;

    public PropiedadesSeguridad {
        if (jwtSecreto == null || jwtSecreto.getBytes(StandardCharsets.UTF_8).length < BYTES_MINIMOS_SECRETO) {
            throw new IllegalStateException(
                    "Define CANCHAPP_JWT_SECRETO con al menos " + BYTES_MINIMOS_SECRETO + " caracteres (ver .env.example).");
        }
        jwtVigencia = jwtVigencia == null ? Duration.ofHours(24) : jwtVigencia;
        corsOrigenes = corsOrigenes == null ? List.of() : List.copyOf(corsOrigenes);
    }
}
