package com.canchapp.canchas.application;

import com.canchapp.canchas.application.CrearCanchaUseCase.DatosCancha;
import com.canchapp.canchas.domain.AlmacenamientoImagenes;
import com.canchapp.canchas.domain.Cancha;
import com.canchapp.canchas.domain.CanchaNoEncontradaException;
import com.canchapp.canchas.domain.CanchaRepository;
import com.canchapp.canchas.domain.ConvertidorWebp;
import com.canchapp.canchas.domain.ImagenInvalidaException;
import com.canchapp.canchas.domain.NombreCanchaRepetidoException;
import com.canchapp.canchas.domain.ReservasActivasDeCancha;
import com.canchapp.canchas.domain.ReservasActivasException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("Canchas · casos de uso (RF-04)")
class CanchasUseCasesTest {

    private static final Instant AHORA = Instant.parse("2026-11-28T15:00:00Z");
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0};
    private static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0};

    private CanchaRepository canchas;
    private ReservasActivasDeCancha reservas;
    private Clock reloj;

    @BeforeEach
    void preparar() {
        canchas = mock(CanchaRepository.class);
        reservas = mock(ReservasActivasDeCancha.class);
        reloj = Clock.fixed(AHORA, ZoneId.of("America/Guayaquil"));
        when(canchas.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private static Cancha cancha(boolean activa, String foto) {
        return Cancha.reconstituir(1L, "Cancha 1", "Sintético", 30, 50, foto, activa, AHORA, AHORA);
    }

    @Test
    @DisplayName("dado un administrador o un cliente, cuando lista, entonces el cliente solo recibe las activas")
    void listar() {
        when(canchas.findAll()).thenReturn(List.of(cancha(true, null), cancha(false, null)));
        when(canchas.findActivas()).thenReturn(List.of(cancha(true, null)));
        ListarCanchasUseCase listar = new ListarCanchasUseCase(canchas);

        assertThat(listar.ejecutar(true)).hasSize(2);
        assertThat(listar.ejecutar(false)).hasSize(1);
    }

    @Test
    @DisplayName("dado un nombre que ya existe, cuando crea, entonces lo rechaza sin guardar")
    void nombreRepetido() {
        when(canchas.existeNombre("Cancha 1", null)).thenReturn(true);

        assertThatThrownBy(() -> new CrearCanchaUseCase(canchas, reloj).ejecutar(new DatosCancha("Cancha 1", "Sintético", 30, 50)))
                .isInstanceOf(NombreCanchaRepetidoException.class);
        verify(canchas, never()).save(any());
    }

    @Test
    @DisplayName("dada una cancha que conserva su nombre, cuando se edita, entonces no choca consigo misma")
    void editarMismoNombre() {
        when(canchas.findById(1L)).thenReturn(Optional.of(cancha(true, null)));
        when(canchas.existeNombre("Cancha 1", 1L)).thenReturn(false);

        Cancha editada = new EditarCanchaUseCase(canchas, reloj).ejecutar(1L, new DatosCancha("Cancha 1", "Sintético", 32, 52));

        assertThat(editada.getAnchoMetros()).isEqualTo(32);
    }

    @Test
    @DisplayName("dada una cancha con reservas activas, cuando la desactiva sin confirmar, entonces avisa cuántas hay y no la cambia")
    void desactivarPideConfirmacion() {
        when(canchas.findById(1L)).thenReturn(Optional.of(cancha(true, null)));
        when(reservas.contar(1L)).thenReturn(2);
        CambiarEstadoCanchaUseCase cambiar = new CambiarEstadoCanchaUseCase(canchas, reservas, reloj);

        assertThatThrownBy(() -> cambiar.ejecutar(1L, false, false))
                .isInstanceOf(ReservasActivasException.class)
                .satisfies(e -> assertThat(((ReservasActivasException) e).getReservasActivas()).isEqualTo(2));
        verify(canchas, never()).save(any());
    }

    @Test
    @DisplayName("dada una cancha con reservas activas, cuando confirma, entonces la desactiva")
    void desactivarConfirmado() {
        when(canchas.findById(1L)).thenReturn(Optional.of(cancha(true, null)));
        when(reservas.contar(1L)).thenReturn(2);

        Cancha cambiada = new CambiarEstadoCanchaUseCase(canchas, reservas, reloj).ejecutar(1L, false, true);

        assertThat(cambiada.isActiva()).isFalse();
    }

    @Test
    @DisplayName("dada una cancha inactiva, cuando se activa, entonces no pregunta por reservas")
    void activar() {
        when(canchas.findById(1L)).thenReturn(Optional.of(cancha(false, null)));

        Cancha cambiada = new CambiarEstadoCanchaUseCase(canchas, reservas, reloj).ejecutar(1L, true, false);

        assertThat(cambiada.isActiva()).isTrue();
        verifyNoInteractions(reservas);
    }

    @Test
    @DisplayName("dada una cancha inactiva, cuando un cliente la pide, entonces para él no existe")
    void inactivaNoVisible() {
        when(canchas.findById(1L)).thenReturn(Optional.of(cancha(false, null)));

        assertThatThrownBy(() -> new ObtenerCanchaUseCase(canchas).ejecutar(1L, false))
                .isInstanceOf(CanchaNoEncontradaException.class);
        assertThat(new ObtenerCanchaUseCase(canchas).ejecutar(1L, true).isActiva()).isFalse();
    }

    @Test
    @DisplayName("dada una foto nueva, cuando la sube, entonces guarda el WebP y borra la anterior")
    void subirFotoReemplaza() {
        ConvertidorWebp convertidor = mock(ConvertidorWebp.class);
        AlmacenamientoImagenes almacenamiento = mock(AlmacenamientoImagenes.class);
        when(canchas.findById(1L)).thenReturn(Optional.of(cancha(true, "vieja.webp")));
        when(convertidor.convertir(JPG)).thenReturn(new byte[] {1});
        when(almacenamiento.guardar(any())).thenReturn("nueva.webp");

        Cancha conFoto = new SubirFotoCanchaUseCase(canchas, convertidor, almacenamiento, reloj).ejecutar(1L, JPG);

        assertThat(conFoto.getClaveFoto()).isEqualTo("nueva.webp");
        verify(almacenamiento).eliminar("vieja.webp");
    }

    @Test
    @DisplayName("dado un archivo que no es JPG ni PNG, cuando lo sube, entonces lo rechaza sin convertir")
    void formatoInvalido() {
        ConvertidorWebp convertidor = mock(ConvertidorWebp.class);
        AlmacenamientoImagenes almacenamiento = mock(AlmacenamientoImagenes.class);
        when(canchas.findById(1L)).thenReturn(Optional.of(cancha(true, null)));
        SubirFotoCanchaUseCase subir = new SubirFotoCanchaUseCase(canchas, convertidor, almacenamiento, reloj);

        assertThatThrownBy(() -> subir.ejecutar(1L, "GIF89a".getBytes()))
                .isInstanceOf(ImagenInvalidaException.class)
                .hasMessage("La imagen debe ser JPG o PNG.");
        assertThatThrownBy(() -> subir.ejecutar(1L, new byte[SubirFotoCanchaUseCase.BYTES_MAXIMOS + 1]))
                .hasMessage("La imagen debe pesar como máximo 5 MB.");
        verifyNoInteractions(convertidor, almacenamiento);
    }

    @Test
    @DisplayName("dado un PNG o un JPG, cuando se valida, entonces se acepta por su firma")
    void firmasValidas() {
        SubirFotoCanchaUseCase.validar(PNG);
        SubirFotoCanchaUseCase.validar(JPG);
    }

    @Test
    @DisplayName("dada una cancha sin foto, cuando se pide la foto, entonces no hay imagen")
    void sinFoto() {
        AlmacenamientoImagenes almacenamiento = mock(AlmacenamientoImagenes.class);
        when(canchas.findById(1L)).thenReturn(Optional.of(cancha(true, null)));

        assertThat(new ObtenerFotoCanchaUseCase(canchas, almacenamiento).ejecutar(1L, false)).isEmpty();
        verify(almacenamiento, never()).leer(anyString());
    }
}
