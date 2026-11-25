package com.canchapp.usuarios.infrastructure;

import com.canchapp.usuarios.domain.CorreoYaRegistradoException;
import com.canchapp.usuarios.domain.Rol;
import com.canchapp.usuarios.domain.Usuario;
import com.canchapp.usuarios.infrastructure.entity.UserEntity;
import com.canchapp.usuarios.infrastructure.entity.UserEntityRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("UserJpaAdapter")
class UserJpaAdapterTest {

    private static final Instant AHORA = Instant.parse("2026-11-24T15:00:00Z");

    @Test
    @DisplayName("dado un usuario ya guardado, cuando se vuelve a guardar, entonces su hash no cambia")
    void guardarNoVuelveACodificar() {
        UserEntityRepository jpa = mock(UserEntityRepository.class);
        when(jpa.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
        UserJpaAdapter adaptador = new UserJpaAdapter(jpa);
        Usuario existente = Usuario.reconstituir(1L, "Ana", "Pérez", "ana@correo.com", null,
                "$2a$10$hashOriginal", Rol.CLIENTE, false, AHORA, AHORA);

        Usuario guardado = adaptador.save(adaptador.save(existente));

        assertThat(guardado.getHashContrasena()).isEqualTo("$2a$10$hashOriginal");
        assertThat(guardado.getFechaCreacion()).isEqualTo(AHORA);
    }

    @Test
    @DisplayName("dados dos registros simultáneos del mismo correo, cuando la base lo rechaza, entonces lanza CORREO_YA_REGISTRADO")
    void correoDuplicadoEnBase() {
        UserEntityRepository jpa = mock(UserEntityRepository.class);
        when(jpa.saveAndFlush(any(UserEntity.class))).thenThrow(new DataIntegrityViolationException("uq_usuario_email"));
        UserJpaAdapter adaptador = new UserJpaAdapter(jpa);

        assertThatThrownBy(() -> adaptador.save(
                Usuario.registrarCliente("Ana", "Pérez", "ana@correo.com", null, "HASH", AHORA)))
                .isInstanceOf(CorreoYaRegistradoException.class);
    }
}
