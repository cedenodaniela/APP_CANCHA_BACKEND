package com.canchapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la API REST de CanchApp (Complejo Deportivo Calceta).
 * <p>
 * Es un monolito modular con Arquitectura Limpia: cada paquete de {@code com.canchapp}
 * es un módulo con sus capas {@code domain}, {@code application}, {@code infrastructure}
 * y {@code web}. Spring encuentra los componentes de todos los módulos porque viven
 * debajo de este paquete.
 */
@SpringBootApplication
public class CanchAppBackendApplication {

    /**
     * Arranca el servidor embebido en el puerto de {@code server.port} (8080 por defecto).
     *
     * @param args argumentos de línea de comandos que Spring Boot interpreta como propiedades
     */
    public static void main(String[] args) {
        SpringApplication.run(CanchAppBackendApplication.class, args);
    }
}
