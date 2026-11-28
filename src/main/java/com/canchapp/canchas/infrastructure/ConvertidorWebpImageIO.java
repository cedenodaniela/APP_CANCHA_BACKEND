package com.canchapp.canchas.infrastructure;

import com.canchapp.canchas.domain.ConvertidorWebp;
import com.canchapp.canchas.domain.ImagenInvalidaException;
import org.springframework.stereotype.Component;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;

/**
 * Convierte JPG o PNG a WebP con ImageIO y la librería {@code webp-imageio} (RF-04).
 * <p>
 * Reduce las fotos a un ancho máximo de {@value #ANCHO_MAXIMO} px: una foto de celular pesa
 * varios MB y en la app se muestra en una tarjeta.
 */
@Component
public class ConvertidorWebpImageIO implements ConvertidorWebp {

    static final int ANCHO_MAXIMO = 1600;
    private static final float CALIDAD = 0.8f;

    @Override
    public byte[] convertir(byte[] imagen) {
        BufferedImage original = leer(imagen);
        BufferedImage reducida = reducir(original);
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream();
             ImageOutputStream flujo = ImageIO.createImageOutputStream(salida)) {
            ImageWriter escritor = escritorWebp();
            try {
                ImageWriteParam parametros = escritor.getDefaultWriteParam();
                parametros.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                parametros.setCompressionType(parametros.getCompressionTypes()[0]);
                parametros.setCompressionQuality(CALIDAD);
                escritor.setOutput(flujo);
                escritor.write(null, new IIOImage(reducida, null, null), parametros);
            } finally {
                escritor.dispose();
            }
            flujo.flush();
            return salida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo convertir la imagen a WebP", e);
        }
    }

    private static BufferedImage leer(byte[] imagen) {
        try {
            BufferedImage leida = ImageIO.read(new ByteArrayInputStream(imagen));
            if (leida == null) {
                throw new ImagenInvalidaException("No se pudo leer la imagen. Usa una foto JPG o PNG.");
            }
            return leida;
        } catch (IOException e) {
            throw new ImagenInvalidaException("No se pudo leer la imagen. Usa una foto JPG o PNG.");
        }
    }

    /** Achica a {@link #ANCHO_MAXIMO} manteniendo la proporción, y quita la transparencia. */
    private static BufferedImage reducir(BufferedImage original) {
        double escala = Math.min(1.0, (double) ANCHO_MAXIMO / original.getWidth());
        int ancho = Math.max(1, (int) Math.round(original.getWidth() * escala));
        int alto = Math.max(1, (int) Math.round(original.getHeight() * escala));
        BufferedImage destino = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = destino.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setColor(java.awt.Color.WHITE);
            g.fillRect(0, 0, ancho, alto);
            g.drawImage(original, 0, 0, ancho, alto, null);
        } finally {
            g.dispose();
        }
        return destino;
    }

    private static ImageWriter escritorWebp() {
        Iterator<ImageWriter> escritores = ImageIO.getImageWritersByMIMEType("image/webp");
        if (!escritores.hasNext()) {
            throw new IllegalStateException("Falta la librería webp-imageio para escribir WebP.");
        }
        return escritores.next();
    }
}
