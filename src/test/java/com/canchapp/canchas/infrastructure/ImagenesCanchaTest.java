package com.canchapp.canchas.infrastructure;

import com.canchapp.canchas.domain.ImagenInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Fotos de canchas · WebP y almacenamiento (RF-04)")
class ImagenesCanchaTest {

    private static byte[] imagen(String formato, int ancho, int alto) throws IOException {
        BufferedImage img = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0x0F6B3E));
        g.fillRect(0, 0, ancho, alto);
        g.dispose();
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        ImageIO.write(img, formato, salida);
        return salida.toByteArray();
    }

    private static boolean esWebp(byte[] b) {
        return new String(Arrays.copyOfRange(b, 0, 4), StandardCharsets.US_ASCII).equals("RIFF")
                && new String(Arrays.copyOfRange(b, 8, 12), StandardCharsets.US_ASCII).equals("WEBP");
    }

    @Test
    @DisplayName("dado un PNG, cuando se convierte, entonces el resultado es WebP")
    void pngAWebp() throws IOException {
        byte[] webp = new ConvertidorWebpImageIO().convertir(imagen("png", 200, 120));

        assertThat(esWebp(webp)).isTrue();
    }

    @Test
    @DisplayName("dado un JPG muy ancho, cuando se convierte, entonces se reduce al ancho máximo")
    void jpgSeReduce() throws IOException {
        byte[] webp = new ConvertidorWebpImageIO().convertir(imagen("jpg", 3200, 1600));

        BufferedImage leida = ImageIO.read(new ByteArrayInputStream(webp));
        assertThat(esWebp(webp)).isTrue();
        assertThat(leida.getWidth()).isEqualTo(ConvertidorWebpImageIO.ANCHO_MAXIMO);
        assertThat(leida.getHeight()).isEqualTo(800);
    }

    @Test
    @DisplayName("dados bytes que no son una imagen, cuando se convierten, entonces se rechazan")
    void noEsImagen() {
        byte[] falso = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1, 2, 3};

        assertThatThrownBy(() -> new ConvertidorWebpImageIO().convertir(falso))
                .isInstanceOf(ImagenInvalidaException.class);
    }

    @Test
    @DisplayName("dada una imagen guardada, cuando se lee y se borra, entonces se recupera y luego desaparece")
    void almacenamientoLocal(@TempDir Path carpeta) {
        AlmacenamientoImagenesLocal almacenamiento = new AlmacenamientoImagenesLocal(carpeta.toString());

        String clave = almacenamiento.guardar(new byte[] {1, 2, 3});
        assertThat(almacenamiento.leer(clave)).hasValue(new byte[] {1, 2, 3});

        almacenamiento.eliminar(clave);
        assertThat(almacenamiento.leer(clave)).isEmpty();
    }

    @Test
    @DisplayName("dada una clave que intenta salir de la carpeta, cuando se lee, entonces no devuelve nada")
    void claveFueraDeLaCarpeta(@TempDir Path carpeta) {
        AlmacenamientoImagenesLocal almacenamiento = new AlmacenamientoImagenesLocal(carpeta.toString());

        assertThat(almacenamiento.leer("../../.env")).isEmpty();
    }
}
