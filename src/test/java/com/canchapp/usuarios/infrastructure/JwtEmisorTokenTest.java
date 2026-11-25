package com.canchapp.usuarios.infrastructure;

import com.canchapp.shared.infrastructure.seguridad.ConfiguracionSeguridad;
import com.canchapp.shared.infrastructure.seguridad.PropiedadesSeguridad;
import com.canchapp.usuarios.domain.EmisorToken.TokenEmitido;
import com.canchapp.usuarios.domain.Rol;
import com.canchapp.usuarios.domain.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtEmisorToken (RF-01, RNF-02)")
class JwtEmisorTokenTest {

    @Test
    @DisplayName("dado un usuario, cuando se emite su token, entonces el backend lo valida y vence a las 24 h")
    void emiteTokenValidoPor24Horas() {
        PropiedadesSeguridad propiedades = new PropiedadesSeguridad(
                "clave-de-prueba-de-al-menos-32-caracteres!!", Duration.ofHours(24), List.of());
        ConfiguracionSeguridad config = new ConfiguracionSeguridad();
        Instant ahora = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        JwtEmisorToken emisor = new JwtEmisorToken(config.jwtEncoder(propiedades), propiedades,
                Clock.fixed(ahora, ZoneId.of("America/Guayaquil")));
        Usuario admin = Usuario.reconstituir(3L, "Daniela", "Cedeño", "admin@correo.com", null, "HASH",
                Rol.ADMINISTRADOR, true, ahora, ahora);

        TokenEmitido token = emisor.emitir(admin);
        JwtDecoder decoder = config.jwtDecoder(propiedades);
        Jwt jwt = decoder.decode(token.valor());

        assertThat(jwt.getSubject()).isEqualTo("3");
        assertThat(jwt.getClaimAsString("rol")).isEqualTo("ADMINISTRADOR");
        assertThat(jwt.getClaimAsString("nombre")).isEqualTo("Daniela");
        assertThat(jwt.getId()).isNotBlank();
        assertThat(jwt.getExpiresAt()).isEqualTo(ahora.plus(Duration.ofHours(24))).isEqualTo(token.expiraEn());
    }
}
