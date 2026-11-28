package com.canchapp.canchas.application;

import com.canchapp.canchas.domain.AlmacenamientoImagenes;
import com.canchapp.canchas.domain.Cancha;
import com.canchapp.canchas.domain.CanchaNoEncontradaException;
import com.canchapp.canchas.domain.CanchaRepository;
import com.canchapp.canchas.domain.ConvertidorWebp;
import com.canchapp.canchas.domain.ImagenInvalidaException;

import java.time.Clock;

/**
 * Sube o reemplaza la foto de una cancha (RF-04): se acepta JPG o PNG de hasta 5 MB y se guarda
 * convertida a WebP. La foto anterior se borra después de guardar la nueva.
 *
 * @see "RF-04: la foto se sube en JPG o PNG y se guarda convertida a WebP"
 */
public class SubirFotoCanchaUseCase {

    /** Límite de las fotos de la app (AGENTS.md §6). */
    public static final int BYTES_MAXIMOS = 5 * 1024 * 1024;

    private final CanchaRepository canchas;
    private final ConvertidorWebp convertidor;
    private final AlmacenamientoImagenes almacenamiento;
    private final Clock reloj;

    public SubirFotoCanchaUseCase(CanchaRepository canchas, ConvertidorWebp convertidor,
                                  AlmacenamientoImagenes almacenamiento, Clock reloj) {
        this.canchas = canchas;
        this.convertidor = convertidor;
        this.almacenamiento = almacenamiento;
        this.reloj = reloj;
    }

    /**
     * @throws CanchaNoEncontradaException si la cancha no existe
     * @throws ImagenInvalidaException si no es JPG ni PNG, está vacía o pasa de 5 MB
     */
    public Cancha ejecutar(Long id, byte[] imagen) {
        Cancha actual = canchas.findById(id).orElseThrow(CanchaNoEncontradaException::new);
        validar(imagen);

        String clave = almacenamiento.guardar(convertidor.convertir(imagen));
        Cancha conFoto = canchas.save(actual.conFoto(clave, reloj.instant()));
        if (actual.tieneFoto()) {
            almacenamiento.eliminar(actual.getClaveFoto());
        }
        return conFoto;
    }

    /** Revisa la firma de los bytes, no la extensión ni el tipo que declara el cliente. */
    static void validar(byte[] imagen) {
        if (imagen == null || imagen.length == 0) {
            throw new ImagenInvalidaException("Adjunta la imagen.");
        }
        if (imagen.length > BYTES_MAXIMOS) {
            throw new ImagenInvalidaException("La imagen debe pesar como máximo 5 MB.");
        }
        if (!esJpg(imagen) && !esPng(imagen)) {
            throw new ImagenInvalidaException("La imagen debe ser JPG o PNG.");
        }
    }

    private static boolean esJpg(byte[] b) {
        return b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
    }

    private static boolean esPng(byte[] b) {
        byte[] firma = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        if (b.length < firma.length) {
            return false;
        }
        for (int i = 0; i < firma.length; i++) {
            if (b[i] != firma[i]) {
                return false;
            }
        }
        return true;
    }
}
