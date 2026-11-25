package com.canchapp.usuarios.infrastructure;

import com.canchapp.usuarios.domain.GeneradorToken;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Tokens de 256 bits con {@link SecureRandom}, en Base64 apto para URL (RF-02).
 * Son imposibles de adivinar en los 30 minutos que dura el enlace.
 */
@Component
public class GeneradorTokenSeguro implements GeneradorToken {

    private static final int BYTES = 32;

    private final SecureRandom aleatorio = new SecureRandom();

    @Override
    public String generar() {
        byte[] bytes = new byte[BYTES];
        aleatorio.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
