package com.canchapp.configuracion.infrastructure;

import com.canchapp.configuracion.domain.Complejo;
import com.canchapp.configuracion.domain.ConfiguracionComplejo;
import com.canchapp.configuracion.domain.ConfiguracionRepository;
import com.canchapp.configuracion.domain.PoliticaReserva;
import com.canchapp.configuracion.domain.TipoCuenta;
import com.canchapp.configuracion.infrastructure.entity.ConfiguracionComplejoEntity;
import com.canchapp.configuracion.infrastructure.entity.ConfiguracionComplejoEntityRepository;
import com.canchapp.shared.infrastructure.ConfiguracionTiempo;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Implementación JPA del puerto {@link ConfiguracionRepository} (RF-03). Lee y escribe siempre la
 * fila 1 de {@code configuracion_complejo}; las fechas van en hora local de Ecuador.
 */
@Repository
public class ConfiguracionJpaAdapter implements ConfiguracionRepository {

    private final ConfiguracionComplejoEntityRepository jpa;

    public ConfiguracionJpaAdapter(ConfiguracionComplejoEntityRepository jpa) {
        this.jpa = jpa;
    }

    /** @throws IllegalStateException si falta la fila que siembra la migración V3 */
    @Override
    public ConfiguracionComplejo obtener() {
        return jpa.findById(ConfiguracionComplejoEntity.ID_UNICO)
                .map(ConfiguracionJpaAdapter::aDominio)
                .orElseThrow(() -> new IllegalStateException("Falta la configuración del complejo (migración V3)."));
    }

    @Override
    public ConfiguracionComplejo guardar(ConfiguracionComplejo configuracion) {
        return aDominio(jpa.save(aEntidad(configuracion)));
    }

    static ConfiguracionComplejo aDominio(ConfiguracionComplejoEntity e) {
        Complejo complejo = new Complejo(e.getNombre(), e.getDireccion(), e.getTelefono(), e.getBanco(),
                TipoCuenta.valueOf(e.getTipoCuenta()), e.getNumeroCuenta(), e.getTitularCuenta());
        PoliticaReserva politica = new PoliticaReserva(e.getMinutosExpiracion(), e.getHorasPlazoCancelacion(),
                e.getMaxReservasEfectivo(), e.getDiasAnticipacion(), e.getLimitarTrasInasistencia());
        return new ConfiguracionComplejo(complejo, politica, aInstante(e.getFechaActualizacion()));
    }

    static ConfiguracionComplejoEntity aEntidad(ConfiguracionComplejo c) {
        ConfiguracionComplejoEntity e = new ConfiguracionComplejoEntity();
        e.setId(ConfiguracionComplejoEntity.ID_UNICO);
        e.setNombre(c.complejo().nombre());
        e.setDireccion(c.complejo().direccion());
        e.setTelefono(c.complejo().telefono());
        e.setBanco(c.complejo().banco());
        e.setTipoCuenta(c.complejo().tipoCuenta().name());
        e.setNumeroCuenta(c.complejo().numeroCuenta());
        e.setTitularCuenta(c.complejo().titularCuenta());
        e.setMinutosExpiracion(c.politica().minutosExpiracion());
        e.setHorasPlazoCancelacion(c.politica().horasPlazoCancelacion());
        e.setMaxReservasEfectivo(c.politica().maxReservasEfectivo());
        e.setDiasAnticipacion(c.politica().diasAnticipacion());
        e.setLimitarTrasInasistencia(c.politica().limitarTrasInasistencia());
        e.setFechaActualizacion(LocalDateTime.ofInstant(c.actualizadaEn(), ConfiguracionTiempo.ZONA_COMPLEJO));
        return e;
    }

    private static Instant aInstante(LocalDateTime fecha) {
        return fecha == null ? null : fecha.atZone(ConfiguracionTiempo.ZONA_COMPLEJO).toInstant();
    }
}
