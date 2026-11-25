package com.canchapp.usuarios.infrastructure;

import com.canchapp.shared.infrastructure.ConfiguracionTiempo;
import com.canchapp.usuarios.domain.TokenRecuperacion;
import com.canchapp.usuarios.domain.TokenRecuperacionRepository;
import com.canchapp.usuarios.infrastructure.entity.RecuperacionContrasenaEntity;
import com.canchapp.usuarios.infrastructure.entity.RecuperacionContrasenaEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Implementación JPA del puerto {@link TokenRecuperacionRepository} (RF-02).
 * Las fechas se guardan en hora local de Ecuador, como en la tabla {@code usuario}.
 */
@Repository
public class RecuperacionJpaAdapter implements TokenRecuperacionRepository {

    private final RecuperacionContrasenaEntityRepository jpa;

    public RecuperacionJpaAdapter(RecuperacionContrasenaEntityRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public TokenRecuperacion save(TokenRecuperacion token) {
        return aDominio(jpa.save(aEntidad(token)));
    }

    @Override
    public Optional<TokenRecuperacion> findByHashToken(String hashToken) {
        return jpa.findByHashToken(hashToken).map(RecuperacionJpaAdapter::aDominio);
    }

    @Override
    @Transactional
    public boolean marcarUsado(Long id, Instant ahora) {
        return jpa.marcarUsado(id, aFechaLocal(ahora)) == 1;
    }

    @Override
    @Transactional
    public void anularPendientes(Long usuarioId, Instant ahora) {
        jpa.anularPendientes(usuarioId, aFechaLocal(ahora));
    }

    static TokenRecuperacion aDominio(RecuperacionContrasenaEntity e) {
        return TokenRecuperacion.reconstituir(e.getId(), e.getUsuarioId(), e.getHashToken(),
                aInstante(e.getFechaCreacion()), aInstante(e.getFechaVencimiento()), aInstante(e.getFechaUso()));
    }

    static RecuperacionContrasenaEntity aEntidad(TokenRecuperacion t) {
        RecuperacionContrasenaEntity e = new RecuperacionContrasenaEntity();
        e.setId(t.getId());
        e.setUsuarioId(t.getUsuarioId());
        e.setHashToken(t.getHashToken());
        e.setFechaCreacion(aFechaLocal(t.getCreadoEn()));
        e.setFechaVencimiento(aFechaLocal(t.getVenceEn()));
        e.setFechaUso(aFechaLocal(t.getUsadoEn()));
        return e;
    }

    private static Instant aInstante(LocalDateTime fecha) {
        return fecha == null ? null : fecha.atZone(ConfiguracionTiempo.ZONA_COMPLEJO).toInstant();
    }

    private static LocalDateTime aFechaLocal(Instant instante) {
        return instante == null ? null : LocalDateTime.ofInstant(instante, ConfiguracionTiempo.ZONA_COMPLEJO);
    }
}
