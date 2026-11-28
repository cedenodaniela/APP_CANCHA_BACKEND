package com.canchapp.reservas.application;

import com.canchapp.canchas.domain.Cancha;
import com.canchapp.canchas.domain.CanchaNoEncontradaException;
import com.canchapp.canchas.domain.CanchaRepository;
import com.canchapp.configuracion.domain.ConfiguracionRepository;
import com.canchapp.configuracion.domain.PoliticaReserva;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Consulta de disponibilidad (RF-06).
 * <p>
 * Devuelve los bloques de 30 minutos de una cancha para una fecha, con su estado (libre u ocupado)
 * y su precio (RN-03). El resultado se arma cruzando el horario de atención del día (RN-02b), las
 * tarifas del día (RN-03), los bloqueos de esa fecha (RF-05) y las reservas activas (RF-07): un
 * bloque queda ocupado si un bloqueo o una reserva activa lo cubre.
 * <p>
 * Si no se indica cancha, se devuelven los bloques de todas las canchas activas; una cancha
 * inactiva no se ofrece al cliente (RF-04, RF-06).
 * <p>
 * Solo se aceptan fechas desde hoy hasta hoy + {@code diasAnticipacion} (RN-02); los bloques ya
 * pasados de hoy no se devuelven como libres.
 *
 * @see "RF-06 · consulta de disponibilidad"
 * @see "RN-01 · bloques de 30 min, mínimo 1 h, consecutivos"
 * @see "RN-02 · anticipación"
 * @see "RN-02b · horario de atención"
 * @see "RN-03 · tarifa"
 * @see "RN-04 · nunca doble reserva"
 */
public class ConsultarDisponibilidadUseCase {

    /** Un bloque de disponibilidad: de qué cancha, en qué fecha, con qué horario y precio. */
    public record BloqueDisponible(Long canchaId, LocalDate fecha, String inicio, String fin, boolean ocupado,
                                   BigDecimal precio) {
    }

    private final HorarioRepository horarios;
    private final TarifaRepository tarifas;
    private final BloqueoRepository bloqueos;
    private final ReservasActivasEnHorario reservas;
    private final CanchaRepository canchas;
    private final ConfiguracionRepository configuracion;
    private final Clock reloj;

    public ConsultarDisponibilidadUseCase(HorarioRepository horarios, TarifaRepository tarifas,
                                         BloqueoRepository bloqueos, ReservasActivasEnHorario reservas,
                                         CanchaRepository canchas, ConfiguracionRepository configuracion,
                                         Clock reloj) {
        this.horarios = horarios;
        this.tarifas = tarifas;
        this.bloqueos = bloqueos;
        this.reservas = reservas;
        this.canchas = canchas;
        this.configuracion = configuracion;
        this.reloj = reloj;
    }

    /**
     * Devuelve los bloques de disponibilidad de una cancha para una fecha.
     *
     * @param canchaId {@code null} para consultar las activas; si no es nula debe existir y estar
     *        activa
     * @param fecha      la fecha consultada, «AAAA-MM-DD»
     * @return los bloques del día con su estado y precio; vacío si el día está cerrado
     * @throws ReservaInvalidaException si la fecha falta o está fuera de la anticipación (RN-02)
     * @throws CanchaNoEncontradaException si la cancha indicada no existe o no está activa
     */
    public List<BloqueDisponible> ejecutar(Long canchaId, LocalDate fecha) {
        PoliticaReserva politica = configuracion.obtener().politica();
        if (fecha == null) {
            throw ReservaInvalidaException.datos("Elige la fecha de la consulta.");
        }
        LocalDate hoy = LocalDate.now(reloj);
        if (fecha.isBefore(hoy) || fecha.isAfter(hoy.plusDays(politica.diasAnticipacion()))) {
            throw ReservaInvalidaException.datos("Solo se puede consultar disponibilidad de hoy hasta "
                    + politica.diasAnticipacion() + " días después.");
        }
        List<Cancha> canchasConsultadas = canchaId == null ? canchas.findActivas() : List.of(cancha(canchaId));
        List<BloqueDisponible> bloques = new ArrayList<>();
        for (Cancha cancha : canchasConsultadas) {
            bloques.addAll(bloquesDe(cancha.getId(), fecha));
        }
        return bloques;
    }

    /** Una cancha inactiva no se ofrece: para el cliente no existe (RF-04, RF-06). */
    private Cancha cancha(Long canchaId) {
        return canchas.findById(canchaId)
                .filter(Cancha::isActiva)
                .orElseThrow(CanchaNoEncontradaException::new);
    }

    /**
     * Bloques del día para una cancha: del horario de atención (RN-02b) al cierre, en bloques de
     * 30 minutos (RN-01), marcando el ocupado por bloqueos o reservas activas.
     */
    private List<BloqueDisponible> bloquesDe(Long canchaId, LocalDate fecha) {
        HorarioSemanal semana = horarios.obtener();
        HorarioDia dia = semana.del(fecha.getDayOfWeek());
        if (!dia.abierto()) {
            return List.of();
        }
        List<Bloqueo> delDia = bloqueos.findDesde(fecha).stream()
                .filter(bloqueo -> bloqueo.fecha().equals(fecha))
                .filter(bloqueo -> bloqueo.esDeTodoElComplejo() || Objects.equals(bloqueo.canchaId(), canchaId))
                .toList();
        List<BloqueDisponible> bloques = new ArrayList<>();
        for (int inicioBloque = dia.apertura().minutos(); inicioBloque < dia.cierre().minutos();
                inicioBloque += HoraDelDia.MINUTOS_BLOQUE) {
            int finBloque = Math.min(inicioBloque + HoraDelDia.MINUTOS_BLOQUE, dia.cierre().minutos());
            boolean ocupado = estaOcupado(delDia, inicioBloque, finBloque)
                    || reservas.contar(canchaId, fecha, new HoraDelDia(inicioBloque), new HoraDelDia(finBloque)) > 0;
            bloques.add(new BloqueDisponible(canchaId, fecha, new HoraDelDia(inicioBloque).texto(),
                    new HoraDelDia(finBloque).texto(), ocupado, precio(fecha, inicioBloque, finBloque)));
        }
        return bloques;
    }

    /** Un bloque está ocupado si un bloqueo del día lo cubre. */
    private boolean estaOcupado(List<Bloqueo> delDia, int inicio, int fin) {
        return delDia.stream().anyMatch(bloqueo -> bloqueo.inicio().minutos() < fin && inicio < bloqueo.fin().minutos());
    }

    /**
     * Precio del bloque: cada franja cobra la mitad de su tarifa por bloque de 30 minutos (RN-03); si
     * el bloque cruza una franja se suma la parte proporcional de cada una.
     */
    private BigDecimal precio(LocalDate fecha, int inicio, int fin) {
        BigDecimal total = BigDecimal.ZERO;
        for (Tarifa tarifa : tarifas.findAll()) {
            if (!tarifa.getDias().contains(fecha.getDayOfWeek())) {
                continue;
            }
            int inicioTarifa = tarifa.getInicio().minutos();
            int finTarifa = tarifa.getFin().minutos();
            if (inicioTarifa >= fin || finTarifa <= inicio) {
                continue;
            }
            int overlapInicio = Math.max(inicio, inicioTarifa);
            int overlapFin = Math.min(fin, finTarifa);
            BigDecimal horas = BigDecimal.valueOf((overlapFin - overlapInicio) / 60.0);
            total = total.add(tarifa.getPrecioHora().multiply(horas));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
