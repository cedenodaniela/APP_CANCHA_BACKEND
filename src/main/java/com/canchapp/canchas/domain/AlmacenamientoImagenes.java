package com.canchapp.canchas.domain;

import java.util.Optional;

/**
 * Puerto para guardar imágenes ya convertidas a WebP (RF-04). Hoy es el disco local; si se cambia a
 * Firebase Storage (DERCAS §2.2.2), solo cambia el adaptador.
 * <p>
 * Las imágenes nunca tienen una URL pública: se sirven por un endpoint que exige sesión.
 */
public interface AlmacenamientoImagenes {

    /** @return la clave con la que se vuelve a leer o se borra */
    String guardar(byte[] webp);

    Optional<byte[]> leer(String clave);

    void eliminar(String clave);
}
