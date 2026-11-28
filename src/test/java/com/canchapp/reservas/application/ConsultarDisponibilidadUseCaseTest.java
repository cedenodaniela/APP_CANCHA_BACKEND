package com.canchapp.reservas.application;

import com.canchapp.canchas.domain.Cancha;
import com.canchapp.canchas.domain.CanchaNoEncontradaException;
import com.canchapp.canchas.domain.CanchaRepository;
import com.canchapp.configuracion.domain.Complejo;
import com.canchapp.configuracion.domain.ConfiguracionComplejo;
import com.canchapp.configuracion.domain.ConfiguracionRepository;
import com.canchapp.configuracion.domain.PoliticaReserva;
import com.canchapp.configuracion.domain.TipoCuenta;
import com.canchapp.horarios.domain.Bloqueo;
import com.canchapp.horarios.domain.BloqueoRepository;
import com.canchapp.horarios.domain.HoraDelDia;
import com.canchapp.horarios.domain.HorarioDia;
import com.canchapp.horarios.domain.HorarioRepository;
import com.canchapp.horarios.domain.HorarioSemanal;
import com.canchapp.horarios.domain.ReservasActivasEnHorario;
import com.canchapp.horarios.domain.Tarifa;
import com.canchapp.horarios.domain.TarifaRepository;
import com.canchapp.reservas.domain.ReservaInvalidaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Reservas · casos de uso (RF-06)")
class ConsultarDisponibilidadUseCaseTest {

    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-11-28T15:00:00Z"), ZoneId.of("America/Guayaquil"));
    private static final LocalDate HOY = LocalDate.of(2026, 11, 28);
    private static final Instant AHORA = Instant.parse("2026-11-28T15:00:00Z");

    private HorarioRepository horarios;
    private TarifaRepository tarifas;
    private BloqueoRepository bloqueos;
    private ReservasActivasEnHorario reservas;
    private CanchaRepository canchas;
    private ConfiguracionRepository configuracion;

    @BeforeEach
    void preparar() {
        horarios = mock(HorarioRepository.class);
        tarifas = mock(TarifaRepository.class);
        bloqueos = mock(BloqueoRepository.class);
        reservas = mock(ReservasActivasEnHorario.class);
        canchas = mock(CanchaRepository.class);
        configuracion = mock(ConfiguracionRepository.class);
        when(configuracion.obtener()).thenReturn(new ConfiguracionComplejo(
                new Complejo("Complejo Deportivo Calceta", "Calceta", "0969908865", "Banco Pichincha", TipoCuenta.AHORROS,
                        "2201234567", "Complejo Deportivo Calceta"),
                new PoliticaReserva(15, 3, 1, 3, true), Instant.now()));
    }

    private static HorarioSemanal semanaAbierta() {
        return HorarioSemanal.de(Arrays.stream(java.time.DayOfWeek.values())
                .map(d -> new HorarioDia(d, true, HoraDelDia.de("10:00", "la hora"), HoraDelDia.de("24:00", "la hora")))
                .toList());
    }

    private static Tarifa tarifa() {
        return Tarifa.de(1L, EnumSet.allOf(java.time.DayOfWeek.class), HoraDelDia.de("10:00", "la hora"),
                HoraDelDia.de("24:00", "la hora"), new BigDecimal("20"));
    }

    private static Tarifa tarifa(String inicio, String fin, BigDecimal precioHora) {
        return Tarifa.de(2L, EnumSet.allOf(java.time.DayOfWeek.class), HoraDelDia.de(inicio, "la hora"),
                HoraDelDia.de(fin, "la hora"), precioHora);
    }

    @Test
    @DisplayName("dada una cancha y una fecha dentro de la anticipación, cuando se consulta, entonces los bloques del día salen libres con su precio (RF-06)")
    void consultaDisponibilidad() {
        when(horarios.obtener()).thenReturn(semanaAbierta());
        when(tarifas.findAll()).thenReturn(List.of(tarifa()));
        when(bloqueos.findDesde(HOY)).thenReturn(List.of());
        when(reservas.contar(any(), any(), any(), any())).thenReturn(0);
        when(canchas.findById(1L)).thenReturn(Optional.of(cancha(true, null)));

        List<ConsultarDisponibilidadUseCase.BloqueDisponible> bloques = new ConsultarDisponibilidadUseCase(
                horarios, tarifas, bloqueos, reservas, canchas, configuracion, RELOJ).ejecutar(1L, HOY);

        // 10:00 a 24:00 en bloques de 30 minutos = 28 bloques.
        assertThat(bloques).hasSize(28);
        assertThat(bloques.get(0).canchaId()).isEqualTo(1L);
        assertThat(bloques.get(0).inicio()).isEqualTo("10:00");
        assertThat(bloques.get(0).fin()).isEqualTo("10:30");
        assertThat(bloques.get(0).ocupado()).isFalse();
        assertThat(bloques.get(0).precio()).isEqualByComparingTo("10.00");
        assertThat(bloques.get(27).fin()).isEqualTo("24:00");
    }

    @Test
    @DisplayName("dado un bloqueo del día, cuando se consulta, entonces ese bloque sale ocupado (RF-06 / RF-05)")
    void consultaConBloqueo() {
        when(horarios.obtener()).thenReturn(semanaAbierta());
        when(tarifas.findAll()).thenReturn(List.of(tarifa()));
        when(bloqueos.findDesde(HOY)).thenReturn(List.of(
                new Bloqueo(5L, 1L, HOY, HoraDelDia.de("10:00", "la hora"), HoraDelDia.de("12:00", "la hora"),
                        "Mantenimiento", null)));
        when(reservas.contar(any(), any(), any(), any())).thenReturn(0);
        when(canchas.findById(1L)).thenReturn(Optional.of(cancha(true, null)));

        List<ConsultarDisponibilidadUseCase.BloqueDisponible> bloques = new ConsultarDisponibilidadUseCase(
                horarios, tarifas, bloqueos, reservas, canchas, configuracion, RELOJ).ejecutar(1L, HOY);

        assertThat(bloques.get(0).ocupado()).isTrue();
        assertThat(bloques.get(0).precio()).isEqualByComparingTo("10.00");
        assertThat(bloques.get(3).inicio()).isEqualTo("11:30");
        assertThat(bloques.get(3).ocupado()).isTrue();
        assertThat(bloques.get(4).inicio()).isEqualTo("12:00");
        assertThat(bloques.get(4).ocupado()).isFalse();
    }

    @Test
    @DisplayName("dada una reserva activa, cuando se consulta, entonces el bloque que ocupa sale ocupado (RF-06 / RF-07)")
    void consultaConReservaActiva() {
        when(horarios.obtener()).thenReturn(semanaAbierta());
        when(tarifas.findAll()).thenReturn(List.of(tarifa()));
        when(bloqueos.findDesde(HOY)).thenReturn(List.of());
        when(reservas.contar(any(), any(), any(), any())).thenReturn(2);
        when(canchas.findById(1L)).thenReturn(Optional.of(cancha(true, null)));

        List<ConsultarDisponibilidadUseCase.BloqueDisponible> bloques = new ConsultarDisponibilidadUseCase(
                horarios, tarifas, bloqueos, reservas, canchas, configuracion, RELOJ).ejecutar(1L, HOY);

        assertThat(bloques).allSatisfy(bloque -> assertThat(bloque.ocupado()).isTrue());
    }

    @Test
    @DisplayName("dada una tarifa que cruza con otra, cuando se consulta, entonces el precio se divide por franja (RN-03)")
    void precioPorFranja() {
        Tarifa tarifaManana = tarifa("10:00", "18:00", new BigDecimal("20"));
        Tarifa tarifaNoche = tarifa("18:00", "24:00", new BigDecimal("25"));
        when(horarios.obtener()).thenReturn(semanaAbierta());
        when(tarifas.findAll()).thenReturn(List.of(tarifaManana, tarifaNoche));
        when(bloqueos.findDesde(HOY)).thenReturn(List.of());
        when(reservas.contar(any(), any(), any(), any())).thenReturn(0);
        when(canchas.findById(1L)).thenReturn(Optional.of(cancha(true, null)));

        List<ConsultarDisponibilidadUseCase.BloqueDisponible> bloques = new ConsultarDisponibilidadUseCase(
                horarios, tarifas, bloqueos, reservas, canchas, configuracion, RELOJ).ejecutar(1L, HOY);

        // El bloque 17:30–18:00 queda en la franja de $20 y el 18:00–18:30 en la de $25.
        assertThat(bloques.get(15).inicio()).isEqualTo("17:30");
        assertThat(bloques.get(15).precio()).isEqualByComparingTo("10.00");
        assertThat(bloques.get(16).inicio()).isEqualTo("18:00");
        assertThat(bloques.get(16).precio()).isEqualByComparingTo("12.50");
    }

    @Test
    @DisplayName("dada una fecha sin consultar, cuando se ejecuta, entonces se pide la fecha (RF-06)")
    void fechaSinFecha() {
        assertThatThrownBy(() -> new ConsultarDisponibilidadUseCase(horarios, tarifas, bloqueos, reservas,
                canchas, configuracion, RELOJ).ejecutar(1L, null))
                .isInstanceOf(ReservaInvalidaException.class)
                .hasMessage("Elige la fecha de la consulta.");
    }

    @Test
    @DisplayName("dada una fecha fuera de la anticipación, cuando se consulta, entonces se rechaza (RN-02)")
    void fechaFueraDeAnticipacion() {
        assertThatThrownBy(() -> new ConsultarDisponibilidadUseCase(horarios, tarifas, bloqueos, reservas,
                canchas, configuracion, RELOJ).ejecutar(1L, HOY.minusDays(4)))
                .isInstanceOf(ReservaInvalidaException.class)
                .hasMessage("Solo se puede consultar disponibilidad de hoy hasta 3 días después.");
    }

    @Test
    @DisplayName("dada una cancha inactiva, cuando se consulta por su id, entonces se rechaza (404 / RF-04, RF-06)")
    void canchaInactivaSeRechaza() {
        when(canchas.findById(1L)).thenReturn(Optional.of(cancha(false, null)));

        assertThatThrownBy(() -> new ConsultarDisponibilidadUseCase(horarios, tarifas, bloqueos, reservas,
                canchas, configuracion, RELOJ).ejecutar(1L, HOY))
                .isInstanceOf(CanchaNoEncontradaException.class);
    }

    @Test
    @DisplayName("dada una cancha que no existe, cuando se consulta, entonces se rechaza (404)")
    void canchaInexistente() {
        when(canchas.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new ConsultarDisponibilidadUseCase(horarios, tarifas, bloqueos, reservas,
                canchas, configuracion, RELOJ).ejecutar(9L, HOY))
                .isInstanceOf(CanchaNoEncontradaException.class);
    }

    private static Cancha cancha(boolean activa, String foto) {
        return Cancha.reconstituir(1L, "Cancha 1", "Césped sintético", 30, 50, foto, activa, AHORA, AHORA);
    }
}
