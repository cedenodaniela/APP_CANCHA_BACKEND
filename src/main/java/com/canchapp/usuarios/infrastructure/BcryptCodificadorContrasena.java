package com.canchapp.usuarios.infrastructure;

import com.canchapp.usuarios.domain.CodificadorContrasena;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Adaptador de {@link CodificadorContrasena} con el {@link PasswordEncoder} BCrypt
 * declarado en la configuración de seguridad (RNF-02).
 */
@Component
public class BcryptCodificadorContrasena implements CodificadorContrasena {

    private final PasswordEncoder encoder;

    public BcryptCodificadorContrasena(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public String codificar(String contrasenaPlana) {
        return encoder.encode(contrasenaPlana);
    }

    @Override
    public boolean coincide(String contrasenaPlana, String hash) {
        return hash != null && encoder.matches(contrasenaPlana, hash);
    }
}
