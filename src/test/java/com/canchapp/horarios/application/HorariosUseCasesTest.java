package com.canchapp.horarios.application;

import com.canchapp.horarios.application.ActualizarHorarioUseCase.DatosDia;
import com.canchapp.horarios.application.CrearBloqueoUseCase.DatosBloqueo;
import com.canchapp.horarios.application.GuardarTarifaUseCase.DatosTarifa;
import com.canchapp.horarios.domain.Bloqueo;
import com.canchapp.horarios.domain.BloqueoRepository;
import com.canchapp.horarios.domain.CanchaDelBloqueoNoExisteException;
import com.canchapp.horarios.domain.CanchasDelComplejo;
import com.canchapp.horarios.domain.DiasSemana;
import com.canchapp.horarios.domain.HoraDelDia;
import com.canchapp.horarios.domain.HorarioInvalidoException;
import com.canchapp.horarios.domain.HorarioRepository;
import com.canchapp.horarios.domain.HorarioSemanal;
import com.canchapp.horarios.domain.ReservasActivasEnHorario;
import com.canchapp.horarios.domain.ReservasEnBloqueoException;
import com.canchapp.horarios.domain.Tarifa;
import com.canchapp.horarios.domain.TarifaRepository;
import com.canchapp.horarios.domain.TarifaSuperpuestaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Horarios, tarifas y bloqueos · casos de uso (RF-05)")
class HorariosUseCasesTest {

    /** Sábado 28 de noviembre de 2026, 10:00 en Guayaquil. */
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-11-28T15:00:00Z"), ZoneId.of("America/Guayaquil"));
    private static final LocalDate HOY = LocalDate.of(2026, 11, 28);

    private TarifaRepository tarifas;
    private BloqueoRepository bloqueos;
    private CanchasDelComplejo canchas;
    private ReservasActivasEnHorario reservas;

    @BeforeEach
    void preparar() {
        tarifas = mock(TarifaRepository.class);
        bloqueos = mock(BloqueoRepository.class);
        canchas = mock(CanchasDelComplejo.class);
        reservas = mock(ReservasActivasEnHorario.class);
        when(tarifas.save(any())).thenAnswer(inv -> ((Tarifa) inv.getArgument(0)).conId(9L));
        when(bloqueos.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private static Tarifa tarifa(Long id, String inicio, String fin) {
        return Tarifa.de(id, EnumSet.allOf(DayOfWeek.class), HoraDelDia.de(inicio, "x"), HoraDelDia.de(fin, "x"),
                new BigDecimal("20.00"));
    }

    @Test
    @DisplayName("dada la semana completa, cuando se actualiza el horario, entonces se guardan los 7 días y el domingo puede cerrar")
    void actualizarHorario() {
        HorarioRepository horarios = mock(HorarioRepository.class);
        when(horarios.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        List<DatosDia> dias = Arrays.stream(DayOfWeek.values())
                .map(d -> d == DayOfWeek.SUNDAY ? new DatosDia("DOMINGO", false, null, null)
                        : new DatosDia(DiasSemana.nombre(d), true, "08:00", "22:30"))
                .toList();

        HorarioSemanal guardado = new ActualizarHorarioUseCase(horarios).ejecutar(dias);

        assertThat(guardado.del(DayOfWeek.SUNDAY).abierto()).isFalse();
        assertThat(guardado.del(DayOfWeek.MONDAY).cierre().texto()).isEqualTo("22:30");
    }

    @Test
    @DisplayName("dada una tarifa que se cruza con otra, cuando se crea, entonces se rechaza sin guardar")
    void tarifaSuperpuesta() {
        when(tarifas.findAll()).thenReturn(List.of(tarifa(1L, "10:00", "18:00")));
        GuardarTarifaUseCase guardar = new GuardarTarifaUseCase(tarifas);

        assertThatThrownBy(() -> guardar.ejecutar(null, new DatosTarifa(List.of("VIERNES"), "17:00", "20:00", new BigDecimal("22"))))
                .isInstanceOf(TarifaSuperpuestaException.class);
        verify(tarifas, never()).save(any());
    }

    @Test
    @DisplayName("dada una tarifa existente, cuando se edita, entonces no choca consigo misma")
    void editarTarifa() {
        when(tarifas.findById(1L)).thenReturn(Optional.of(tarifa(1L, "10:00", "18:00")));
        when(tarifas.findAll()).thenReturn(List.of(tarifa(1L, "10:00", "18:00"), tarifa(2L, "18:00", "24:00")));

        Tarifa editada = new GuardarTarifaUseCase(tarifas)
                .ejecutar(1L, new DatosTarifa(List.of("LUNES"), "10:00", "18:00", new BigDecimal("21.50")));

        assertThat(editada.getPrecioHora()).isEqualByComparingTo("21.50");
    }

    @Test
    @DisplayName("dado un horario con reservas activas, cuando se bloquea sin confirmar, entonces avisa cuántas hay y no guarda")
    void bloqueoConReservas() {
        when(canchas.existe(1L)).thenReturn(true);
        when(reservas.contar(any(), any(), any(), any())).thenReturn(3);
        CrearBloqueoUseCase crear = new CrearBloqueoUseCase(bloqueos, canchas, reservas, RELOJ);
        DatosBloqueo datos = new DatosBloqueo(1L, HOY.plusDays(1), "10:00", "14:00", "Mantenimiento");

        assertThatThrownBy(() -> crear.ejecutar(datos, false))
                .isInstanceOf(ReservasEnBloqueoException.class)
                .hasMessage("Ese horario tiene 3 reservas activas. Confirma si quieres bloquearlo de todos modos.");
        verify(bloqueos, never()).save(any());

        Bloqueo guardado = crear.ejecutar(datos, true);
        assertThat(guardado.motivo()).isEqualTo("Mantenimiento");
    }

    @Test
    @DisplayName("dado un bloqueo de todo el complejo, cuando se crea, entonces no pregunta por ninguna cancha")
    void bloqueoDelComplejo() {
        Bloqueo guardado = new CrearBloqueoUseCase(bloqueos, canchas, reservas, RELOJ)
                .ejecutar(new DatosBloqueo(null, HOY, "10:00", "24:00", "Feriado"), false);

        assertThat(guardado.esDeTodoElComplejo()).isTrue();
        verify(canchas, never()).existe(anyLong());
    }

    @Test
    @DisplayName("dado un bloqueo de una cancha que no existe, cuando se crea, entonces se rechaza")
    void bloqueoCanchaInexistente() {
        when(canchas.existe(9L)).thenReturn(false);

        assertThatThrownBy(() -> new CrearBloqueoUseCase(bloqueos, canchas, reservas, RELOJ)
                .ejecutar(new DatosBloqueo(9L, HOY, "10:00", "12:00", "Mantenimiento"), false))
                .isInstanceOf(CanchaDelBloqueoNoExisteException.class);
    }

    @Test
    @DisplayName("dado un bloqueo sin fecha, cuando se crea, entonces lo pide")
    void bloqueoSinFecha() {
        assertThatThrownBy(() -> new CrearBloqueoUseCase(bloqueos, canchas, reservas, RELOJ)
                .ejecutar(new DatosBloqueo(null, null, "10:00", "12:00", "Feriado"), false))
                .isInstanceOf(HorarioInvalidoException.class)
                .hasMessage("Elige la fecha del bloqueo.");
    }

    @Test
    @DisplayName("dados bloqueos guardados, cuando se listan, entonces se piden desde hoy")
    void listarDesdeHoy() {
        new ListarBloqueosUseCase(bloqueos, RELOJ).ejecutar();

        verify(bloqueos).findDesde(HOY);
    }
}
