package com.canchapp.canchas.infrastructure;

import com.canchapp.canchas.domain.AlmacenamientoImagenes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Guarda las imágenes WebP en una carpeta del servidor ({@code CANCHAPP_DIR_IMAGENES}).
 * <p>
 * Es el adaptador de desarrollo de {@link AlmacenamientoImagenes}: cuando exista el proyecto de
 * Firebase Storage (DERCAS §2.2.2), se reemplaza solo esta clase. Las claves son UUID generados
 * aquí, así que nadie puede pedir un archivo fuera de la carpeta.
 */
@Component
public class AlmacenamientoImagenesLocal implements AlmacenamientoImagenes {

    private static final Pattern FORMATO_CLAVE = Pattern.compile("^[0-9a-f-]{36}\\.webp$");

    private final Path carpeta;

    public AlmacenamientoImagenesLocal(@Value("${canchapp.imagenes.directorio}") String carpeta) {
        this.carpeta = Path.of(carpeta).toAbsolutePath().normalize();
    }

    @Override
    public String guardar(byte[] webp) {
        String clave = UUID.randomUUID() + ".webp";
        try {
            Files.createDirectories(carpeta);
            Files.write(carpeta.resolve(clave), webp);
            return clave;
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar la imagen", e);
        }
    }

    @Override
    public Optional<byte[]> leer(String clave) {
        Path archivo = ruta(clave);
        try {
            return archivo != null && Files.exists(archivo) ? Optional.of(Files.readAllBytes(archivo)) : Optional.empty();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer la imagen", e);
        }
    }

    @Override
    public void eliminar(String clave) {
        Path archivo = ruta(clave);
        if (archivo == null) {
            return;
        }
        try {
            Files.deleteIfExists(archivo);
        } catch (IOException e) {
            // Una foto vieja que no se pudo borrar no debe impedir guardar la nueva.
        }
    }

    private Path ruta(String clave) {
        return clave != null && FORMATO_CLAVE.matcher(clave).matches() ? carpeta.resolve(clave) : null;
    }
}
