package com.canchapp.shared.infrastructure.seguridad;

import com.canchapp.shared.web.ErrorRespuesta;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Seguridad de la API (RNF-02).
 * <ul>
 *   <li>Sin sesión de servidor ni CSRF: cada solicitud trae su JWT en {@code Authorization: Bearer}.</li>
 *   <li>Públicos solo el inicio de sesión y el registro (RF-01) y la recuperación de contraseña
 *       (RF-02). Todo lo demás exige token.</li>
 *   <li>El claim {@code rol} se convierte en {@code ROLE_CLIENTE} o {@code ROLE_ADMINISTRADOR},
 *       para autorizar por rol en cada endpoint con {@code @PreAuthorize}.</li>
 *   <li>Los errores 401 y 403 usan el mismo cuerpo {@code {codigo, mensaje}} que el resto de la API.</li>
 * </ul>
 */
@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(PropiedadesSeguridad.class)
public class ConfiguracionSeguridad {

    public static final String MENSAJE_SIN_SESION = "Debes iniciar sesión para continuar.";
    public static final String MENSAJE_SIN_PERMISO = "No tienes permiso para realizar esta acción.";

    @Bean
    public SecurityFilterChain filtroSeguridad(HttpSecurity http, ObjectMapper json) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/usuarios",
                                "/api/v1/auth/recuperacion", "/api/v1/auth/recuperacion/restablecer").permitAll()
                        // Página del botón del correo de recuperación, que abre la app (RF-02).
                        .requestMatchers(HttpMethod.GET, "/recuperar-contrasena").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(rs -> rs
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(convertidorRoles()))
                        .authenticationEntryPoint((req, res, ex) ->
                                escribirError(res, json, HttpServletResponse.SC_UNAUTHORIZED,
                                        "NO_AUTENTICADO", MENSAJE_SIN_SESION))
                        .accessDeniedHandler((req, res, ex) ->
                                escribirError(res, json, HttpServletResponse.SC_FORBIDDEN,
                                        "SIN_PERMISO", MENSAJE_SIN_PERMISO)))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                escribirError(res, json, HttpServletResponse.SC_UNAUTHORIZED,
                                        "NO_AUTENTICADO", MENSAJE_SIN_SESION))
                        .accessDeniedHandler((req, res, ex) ->
                                escribirError(res, json, HttpServletResponse.SC_FORBIDDEN,
                                        "SIN_PERMISO", MENSAJE_SIN_PERMISO)));
        return http.build();
    }

    /** BCrypt para las contraseñas (RNF-02). */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtEncoder jwtEncoder(PropiedadesSeguridad propiedades) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(clave(propiedades)));
    }

    /** Valida firma HS256 y vencimiento ({@code exp}) de cada token. */
    @Bean
    public JwtDecoder jwtDecoder(PropiedadesSeguridad propiedades) {
        return NimbusJwtDecoder.withSecretKey(clave(propiedades)).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(PropiedadesSeguridad propiedades) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(propiedades.corsOrigenes());
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", cors);
        return fuente;
    }

    private static JwtAuthenticationConverter convertidorRoles() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("rol");
        roles.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter convertidor = new JwtAuthenticationConverter();
        convertidor.setJwtGrantedAuthoritiesConverter(roles);
        return convertidor;
    }

    private static SecretKey clave(PropiedadesSeguridad propiedades) {
        return new SecretKeySpec(propiedades.jwtSecreto().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    private static void escribirError(HttpServletResponse res, ObjectMapper json, int estado,
                                      String codigo, String mensaje) throws IOException {
        res.setStatus(estado);
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        json.writeValue(res.getOutputStream(), new ErrorRespuesta(codigo, mensaje));
    }
}
