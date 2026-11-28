package com.canchapp.reservas.infrastructure;

import com.canchapp.canchas.domain.CanchaRepository;
import com.canchapp.configuracion.domain.ConfiguracionRepository;
import com.canchapp.horarios.domain.BloqueoRepository;
import com.canchapp.horarios.domain.HorarioRepository;
import com.canchapp.horarios.domain.ReservasActivasEnHorario;
import com.canchapp.horarios.domain.TarifaRepository;
import com.canchapp.reservas.application.ConsultarDisponibilidadUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Declara los casos de uso del módulo {@code reservas} como beans de Spring. Los casos de uso son
 * Java puro (AGENTS.md §4): Spring se agrega aquí, en infraestructura.
 */
@Configuration
public class ConfiguracionReservas {

    @Bean
    public ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase(HorarioRepository horarios,
                                                                           TarifaRepository tarifas,
                                                                           BloqueoRepository bloqueos,
                                                                           ReservasActivasEnHorario reservas,
                                                                           CanchaRepository canchas,
                                                                           ConfiguracionRepository configuracion,
                                                                           Clock reloj) {
        return new ConsultarDisponibilidadUseCase(horarios, tarifas, bloqueos, reservas, canchas, configuracion,
                reloj);
    }
}
