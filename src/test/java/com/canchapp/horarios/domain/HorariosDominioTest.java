package com.canchapp.horarios.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Horarios, tarifas y bloqueos · dominio (RF-05)")
class HorariosDominioTest {

    private static final Set<DayOfWeek> TODOS = EnumSet.allOf(DayOfWeek.class);

    private static HoraDelDia h(String texto) {
        return HoraDelDia.de(texto, "la hora");
    }

    private static Tarifa tarifa(Set<DayOfWeek> dias, String inicio, String fin, String precio) {
        return Tarifa.de(null, dias, h(inicio), h(fin), new BigDecimal(precio));
    }

    @Test
    @DisplayName("dado «24:00», cuando se lee la hora, entonces es el final del día (RN-02b)")
    void veinticuatroHoras() {
        assertThat(h("24:00").minutos()).isEqualTo(1440);
        assertThat(h("17:30").texto()).isEqualTo("17:30");
    }

    @ParameterizedTest
    @ValueSource(strings = {"10:15", "24:30", "25:00", "10", "diez"})
    @DisplayName("dada una hora que no cae en un bloque de 30 minutos o no es válida, cuando se lee, entonces se rechaza (RN-01)")
    void horaInvalida(String texto) {
        assertThatThrownBy(() -> h(texto)).isInstanceOf(HorarioInvalidoException.class);
    }

    @Test
    @DisplayName("dado un día abierto con el cierre antes de la apertura, cuando se crea, entonces se rechaza")
    void diaAlReves() {
        assertThatThrownBy(() -> new HorarioDia(DayOfWeek.MONDAY, true, h("18:00"), h("10:00")))
                .isInstanceOf(HorarioInvalidoException.class);
    }

    @Test
    @DisplayName("dada una semana sin un día, cuando se arma el horario, entonces se rechaza")
    void semanaIncompleta() {
        List<HorarioDia> dias = new ArrayList<>();
        for (DayOfWeek d : DayOfWeek.values()) {
            if (d != DayOfWeek.SUNDAY) {
                dias.add(new HorarioDia(d, true, h("10:00"), h("24:00")));
            }
        }

        assertThatThrownBy(() -> HorarioSemanal.de(dias)).isInstanceOf(HorarioInvalidoException.class);
    }

    @Test
    @DisplayName("dadas las tarifas del DERCAS, cuando se comparan, entonces se tocan en las 18:00 pero no se superponen (RN-03)")
    void tarifasDelDercas() {
        Tarifa dia = tarifa(TODOS, "10:00", "18:00", "20.00");
        Tarifa noche = tarifa(TODOS, "18:00", "24:00", "25.00");

        assertThat(dia.seSuperponeCon(noche)).isFalse();
        assertThat(noche.getPrecioHora()).isEqualByComparingTo("25.00");
        assertThat(noche.getPrecioHora().scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("dadas dos tarifas que comparten un día y se cruzan en la hora, cuando se comparan, entonces se superponen")
    void tarifasSuperpuestas() {
        Tarifa semana = tarifa(EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY), "10:00", "18:00", "20");
        Tarifa viernes = tarifa(EnumSet.of(DayOfWeek.FRIDAY), "17:00", "20:00", "22");
        Tarifa sabado = tarifa(EnumSet.of(DayOfWeek.SATURDAY), "17:00", "20:00", "22");

        assertThat(semana.seSuperponeCon(viernes)).isTrue();
        assertThat(semana.seSuperponeCon(sabado)).isFalse();
    }

    @Test
    @DisplayName("dado un precio de 0 o con 3 decimales, cuando se crea la tarifa, entonces se rechaza")
    void precioInvalido() {
        assertThatThrownBy(() -> tarifa(TODOS, "10:00", "18:00", "0")).isInstanceOf(HorarioInvalidoException.class);
        assertThatThrownBy(() -> tarifa(TODOS, "10:00", "18:00", "20.125")).isInstanceOf(HorarioInvalidoException.class);
    }

    @Test
    @DisplayName("dado un bloqueo para ayer, cuando se crea, entonces se rechaza; sin motivo, también")
    void bloqueoInvalido() {
        LocalDate hoy = LocalDate.of(2026, 11, 28);
        Instant ahora = Instant.parse("2026-11-28T15:00:00Z");

        assertThatThrownBy(() -> Bloqueo.nuevo(null, hoy.minusDays(1), h("10:00"), h("12:00"), "Mantenimiento", hoy, ahora))
                .hasMessage("La fecha del bloqueo no puede ser anterior a hoy.");
        assertThatThrownBy(() -> Bloqueo.nuevo(1L, hoy, h("10:00"), h("12:00"), " ", hoy, ahora))
                .hasMessage("Escribe el motivo del bloqueo.");
        assertThat(Bloqueo.nuevo(null, hoy, h("10:00"), h("12:00"), "Feriado", hoy, ahora).esDeTodoElComplejo()).isTrue();
    }

    @Test
    @DisplayName("dados los días de una tarifa, cuando se guardan como bits y se leen, entonces son los mismos")
    void diasComoBits() {
        Set<DayOfWeek> dias = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.SUNDAY);

        assertThat(DiasSemana.aBits(TODOS)).isEqualTo(127);
        assertThat(DiasSemana.deBits(DiasSemana.aBits(dias))).isEqualTo(dias);
        assertThat(DiasSemana.dia("miércoles")).isEqualTo(DayOfWeek.WEDNESDAY);
    }
}
