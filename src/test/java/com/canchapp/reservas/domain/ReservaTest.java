package com.canchapp.reservas.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Entidad Reserva: las nueve transiciones del diagrama de estados (AGENTS.md §5) se prueban antes
 * de exponerlas en un endpoint (PLANIFICACION §6).
 */
@DisplayName("Reserva · estados y transiciones (RF-07)")
class ReservaTest {

    /** Sábado 28 de noviembre de 2026, 10:00 en Guayaquil (UTC-5). */
    private static final Instant AHORA = Instant.parse("2026-11-28T15:00:00Z");
    private static final LocalDate MANANA = LocalDate.of(2026, 11, 29);
    private static final int MINUTOS_EXPIRACION = 15;
    private static final int HORAS_CANCELACION = 3;

    /** Mañana de 18:00 a 19:30 (3 bloques). */
    private static Reserva nueva(MetodoPago metodo) {
        return Reserva.nueva(7L, 1L, MANANA, 18 * 60, 19 * 60 + 30, metodo, new BigDecimal("37.5"), AHORA, MINUTOS_EXPIRACION);
    }

    @Nested
    @DisplayName("Al crearla")
    class AlCrear {

        @Test
        @DisplayName("dada una transferencia, cuando se reserva, entonces queda PENDIENTE_COMPROBANTE con 15 minutos para el comprobante (RN-05)")
        void transferencia() {
            Reserva r = nueva(MetodoPago.TRANSFERENCIA);

            assertThat(r.getEstado()).isEqualTo(EstadoReserva.PENDIENTE_COMPROBANTE);
            assertThat(r.getVenceComprobanteEn()).isEqualTo(AHORA.plus(Duration.ofMinutes(15)));
            assertThat(r.getTotal()).isEqualByComparingTo("37.50");
            assertThat(r.getTotal().scale()).isEqualTo(2);
            assertThat(r.bloqueaHorario()).isTrue();
        }

        @Test
        @DisplayName("dado efectivo, cuando se reserva, entonces queda CONFIRMADA sin plazo de comprobante")
        void efectivo() {
            Reserva r = nueva(MetodoPago.EFECTIVO);

            assertThat(r.getEstado()).isEqualTo(EstadoReserva.CONFIRMADA);
            assertThat(r.getVenceComprobanteEn()).isNull();
        }

        @Test
        @DisplayName("dado saldo a favor que cubre el total, cuando se reserva, entonces queda PAGADA directamente")
        void saldoFavor() {
            assertThat(nueva(MetodoPago.SALDO_FAVOR).getEstado()).isEqualTo(EstadoReserva.PAGADA);
        }

        @Test
        @DisplayName("dado un solo bloque, cuando se reserva, entonces se rechaza: la mínima es 1 hora (RN-01)")
        void minimoUnaHora() {
            assertThatThrownBy(() -> Reserva.nueva(7L, 1L, MANANA, 18 * 60, 18 * 60 + 30, MetodoPago.EFECTIVO,
                    new BigDecimal("12.50"), AHORA, MINUTOS_EXPIRACION))
                    .isInstanceOf(ReservaInvalidaException.class)
                    .hasMessage("La reserva mínima es de 1 hora.");
        }

        @Test
        @DisplayName("dado un inicio que no cae en punto o y media, cuando se reserva, entonces se rechaza (RN-01)")
        void bloquesDeTreintaMinutos() {
            assertThatThrownBy(() -> Reserva.nueva(7L, 1L, MANANA, 18 * 60 + 15, 19 * 60 + 15, MetodoPago.EFECTIVO,
                    new BigDecimal("25"), AHORA, MINUTOS_EXPIRACION))
                    .isInstanceOf(ReservaInvalidaException.class);
        }

        @Test
        @DisplayName("dado el último bloque del día, cuando se reserva hasta las 24:00, entonces se acepta (RN-02b)")
        void hastaMedianoche() {
            Reserva r = Reserva.nueva(7L, 1L, MANANA, 23 * 60, 24 * 60, MetodoPago.EFECTIVO, new BigDecimal("25"), AHORA,
                    MINUTOS_EXPIRACION);

            assertThat(r.fin()).isEqualTo(Instant.parse("2026-11-30T05:00:00Z"));
        }
    }

    @Nested
    @DisplayName("Las nueve transiciones")
    class Transiciones {

        @Test
        @DisplayName("1 · PENDIENTE_COMPROBANTE → EN_VERIFICACION al subir el comprobante")
        void subirComprobante() {
            assertThat(nueva(MetodoPago.TRANSFERENCIA).subirComprobante().getEstado()).isEqualTo(EstadoReserva.EN_VERIFICACION);
        }

        @Test
        @DisplayName("2 · EN_VERIFICACION → PAGADA al aprobar el comprobante")
        void aprobar() {
            Reserva r = nueva(MetodoPago.TRANSFERENCIA).subirComprobante().aprobarComprobante();

            assertThat(r.getEstado()).isEqualTo(EstadoReserva.PAGADA);
        }

        @Test
        @DisplayName("3 · PAGADA → COMPLETADA cuando terminó el horario, y repetirlo no cambia nada")
        void completar() {
            Reserva pagada = nueva(MetodoPago.SALDO_FAVOR);
            Instant despues = pagada.fin().plusSeconds(1);

            Reserva completada = pagada.completar(despues);

            assertThat(completada.getEstado()).isEqualTo(EstadoReserva.COMPLETADA);
            assertThat(completada.completar(despues)).isSameAs(completada);
            assertThat(completada.bloqueaHorario()).isFalse();
        }

        @Test
        @DisplayName("4 · PENDIENTE_COMPROBANTE → EXPIRADA a los 15 minutos, y repetirlo no cambia nada (RN-05)")
        void expirar() {
            Reserva pendiente = nueva(MetodoPago.TRANSFERENCIA);

            assertThatThrownBy(() -> pendiente.expirar(AHORA.plus(Duration.ofMinutes(14))))
                    .isInstanceOf(ReservaInvalidaException.class);
            Reserva expirada = pendiente.expirar(AHORA.plus(Duration.ofMinutes(15)));

            assertThat(expirada.getEstado()).isEqualTo(EstadoReserva.EXPIRADA);
            assertThat(expirada.expirar(AHORA.plus(Duration.ofHours(1)))).isSameAs(expirada);
            assertThat(expirada.bloqueaHorario()).isFalse();
        }

        @Test
        @DisplayName("5 · EN_VERIFICACION → RECHAZADA con motivo; sin motivo no se puede (RN-06)")
        void rechazar() {
            Reserva enVerificacion = nueva(MetodoPago.TRANSFERENCIA).subirComprobante();

            assertThatThrownBy(() -> enVerificacion.rechazarComprobante(" "))
                    .hasMessage("Escribe el motivo del rechazo.");
            Reserva rechazada = enVerificacion.rechazarComprobante(" El monto no coincide ");

            assertThat(rechazada.getEstado()).isEqualTo(EstadoReserva.RECHAZADA);
            assertThat(rechazada.getMotivoRechazo()).isEqualTo("El monto no coincide");
            assertThat(rechazada.bloqueaHorario()).isFalse();
        }

        @Test
        @DisplayName("6 · CONFIRMADA → PAGADA al registrar el pago en efectivo")
        void pagoEfectivo() {
            assertThat(nueva(MetodoPago.EFECTIVO).registrarPagoEfectivo().getEstado()).isEqualTo(EstadoReserva.PAGADA);
        }

        @Test
        @DisplayName("7 · CONFIRMADA → NO_ASISTIO al marcar la inasistencia (RN-08)")
        void inasistencia() {
            Reserva r = nueva(MetodoPago.EFECTIVO).marcarInasistencia();

            assertThat(r.getEstado()).isEqualTo(EstadoReserva.NO_ASISTIO);
            assertThat(r.bloqueaHorario()).isFalse();
        }

        @ParameterizedTest(name = "8 · {0} → CANCELADA con 3 horas o más de anticipación")
        @EnumSource(value = MetodoPago.class)
        @DisplayName("8 · PENDIENTE_COMPROBANTE, CONFIRMADA o PAGADA → CANCELADA dentro del plazo (RN-09)")
        void cancelarDentroDelPlazo(MetodoPago metodo) {
            Reserva r = nueva(metodo);
            Instant tresHorasAntes = r.inicio().minus(Duration.ofHours(HORAS_CANCELACION));

            assertThat(r.cancelar(tresHorasAntes, HORAS_CANCELACION).getEstado()).isEqualTo(EstadoReserva.CANCELADA);
        }

        @Test
        @DisplayName("9 · PAGADA → CANCELADA no se permite a menos de 3 horas del inicio (RN-09)")
        void cancelarFueraDePlazo() {
            Reserva pagada = nueva(MetodoPago.SALDO_FAVOR);
            Instant dosHorasAntes = pagada.inicio().minus(Duration.ofHours(2));

            assertThatThrownBy(() -> pagada.cancelar(dosHorasAntes, HORAS_CANCELACION))
                    .isInstanceOf(ReservaInvalidaException.class)
                    .hasMessage("Solo se puede cancelar hasta 3 horas antes del partido.");
        }
    }

    @Nested
    @DisplayName("Transiciones que no existen")
    class Invalidas {

        @Test
        @DisplayName("dada una reserva EN_VERIFICACION, cuando se cancela, entonces no se puede: el cliente debe esperar (RN-11)")
        void enVerificacionNoSeCancela() {
            Reserva enVerificacion = nueva(MetodoPago.TRANSFERENCIA).subirComprobante();

            assertThatThrownBy(() -> enVerificacion.cancelar(AHORA, HORAS_CANCELACION))
                    .isInstanceOf(ReservaEnVerificacionException.class)
                    .hasFieldOrPropertyWithValue("codigo", "RESERVA_EN_VERIFICACION");
        }

        @Test
        @DisplayName("dada una reserva en efectivo, cuando se sube un comprobante, entonces no se puede")
        void efectivoSinComprobante() {
            assertThatThrownBy(() -> nueva(MetodoPago.EFECTIVO).subirComprobante())
                    .isInstanceOf(ReservaInvalidaException.class)
                    .hasFieldOrPropertyWithValue("codigo", "TRANSICION_INVALIDA");
        }

        @Test
        @DisplayName("dada una reserva por transferencia pendiente, cuando se aprueba sin comprobante, entonces no se puede")
        void aprobarSinComprobante() {
            assertThatThrownBy(() -> nueva(MetodoPago.TRANSFERENCIA).aprobarComprobante())
                    .isInstanceOf(ReservaInvalidaException.class);
        }

        @Test
        @DisplayName("dada una reserva cancelada, cuando se intenta volver a cancelar o pagar, entonces no se puede")
        void canceladaEsFinal() {
            Reserva cancelada = nueva(MetodoPago.EFECTIVO).cancelar(AHORA, HORAS_CANCELACION);

            assertThatThrownBy(() -> cancelada.cancelar(AHORA, HORAS_CANCELACION)).isInstanceOf(ReservaInvalidaException.class);
            assertThatThrownBy(cancelada::registrarPagoEfectivo).isInstanceOf(ReservaInvalidaException.class);
            assertThat(cancelada.bloqueaHorario()).isFalse();
        }

        @Test
        @DisplayName("dada una reserva pagada antes de terminar, cuando se completa, entonces no se puede")
        void completarAntesDeTiempo() {
            assertThatThrownBy(() -> nueva(MetodoPago.SALDO_FAVOR).completar(AHORA))
                    .hasMessage("La reserva todavía no termina.");
        }
    }

    @Test
    @DisplayName("dados todos los estados, cuando se pregunta cuáles ocupan el horario, entonces solo los 4 activos (RN-04)")
    void estadosQueBloquean() {
        assertThat(java.util.Arrays.stream(EstadoReserva.values()).filter(EstadoReserva::bloqueaHorario))
                .containsExactlyInAnyOrder(EstadoReserva.PENDIENTE_COMPROBANTE, EstadoReserva.EN_VERIFICACION,
                        EstadoReserva.CONFIRMADA, EstadoReserva.PAGADA);
    }
}
