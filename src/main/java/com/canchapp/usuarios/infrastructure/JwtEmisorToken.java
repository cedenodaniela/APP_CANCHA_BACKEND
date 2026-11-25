package com.canchapp.usuarios.infrastructure;

import com.canchapp.shared.infrastructure.seguridad.PropiedadesSeguridad;
import com.canchapp.usuarios.domain.EmisorToken;
import com.canchapp.usuarios.domain.Usuario;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * Emite el JWT de sesión firmado con HS256 (RF-01, RNF-02).
 * <p>
 * Claims: {@code sub} = id del usuario, {@code rol}, {@code nombre}, {@code email} y un
 * {@code jti} único, que servirá para revocar el token al cerrar sesión.
 */
@Component
public class JwtEmisorToken implements EmisorToken {

    public static final String CLAIM_ROL = "rol";
    public static final String CLAIM_NOMBRE = "nombre";
    public static final String CLAIM_EMAIL = "email";

    private final JwtEncoder encoder;
    private final PropiedadesSeguridad propiedades;
    private final Clock reloj;

    public JwtEmisorToken(JwtEncoder encoder, PropiedadesSeguridad propiedades, Clock reloj) {
        this.encoder = encoder;
        this.propiedades = propiedades;
        this.reloj = reloj;
    }

    @Override
    public TokenEmitido emitir(Usuario usuario) {
        Instant ahora = reloj.instant();
        Instant expira = ahora.plus(propiedades.jwtVigencia());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("canchapp")
                .subject(String.valueOf(usuario.getId()))
                .issuedAt(ahora)
                .expiresAt(expira)
                .id(UUID.randomUUID().toString())
                .claim(CLAIM_ROL, usuario.getRol().name())
                .claim(CLAIM_NOMBRE, usuario.getNombre())
                .claim(CLAIM_EMAIL, usuario.getEmail())
                .build();
        JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).build();
        String valor = encoder.encode(JwtEncoderParameters.from(cabecera, claims)).getTokenValue();
        return new TokenEmitido(valor, expira);
    }
}
