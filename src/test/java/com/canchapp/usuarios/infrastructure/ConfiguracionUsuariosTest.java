package com.canchapp.usuarios.infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Las pruebas de los controladores reemplazan los casos de uso por mocks, así que no notan si
 * a uno le falta su {@code @Bean} y la API no arranca. Esta prueba lo revisa sin base de datos.
 */
@DisplayName("ConfiguracionUsuarios")
class ConfiguracionUsuariosTest {

    @Test
    @DisplayName("dado cada caso de uso del módulo, cuando arranca la API, entonces tiene su @Bean")
    void cadaCasoDeUsoEsUnBean() {
        List<String> sinBean = Arrays.stream(ConfiguracionUsuarios.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .filter(m -> m.getReturnType().getSimpleName().endsWith("UseCase"))
                .filter(m -> !m.isAnnotationPresent(Bean.class))
                .map(Method::getName)
                .toList();

        assertThat(sinBean).as("métodos sin @Bean").isEmpty();
    }
}
