package com.canchapp.canchas.infrastructure;

import com.canchapp.canchas.application.CambiarEstadoCanchaUseCase;
import com.canchapp.canchas.application.CrearCanchaUseCase;
import com.canchapp.canchas.application.EditarCanchaUseCase;
import com.canchapp.canchas.application.ListarCanchasUseCase;
import com.canchapp.canchas.application.ObtenerCanchaUseCase;
import com.canchapp.canchas.application.ObtenerFotoCanchaUseCase;
import com.canchapp.canchas.application.SubirFotoCanchaUseCase;
import com.canchapp.canchas.domain.AlmacenamientoImagenes;
import com.canchapp.canchas.domain.CanchaRepository;
import com.canchapp.canchas.domain.ConvertidorWebp;
import com.canchapp.canchas.domain.ReservasActivasDeCancha;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Declara los casos de uso del módulo {@code canchas} como beans de Spring. Los casos de uso son
 * Java puro (AGENTS.md §4): Spring se agrega aquí, en infraestructura.
 */
@Configuration
public class ConfiguracionCanchas {

    @Bean
    public ListarCanchasUseCase listarCanchasUseCase(CanchaRepository canchas) {
        return new ListarCanchasUseCase(canchas);
    }

    @Bean
    public ObtenerCanchaUseCase obtenerCanchaUseCase(CanchaRepository canchas) {
        return new ObtenerCanchaUseCase(canchas);
    }

    @Bean
    public CrearCanchaUseCase crearCanchaUseCase(CanchaRepository canchas, Clock reloj) {
        return new CrearCanchaUseCase(canchas, reloj);
    }

    @Bean
    public EditarCanchaUseCase editarCanchaUseCase(CanchaRepository canchas, Clock reloj) {
        return new EditarCanchaUseCase(canchas, reloj);
    }

    @Bean
    public CambiarEstadoCanchaUseCase cambiarEstadoCanchaUseCase(CanchaRepository canchas,
                                                                 ReservasActivasDeCancha reservas, Clock reloj) {
        return new CambiarEstadoCanchaUseCase(canchas, reservas, reloj);
    }

    @Bean
    public SubirFotoCanchaUseCase subirFotoCanchaUseCase(CanchaRepository canchas, ConvertidorWebp convertidor,
                                                         AlmacenamientoImagenes almacenamiento, Clock reloj) {
        return new SubirFotoCanchaUseCase(canchas, convertidor, almacenamiento, reloj);
    }

    @Bean
    public ObtenerFotoCanchaUseCase obtenerFotoCanchaUseCase(CanchaRepository canchas,
                                                             AlmacenamientoImagenes almacenamiento) {
        return new ObtenerFotoCanchaUseCase(canchas, almacenamiento);
    }

    /**
     * Mientras no exista el módulo {@code reservas} (RF-07), ninguna cancha tiene reservas activas.
     * Cuando ese módulo declare su propio {@link ReservasActivasDeCancha}, este deja de usarse.
     */
    @Bean
    @ConditionalOnMissingBean(ReservasActivasDeCancha.class)
    public ReservasActivasDeCancha sinReservasTodavia() {
        return canchaId -> 0;
    }
}
