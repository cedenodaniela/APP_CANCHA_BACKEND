package com.canchapp.usuarios.infrastructure;

import com.canchapp.usuarios.application.IniciarSesionUseCase;
import com.canchapp.usuarios.application.RegistrarUsuarioUseCase;
import com.canchapp.usuarios.application.SembrarAdministradorInicialUseCase;
import com.canchapp.usuarios.application.RestablecerContrasenaUseCase;
import com.canchapp.usuarios.application.SolicitarRecuperacionUseCase;
import com.canchapp.usuarios.domain.CodificadorContrasena;
import com.canchapp.usuarios.domain.EmisorToken;
import com.canchapp.usuarios.domain.EnviadorCorreoRecuperacion;
import com.canchapp.usuarios.domain.GeneradorToken;
import com.canchapp.usuarios.domain.TokenRecuperacionRepository;
import com.canchapp.usuarios.domain.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Declara los casos de uso del módulo {@code usuarios} como beans de Spring.
 * <p>
 * Los casos de uso son Java puro (AGENTS.md §4): Spring se agrega aquí, en infraestructura,
 * y no en la capa {@code application}.
 */
@Configuration
public class ConfiguracionUsuarios {

    @Bean
    public RegistrarUsuarioUseCase registrarUsuarioUseCase(UsuarioRepository usuarios,
                                                           CodificadorContrasena codificador,
                                                           Clock reloj) {
        return new RegistrarUsuarioUseCase(usuarios, codificador, reloj);
    }

    @Bean
    public SembrarAdministradorInicialUseCase sembrarAdministradorInicialUseCase(UsuarioRepository usuarios,
                                                                                 CodificadorContrasena codificador,
                                                                                 Clock reloj) {
        return new SembrarAdministradorInicialUseCase(usuarios, codificador, reloj);
    }
  
    public SolicitarRecuperacionUseCase solicitarRecuperacionUseCase(
            UsuarioRepository usuarios, TokenRecuperacionRepository tokens, GeneradorToken generador,
            EnviadorCorreoRecuperacion correo, Clock reloj,
            @Value("${canchapp.recuperacion.url-enlace}") String urlEnlace) {
        return new SolicitarRecuperacionUseCase(usuarios, tokens, generador, correo, reloj, urlEnlace);
    }

    @Bean
    public RestablecerContrasenaUseCase restablecerContrasenaUseCase(UsuarioRepository usuarios,
                                                                     TokenRecuperacionRepository tokens,
                                                                     CodificadorContrasena codificador,
                                                                     Clock reloj) {
        return new RestablecerContrasenaUseCase(usuarios, tokens, codificador, reloj);
    }

    @Bean
    public IniciarSesionUseCase iniciarSesionUseCase(UsuarioRepository usuarios,
                                                     CodificadorContrasena codificador,
                                                     EmisorToken emisor) {
        return new IniciarSesionUseCase(usuarios, codificador, emisor);
    }
  
}
