package com.canchapp.reservas.domain;

import com.canchapp.shared.domain.ExcepcionNegocio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Reserva de una cancha en bloques consecutivos de 30 minutos (RF-07).
 * <p>
 * Java puro y sin setters: el estado cambia solo con los métodos de transición, que validan que
 * el cambio exista en el diagrama de estados (AGENTS.md §5) y devuelven una reserva nueva. Una
 * transición que no existe lanza {@link ReservaInvalidaException} con código {@code TRANSICION_INVALIDA}.
 * <p>
 * Los plazos (minutos para el comprobante, horas para cancelar) llegan como parámetros de
 * {@code PoliticaReserva}; nunca se escriben aquí (AGENTS.md §5).
 *
 * @see "RN-01 · bloques de 30 min, mínimo 1 h, consecutivos"
 * @see "RN-04 · nunca doble reserva"
 */
public final class Reserva {

    /** RN-01: un bloque dura 30 minutos. */
    public static final int MINUTOS_BLOQUE = 30;
    /** RN-01: la reserva mínima es de 1 hora (2 bloques). */
    public static final int BLOQUES_MINIMOS = 2;

    private static final ZoneId ZONA = ZoneId.of("America/Guayaquil");

    private final Long id;
    private final Long clienteId;
    private final Long canchaId;
    private final LocalDate fecha;
    private final int minutoInicio;
    private final int minutoFin;
    private final MetodoPago metodoPago;
    private final BigDecimal total;
    private final EstadoReserva estado;
    private final Instant creadaEn;
    private final Instant venceComprobanteEn;
    private final String motivoRechazo;

    private Reserva(Long id, Long clienteId, Long canchaId, LocalDate fecha, int minutoInicio, int minutoFin,
                    MetodoPago metodoPago, BigDecimal total, EstadoReserva estado, Instant creadaEn,
                    Instant venceComprobanteEn, String motivoRechazo) {
        this.id = id;
        this.clienteId = clienteId;
        this.canchaId = canchaId;
        this.fecha = fecha;
        this.minutoInicio = minutoInicio;
        this.minutoFin = minutoFin;
        this.metodoPago = metodoPago;
        this.total = total;
        this.estado = estado;
        this.creadaEn = creadaEn;
        this.venceComprobanteEn = venceComprobanteEn;
        this.motivoRechazo = motivoRechazo;
    }

    /**
     * Crea una reserva nueva. Su primer estado depende de cómo paga:
     * <ul>
     *   <li>transferencia: {@code PENDIENTE_COMPROBANTE}, con {@code minutosExpiracion} para subir el comprobante (RN-05);</li>
     *   <li>efectivo: {@code CONFIRMADA} (RN-07 se valida en el caso de uso);</li>
     *   <li>saldo a favor que cubre el total: {@code PAGADA} directamente.</li>
     * </ul>
     *
     * @param minutoInicio minutos desde las 00:00, múltiplo de 30
     * @param minutoFin    minutos desde las 00:00, hasta 1440 (24:00)
     * @param total        calculado con las tarifas (RN-03), con escala 2
     * @param minutosExpiracion de {@code PoliticaReserva}; solo se usa con transferencia
     * @throws ReservaInvalidaException si los bloques no cumplen RN-01
     */
    public static Reserva nueva(Long clienteId, Long canchaId, LocalDate fecha, int minutoInicio, int minutoFin,
                                MetodoPago metodoPago, BigDecimal total, Instant ahora, int minutosExpiracion) {
        Objects.requireNonNull(clienteId, "clienteId");
        Objects.requireNonNull(canchaId, "canchaId");
        Objects.requireNonNull(fecha, "fecha");
        Objects.requireNonNull(metodoPago, "metodoPago");
        validarBloques(minutoInicio, minutoFin);
        if (total == null || total.signum() <= 0) {
            throw ReservaInvalidaException.datos("El total de la reserva debe ser mayor que 0.");
        }
        EstadoReserva inicial = switch (metodoPago) {
            case TRANSFERENCIA -> EstadoReserva.PENDIENTE_COMPROBANTE;
            case EFECTIVO -> EstadoReserva.CONFIRMADA;
            case SALDO_FAVOR -> EstadoReserva.PAGADA;
        };
        Instant vence = metodoPago == MetodoPago.TRANSFERENCIA ? ahora.plus(Duration.ofMinutes(minutosExpiracion)) : null;
        return new Reserva(null, clienteId, canchaId, fecha, minutoInicio, minutoFin, metodoPago,
                total.setScale(2, RoundingMode.HALF_UP), inicial, ahora, vence, null);
    }

    /** Reconstruye una reserva guardada. Solo lo usan los adaptadores de persistencia. */
    public static Reserva reconstituir(Long id, Long clienteId, Long canchaId, LocalDate fecha, int minutoInicio,
                                       int minutoFin, MetodoPago metodoPago, BigDecimal total, EstadoReserva estado,
                                       Instant creadaEn, Instant venceComprobanteEn, String motivoRechazo) {
        return new Reserva(id, clienteId, canchaId, fecha, minutoInicio, minutoFin, metodoPago, total, estado,
                creadaEn, venceComprobanteEn, motivoRechazo);
    }

    /** RN-01: bloques de 30 minutos, al menos 2, consecutivos dentro del mismo día. */
    static void validarBloques(int minutoInicio, int minutoFin) {
        if (minutoInicio < 0 || minutoFin > 24 * 60 || minutoInicio % MINUTOS_BLOQUE != 0 || minutoFin % MINUTOS_BLOQUE != 0) {
            throw ReservaInvalidaException.datos("La reserva debe empezar y terminar en punto o y media.");
        }
        if ((minutoFin - minutoInicio) < BLOQUES_MINIMOS * MINUTOS_BLOQUE) {
            throw ReservaInvalidaException.datos("La reserva mínima es de 1 hora.");
        }
    }

    // ------------------------------------------------------------ Transiciones

    /** PENDIENTE_COMPROBANTE → EN_VERIFICACION. El contador de RN-05 se detiene al subir el comprobante. */
    public Reserva subirComprobante() {
        exigir(EstadoReserva.PENDIENTE_COMPROBANTE, "subir el comprobante de");
        return con(EstadoReserva.EN_VERIFICACION, null);
    }

    /** EN_VERIFICACION → PAGADA: el administrador aprueba el comprobante (RF-09). */
    public Reserva aprobarComprobante() {
        exigir(EstadoReserva.EN_VERIFICACION, "aprobar el comprobante de");
        return con(EstadoReserva.PAGADA, null);
    }

    /**
     * EN_VERIFICACION → RECHAZADA, con motivo obligatorio. El horario queda libre (RN-06).
     *
     * @see "RN-06 · rechazar exige un motivo"
     */
    public Reserva rechazarComprobante(String motivo) {
        exigir(EstadoReserva.EN_VERIFICACION, "rechazar el comprobante de");
        if (motivo == null || motivo.isBlank()) {
            throw ReservaInvalidaException.datos("Escribe el motivo del rechazo.");
        }
        return con(EstadoReserva.RECHAZADA, motivo.trim());
    }

    /**
     * PENDIENTE_COMPROBANTE → EXPIRADA, solo cuando pasó el tiempo para subir el comprobante (RN-05).
     * La llama la tarea programada de RF-13; es idempotente si ya expiró.
     */
    public Reserva expirar(Instant ahora) {
        if (estado == EstadoReserva.EXPIRADA) {
            return this;
        }
        exigir(EstadoReserva.PENDIENTE_COMPROBANTE, "expirar");
        if (ahora.isBefore(venceComprobanteEn)) {
            throw ReservaInvalidaException.datos("La reserva todavía tiene tiempo para subir el comprobante.");
        }
        return con(EstadoReserva.EXPIRADA, null);
    }

    /** CONFIRMADA → PAGADA: el administrador registra el pago en efectivo (RF-10). */
    public Reserva registrarPagoEfectivo() {
        exigir(EstadoReserva.CONFIRMADA, "registrar el pago en efectivo de");
        return con(EstadoReserva.PAGADA, null);
    }

    /** CONFIRMADA → NO_ASISTIO: el cliente no llegó a una reserva en efectivo (RN-08). */
    public Reserva marcarInasistencia() {
        exigir(EstadoReserva.CONFIRMADA, "marcar la inasistencia de");
        return con(EstadoReserva.NO_ASISTIO, null);
    }

    /**
     * PAGADA → COMPLETADA cuando terminó el horario. La llama la tarea programada de RF-13; es
     * idempotente si ya está completada.
     */
    public Reserva completar(Instant ahora) {
        if (estado == EstadoReserva.COMPLETADA) {
            return this;
        }
        exigir(EstadoReserva.PAGADA, "completar");
        if (ahora.isBefore(fin())) {
            throw ReservaInvalidaException.datos("La reserva todavía no termina.");
        }
        return con(EstadoReserva.COMPLETADA, null);
    }

    /**
     * PENDIENTE_COMPROBANTE, CONFIRMADA o PAGADA → CANCELADA, hasta {@code horasPlazoCancelacion}
     * antes del inicio (RN-09). Una reserva EN_VERIFICACION no se cancela (RN-11): el caso de uso
     * avisa al administrador para que verifique el pago.
     *
     * @throws ReservaEnVerificacionException si está EN_VERIFICACION (RN-11)
     * @see "RN-09 · cancelación hasta 3 h antes (horasPlazoCancelacion)"
     * @see "RN-11 · una reserva EN_VERIFICACION no se puede cancelar"
     */
    public Reserva cancelar(Instant ahora, int horasPlazoCancelacion) {
        if (estado == EstadoReserva.EN_VERIFICACION) {
            throw new ReservaEnVerificacionException();
        }
        if (estado != EstadoReserva.PENDIENTE_COMPROBANTE && estado != EstadoReserva.CONFIRMADA
                && estado != EstadoReserva.PAGADA) {
            throw ReservaInvalidaException.transicion(estado, "cancelar");
        }
        if (ahora.plus(Duration.ofHours(horasPlazoCancelacion)).isAfter(inicio())) {
            throw new ReservaInvalidaException("FUERA_DE_PLAZO",
                    "Solo se puede cancelar hasta " + horasPlazoCancelacion + " horas antes del partido.",
                    ExcepcionNegocio.Tipo.CONFLICTO);
        }
        return con(EstadoReserva.CANCELADA, null);
    }

    // ------------------------------------------------------------ Consultas

    /** RN-04: si la reserva ocupa sus bloques. */
    public boolean bloqueaHorario() {
        return estado.bloqueaHorario();
    }

    public Instant inicio() {
        return fecha.atStartOfDay(ZONA).plusMinutes(minutoInicio).toInstant();
    }

    public Instant fin() {
        return fecha.atStartOfDay(ZONA).plusMinutes(minutoFin).toInstant();
    }

    private void exigir(EstadoReserva esperado, String accion) {
        if (estado != esperado) {
            throw ReservaInvalidaException.transicion(estado, accion);
        }
    }

    private Reserva con(EstadoReserva nuevo, String motivo) {
        return new Reserva(id, clienteId, canchaId, fecha, minutoInicio, minutoFin, metodoPago, total, nuevo,
                creadaEn, venceComprobanteEn, motivo != null ? motivo : motivoRechazo);
    }

    public Long getId() { return id; }
    public Long getClienteId() { return clienteId; }
    public Long getCanchaId() { return canchaId; }
    public LocalDate getFecha() { return fecha; }
    public int getMinutoInicio() { return minutoInicio; }
    public int getMinutoFin() { return minutoFin; }
    public MetodoPago getMetodoPago() { return metodoPago; }
    public BigDecimal getTotal() { return total; }
    public EstadoReserva getEstado() { return estado; }
    public Instant getCreadaEn() { return creadaEn; }
    public Instant getVenceComprobanteEn() { return venceComprobanteEn; }
    public String getMotivoRechazo() { return motivoRechazo; }
}
