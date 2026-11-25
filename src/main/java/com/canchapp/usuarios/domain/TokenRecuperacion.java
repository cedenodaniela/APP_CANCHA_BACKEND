package com.canchapp.usuarios.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Enlace de recuperación de contraseña (RF-02).
 * <p>
 * Solo se guarda el <b>hash SHA-256</b> del token: quien lea la base de datos no puede usar
 * los enlaces vigentes. El token en claro viaja una sola vez, en el correo.
 *
 * @see "RF-02: el enlace vence a los 30 minutos y sirve una sola vez"
 */
public final class TokenRecuperacion {

    /** RF-02: el enlace vence a los 30 minutos. */
    public static final Duration VIGENCIA = Duration.ofMinutes(30);

    private final Long id;
    private final Long usuarioId;
    private final String hashToken;
    private final Instant creadoEn;
    private final Instant venceEn;
    private final Instant usadoEn;

    private TokenRecuperacion(Long id, Long usuarioId, String hashToken, Instant creadoEn, Instant venceEn,
                              Instant usadoEn) {
        this.id = id;
        this.usuarioId = Objects.requireNonNull(usuarioId, "usuarioId");
        this.hashToken = Objects.requireNonNull(hashToken, "hashToken");
        this.creadoEn = creadoEn;
        this.venceEn = venceEn;
        this.usadoEn = usadoEn;
    }

    /** Emite un enlace nuevo que vence {@link #VIGENCIA} después de {@code ahora}. */
    public static TokenRecuperacion emitir(Long usuarioId, String tokenEnClaro, Instant ahora) {
        return new TokenRecuperacion(null, usuarioId, hash(tokenEnClaro), ahora, ahora.plus(VIGENCIA), null);
    }

    /** Reconstruye un enlace guardado. Solo lo usan los adaptadores de persistencia. */
    public static TokenRecuperacion reconstituir(Long id, Long usuarioId, String hashToken, Instant creadoEn,
                                                 Instant venceEn, Instant usadoEn) {
        return new TokenRecuperacion(id, usuarioId, hashToken, creadoEn, venceEn, usadoEn);
    }

    /**
     * Comprueba que el enlace todavía sirve.
     *
     * @throws EnlaceRecuperacionInvalidoException si ya se usó o ya venció
     */
    public void verificarVigente(Instant ahora) {
        if (usadoEn != null || !ahora.isBefore(venceEn)) {
            throw new EnlaceRecuperacionInvalidoException();
        }
    }

    /** Hash SHA-256 en hexadecimal con el que se guarda y se busca un token. */
    public static String hash(String tokenEnClaro) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(Objects.requireNonNull(tokenEnClaro, "token").getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Toda JVM incluye SHA-256", e);
        }
    }

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public String getHashToken() { return hashToken; }
    public Instant getCreadoEn() { return creadoEn; }
    public Instant getVenceEn() { return venceEn; }
    public Instant getUsadoEn() { return usadoEn; }
}
