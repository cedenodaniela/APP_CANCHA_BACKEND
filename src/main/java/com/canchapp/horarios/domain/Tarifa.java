package com.canchapp.horarios.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Precio por hora en una franja de ciertos días (RF-05). Cada bloque de 30 minutos cuesta la mitad
 * de la tarifa de su franja (RN-03).
 * <p>
 * Dinero siempre con {@link BigDecimal} y escala 2 (AGENTS.md §6).
 *
 * @see "RN-03 · $20/h de 10:00 a 18:00 y $25/h de 18:00 a 24:00 (valores iniciales)"
 */
public final class Tarifa {

    private final Long id;
    private final Set<DayOfWeek> dias;
    private final HoraDelDia inicio;
    private final HoraDelDia fin;
    private final BigDecimal precioHora;

    private Tarifa(Long id, Set<DayOfWeek> dias, HoraDelDia inicio, HoraDelDia fin, BigDecimal precioHora) {
        this.id = id;
        this.dias = Collections.unmodifiableSet(EnumSet.copyOf(dias));
        this.inicio = inicio;
        this.fin = fin;
        this.precioHora = precioHora;
    }

    /**
     * @throws HorarioInvalidoException si no hay días, el inicio no es antes del fin o el precio no es mayor que 0
     */
    public static Tarifa de(Long id, Set<DayOfWeek> dias, HoraDelDia inicio, HoraDelDia fin, BigDecimal precioHora) {
        if (dias == null || dias.isEmpty()) {
            throw new HorarioInvalidoException("Elige al menos un día para la tarifa.");
        }
        Objects.requireNonNull(inicio, "inicio");
        Objects.requireNonNull(fin, "fin");
        if (!inicio.esAntesDe(fin)) {
            throw new HorarioInvalidoException("La hora de inicio de la tarifa debe ser antes de la hora de fin.");
        }
        if (precioHora == null || precioHora.signum() <= 0) {
            throw new HorarioInvalidoException("El precio por hora debe ser mayor que 0.");
        }
        if (precioHora.scale() > 2 && precioHora.stripTrailingZeros().scale() > 2) {
            throw new HorarioInvalidoException("El precio por hora admite hasta 2 decimales.");
        }
        return new Tarifa(id, dias, inicio, fin, precioHora.setScale(2, RoundingMode.UNNECESSARY));
    }

    /** Dos tarifas chocan si comparten al menos un día y sus franjas se cruzan (los bordes pueden tocarse). */
    public boolean seSuperponeCon(Tarifa otra) {
        boolean compartenDia = dias.stream().anyMatch(otra.dias::contains);
        return compartenDia && inicio.esAntesDe(otra.fin) && otra.inicio.esAntesDe(fin);
    }

    public Tarifa conId(Long nuevoId) {
        return new Tarifa(nuevoId, dias, inicio, fin, precioHora);
    }

    public Long getId() { return id; }
    public Set<DayOfWeek> getDias() { return dias; }
    public HoraDelDia getInicio() { return inicio; }
    public HoraDelDia getFin() { return fin; }
    public BigDecimal getPrecioHora() { return precioHora; }
}
