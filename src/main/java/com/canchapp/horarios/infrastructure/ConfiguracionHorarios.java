package com.canchapp.horarios.infrastructure;

import com.canchapp.horarios.application.ActualizarHorarioUseCase;
import com.canchapp.horarios.application.ConsultarHorarioUseCase;
import com.canchapp.horarios.application.CrearBloqueoUseCase;
import com.canchapp.horarios.application.EliminarBloqueoUseCase;
import com.canchapp.horarios.application.EliminarTarifaUseCase;
import com.canchapp.horarios.application.GuardarTarifaUseCase;
import com.canchapp.horarios.application.ListarBloqueosUseCase;
import com.canchapp.horarios.application.ListarTarifasUseCase;
import com.canchapp.horarios.domain.BloqueoRepository;
import com.canchapp.horarios.domain.CanchasDelComplejo;
import com.canchapp.horarios.domain.HorarioRepository;
import com.canchapp.horarios.domain.ReservasActivasEnHorario;
import com.canchapp.horarios.domain.TarifaRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Declara los casos de uso del módulo {@code horarios} como beans de Spring. Los casos de uso son
 * Java puro (AGENTS.md §4): Spring se agrega aquí, en infraestructura.
 */
@Configuration
public class ConfiguracionHorarios {

    @Bean
    public ConsultarHorarioUseCase consultarHorarioUseCase(HorarioRepository horarios) {
        return new ConsultarHorarioUseCase(horarios);
    }

    @Bean
    public ActualizarHorarioUseCase actualizarHorarioUseCase(HorarioRepository horarios) {
        return new ActualizarHorarioUseCase(horarios);
    }

    @Bean
    public ListarTarifasUseCase listarTarifasUseCase(TarifaRepository tarifas) {
        return new ListarTarifasUseCase(tarifas);
    }

    @Bean
    public GuardarTarifaUseCase guardarTarifaUseCase(TarifaRepository tarifas) {
        return new GuardarTarifaUseCase(tarifas);
    }

    @Bean
    public EliminarTarifaUseCase eliminarTarifaUseCase(TarifaRepository tarifas) {
        return new EliminarTarifaUseCase(tarifas);
    }

    @Bean
    public ListarBloqueosUseCase listarBloqueosUseCase(BloqueoRepository bloqueos, Clock reloj) {
        return new ListarBloqueosUseCase(bloqueos, reloj);
    }

    @Bean
    public CrearBloqueoUseCase crearBloqueoUseCase(BloqueoRepository bloqueos, CanchasDelComplejo canchas,
                                                   ReservasActivasEnHorario reservas, Clock reloj) {
        return new CrearBloqueoUseCase(bloqueos, canchas, reservas, reloj);
    }

    @Bean
    public EliminarBloqueoUseCase eliminarBloqueoUseCase(BloqueoRepository bloqueos) {
        return new EliminarBloqueoUseCase(bloqueos);
    }

    /**
     * Mientras no exista el módulo {@code reservas} (RF-07), ningún horario tiene reservas activas.
     * Cuando ese módulo declare su propio {@link ReservasActivasEnHorario}, este deja de usarse.
     */
    @Bean
    @ConditionalOnMissingBean(ReservasActivasEnHorario.class)
    public ReservasActivasEnHorario sinReservasEnHorarioTodavia() {
        return (canchaId, fecha, inicio, fin) -> 0;
    }
}
