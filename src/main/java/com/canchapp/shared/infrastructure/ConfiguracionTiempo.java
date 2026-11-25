package com.canchapp.shared.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Reloj único del sistema en la zona del complejo (AGENTS.md §6).
 * <p>
 * Los casos de uso reciben este {@link Clock} en lugar de llamar a {@code Instant.now()},
 * para que las pruebas puedan fijar la hora.
 */
@Configuration
public class ConfiguracionTiempo {

    public static final ZoneId ZONA_COMPLEJO = ZoneId.of("America/Guayaquil");

    @Bean
    public Clock reloj() {
        return Clock.system(ZONA_COMPLEJO);
    }
}
