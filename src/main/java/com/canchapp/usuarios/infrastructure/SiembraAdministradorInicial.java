package com.canchapp.usuarios.infrastructure;

import com.canchapp.shared.domain.ExcepcionNegocio;
import com.canchapp.usuarios.application.SembrarAdministradorInicialUseCase;
import com.canchapp.usuarios.application.SembrarAdministradorInicialUseCase.DatosAdministrador;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Ejecuta {@link SembrarAdministradorInicialUseCase} al arrancar, con las propiedades
 * {@code canchapp.admin-inicial.*} (variables {@code CANCHAPP_ADMIN_*} del {@code .env}).
 * <p>
 * Si las variables no están definidas no siembra nada. Un dato inválido no detiene la API:
 * se registra en el log para que la persona que instala lo corrija.
 *
 * @see "RF-18 · Acceso al panel web"
 */
@Component
@EnableConfigurationProperties(SiembraAdministradorInicial.Propiedades.class)
public class SiembraAdministradorInicial implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(SiembraAdministradorInicial.class);

    /**
     * @param email      correo de la cuenta; si está vacío no se siembra
     * @param contrasena contraseña inicial; se guarda solo su hash BCrypt
     */
    @ConfigurationProperties(prefix = "canchapp.admin-inicial")
    public record Propiedades(String nombre, String apellido, String email, String contrasena) {
    }

    private final SembrarAdministradorInicialUseCase sembrar;
    private final Propiedades propiedades;

    public SiembraAdministradorInicial(SembrarAdministradorInicialUseCase sembrar, Propiedades propiedades) {
        this.sembrar = sembrar;
        this.propiedades = propiedades;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (vacio(propiedades.email()) || vacio(propiedades.contrasena())) {
            LOG.info("CANCHAPP_ADMIN_EMAIL o CANCHAPP_ADMIN_CONTRASENA sin definir: no se siembra el administrador inicial.");
            return;
        }
        try {
            sembrar.ejecutar(new DatosAdministrador(propiedades.nombre(), propiedades.apellido(),
                            propiedades.email(), propiedades.contrasena()))
                    .ifPresentOrElse(
                            admin -> LOG.info("Administrador inicial creado: {}", admin.getEmail()),
                            () -> LOG.info("Ya existe un administrador: no se siembra otro."));
        } catch (ExcepcionNegocio e) {
            LOG.warn("No se pudo sembrar el administrador inicial: {}", e.getMessage());
        }
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
