package com.canchapp.canchas.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Cancha del complejo (RF-04).
 * <p>
 * Java puro y sin setters: cada cambio devuelve una cancha nueva. Una cancha inactiva sigue
 * existiendo (para su historial de reservas), pero no se ofrece al cliente (RF-06).
 *
 * @see "RF-04 · Gestión de canchas"
 */
public final class Cancha {

    private final Long id;
    private final String nombre;
    private final String tipoCesped;
    private final int anchoMetros;
    private final int largoMetros;
    private final String claveFoto;
    private final boolean activa;
    private final Instant creadaEn;
    private final Instant actualizadaEn;

    private Cancha(Long id, String nombre, String tipoCesped, int anchoMetros, int largoMetros, String claveFoto,
                   boolean activa, Instant creadaEn, Instant actualizadaEn) {
        this.id = id;
        this.nombre = nombre;
        this.tipoCesped = tipoCesped;
        this.anchoMetros = anchoMetros;
        this.largoMetros = largoMetros;
        this.claveFoto = claveFoto;
        this.activa = activa;
        this.creadaEn = creadaEn;
        this.actualizadaEn = actualizadaEn;
    }

    /** Alta de una cancha: nace activa y sin foto. */
    public static Cancha nueva(String nombre, String tipoCesped, Integer anchoMetros, Integer largoMetros, Instant ahora) {
        return new Cancha(null, requerido(nombre, "el nombre de la cancha", 60), requerido(tipoCesped, "el tipo de césped", 40),
                positivo(anchoMetros, "El ancho"), positivo(largoMetros, "El largo"), null, true, ahora, ahora);
    }

    /** Reconstruye una cancha guardada. Solo lo usan los adaptadores de persistencia. */
    public static Cancha reconstituir(Long id, String nombre, String tipoCesped, int anchoMetros, int largoMetros,
                                      String claveFoto, boolean activa, Instant creadaEn, Instant actualizadaEn) {
        return new Cancha(id, nombre, tipoCesped, anchoMetros, largoMetros, claveFoto, activa, creadaEn, actualizadaEn);
    }

    /** Cambia nombre, césped y dimensiones; conserva la foto y el estado. */
    public Cancha editar(String nombre, String tipoCesped, Integer anchoMetros, Integer largoMetros, Instant ahora) {
        return new Cancha(id, requerido(nombre, "el nombre de la cancha", 60), requerido(tipoCesped, "el tipo de césped", 40),
                positivo(anchoMetros, "El ancho"), positivo(largoMetros, "El largo"), claveFoto, activa, creadaEn, ahora);
    }

    /** @param claveFoto clave de la imagen WebP en {@link AlmacenamientoImagenes} */
    public Cancha conFoto(String claveFoto, Instant ahora) {
        return new Cancha(id, nombre, tipoCesped, anchoMetros, largoMetros,
                Objects.requireNonNull(claveFoto, "claveFoto"), activa, creadaEn, ahora);
    }

    public Cancha activar(Instant ahora) {
        return new Cancha(id, nombre, tipoCesped, anchoMetros, largoMetros, claveFoto, true, creadaEn, ahora);
    }

    public Cancha desactivar(Instant ahora) {
        return new Cancha(id, nombre, tipoCesped, anchoMetros, largoMetros, claveFoto, false, creadaEn, ahora);
    }

    public boolean tieneFoto() {
        return claveFoto != null;
    }

    private static String requerido(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) {
            throw new CanchaInvalidaException("Ingresa " + campo + ".");
        }
        String limpio = valor.trim();
        if (limpio.length() > maximo) {
            throw new CanchaInvalidaException("El campo " + campo + " admite hasta " + maximo + " caracteres.");
        }
        return limpio;
    }

    private static int positivo(Integer metros, String medida) {
        if (metros == null || metros <= 0) {
            throw new CanchaInvalidaException(medida + " de la cancha debe ser mayor que 0 metros.");
        }
        return metros;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getTipoCesped() { return tipoCesped; }
    public int getAnchoMetros() { return anchoMetros; }
    public int getLargoMetros() { return largoMetros; }
    public String getClaveFoto() { return claveFoto; }
    public boolean isActiva() { return activa; }
    public Instant getCreadaEn() { return creadaEn; }
    public Instant getActualizadaEn() { return actualizadaEn; }
}
