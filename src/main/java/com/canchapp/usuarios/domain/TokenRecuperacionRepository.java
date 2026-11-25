package com.canchapp.usuarios.domain;

import java.time.Instant;
import java.util.Optional;

/** Puerto de persistencia de los enlaces de recuperación (RF-02). */
public interface TokenRecuperacionRepository {

    TokenRecuperacion save(TokenRecuperacion token);

    /** Busca por el hash SHA-256 del token (ver {@link TokenRecuperacion#hash(String)}). */
    Optional<TokenRecuperacion> findByHashToken(String hashToken);

    /**
     * Marca el enlace como usado solo si todavía no lo estaba, en una sola operación atómica.
     *
     * @return {@code false} si otra solicitud ya lo había usado: así sirve una sola vez aunque
     *         lleguen dos solicitudes al mismo tiempo
     */
    boolean marcarUsado(Long id, Instant ahora);

    /** Anula los enlaces pendientes de un usuario: solo vale el último que pidió. */
    void anularPendientes(Long usuarioId, Instant ahora);
}
