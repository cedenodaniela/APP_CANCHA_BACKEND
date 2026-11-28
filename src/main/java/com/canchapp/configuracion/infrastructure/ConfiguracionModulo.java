package com.canchapp.configuracion.infrastructure;

import com.canchapp.configuracion.application.ActualizarConfiguracionUseCase;
import com.canchapp.configuracion.application.ConsultarConfiguracionUseCase;
import com.canchapp.configuracion.domain.ConfiguracionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Declara los casos de uso del módulo {@code configuracion} como beans de Spring. Los casos de
 * uso son Java puro (AGENTS.md §4): Spring se agrega aquí, en infraestructura.
 */
@Configuration
public class ConfiguracionModulo {

    @Bean
    public ConsultarConfiguracionUseCase consultarConfiguracionUseCase(ConfiguracionRepository configuraciones) {
        return new ConsultarConfiguracionUseCase(configuraciones);
    }

    @Bean
    public ActualizarConfiguracionUseCase actualizarConfiguracionUseCase(ConfiguracionRepository configuraciones,
                                                                         Clock reloj) {
        return new ActualizarConfiguracionUseCase(configuraciones, reloj);
    }
}
