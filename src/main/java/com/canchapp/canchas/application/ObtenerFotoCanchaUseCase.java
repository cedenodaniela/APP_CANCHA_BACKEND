package com.canchapp.canchas.application;

import com.canchapp.canchas.domain.AlmacenamientoImagenes;
import com.canchapp.canchas.domain.Cancha;
import com.canchapp.canchas.domain.CanchaNoEncontradaException;
import com.canchapp.canchas.domain.CanchaRepository;

import java.util.Optional;

/** Devuelve la foto WebP de una cancha, para el endpoint autenticado (RF-04). */
public class ObtenerFotoCanchaUseCase {

    private final CanchaRepository canchas;
    private final AlmacenamientoImagenes almacenamiento;

    public ObtenerFotoCanchaUseCase(CanchaRepository canchas, AlmacenamientoImagenes almacenamiento) {
        this.canchas = canchas;
        this.almacenamiento = almacenamiento;
    }

    /**
     * @return vacío si la cancha no tiene foto
     * @throws CanchaNoEncontradaException si la cancha no existe, o si está inactiva y no se incluyen
     */
    public Optional<byte[]> ejecutar(Long id, boolean incluirInactivas) {
        Cancha cancha = canchas.findById(id)
                .filter(c -> incluirInactivas || c.isActiva())
                .orElseThrow(CanchaNoEncontradaException::new);
        return cancha.tieneFoto() ? almacenamiento.leer(cancha.getClaveFoto()) : Optional.empty();
    }
}
