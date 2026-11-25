package com.canchapp.usuarios.domain;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Cuenta de usuario del sistema (CLIENTE o ADMINISTRADOR).
 * <p>
 * Java puro: no conoce Spring, JPA ni BCrypt. Guarda solo el <b>hash</b> de la contraseña;
 * la codificación la hace el puerto {@link CodificadorContrasena} una sola vez, al registrar.
 * El estado no se cambia con setters desde fuera (AGENTS.md §4).
 *
 * @see "RF-01 · Registro e inicio de sesión"
 */
public final class Usuario {

    /** RF-01: la contraseña tiene al menos 8 caracteres. */
    public static final int LONGITUD_MINIMA_CONTRASENA = 8;

    private static final Pattern FORMATO_CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final Long id;
    private final String nombre;
    private final String apellido;
    private final String email;
    private final String telefono;
    private final String hashContrasena;
    private final Rol rol;
    private final boolean activo;
    private final Instant fechaCreacion;
    private final Instant fechaActualizacion;

    private Usuario(Long id, String nombre, String apellido, String email, String telefono,
                    String hashContrasena, Rol rol, boolean activo,
                    Instant fechaCreacion, Instant fechaActualizacion) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.telefono = telefono;
        this.hashContrasena = hashContrasena;
        this.rol = rol;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }

    /**
     * Crea una cuenta nueva desde el registro público. Siempre es {@link Rol#CLIENTE}.
     *
     * @param hashContrasena contraseña ya codificada por {@link CodificadorContrasena}
     * @see "RF-01: el registro crea únicamente cuentas de cliente"
     */
    public static Usuario registrarCliente(String nombre, String apellido, String email, String telefono,
                                           String hashContrasena, Instant ahora) {
        return new Usuario(null, requerido(nombre, "nombre"), requerido(apellido, "apellido"),
                normalizarCorreo(email), vacioComoNulo(telefono),
                Objects.requireNonNull(hashContrasena, "hashContrasena"), Rol.CLIENTE, true, ahora, ahora);
    }

    /** Reconstruye un usuario ya guardado. Solo lo usan los adaptadores de persistencia. */
    public static Usuario reconstituir(Long id, String nombre, String apellido, String email, String telefono,
                                       String hashContrasena, Rol rol, boolean activo,
                                       Instant fechaCreacion, Instant fechaActualizacion) {
        return new Usuario(id, nombre, apellido, email, telefono, hashContrasena, rol, activo,
                fechaCreacion, fechaActualizacion);
    }

    /**
     * Valida la contraseña en texto plano antes de codificarla.
     *
     * @throws DatosInvalidosException si tiene menos de {@value #LONGITUD_MINIMA_CONTRASENA} caracteres
     */
    public static void validarContrasena(String contrasenaPlana) {
        if (contrasenaPlana == null || contrasenaPlana.length() < LONGITUD_MINIMA_CONTRASENA) {
            throw new DatosInvalidosException(
                    "La contraseña debe tener al menos " + LONGITUD_MINIMA_CONTRASENA + " caracteres.");
        }
    }

    /**
     * Normaliza el correo (sin espacios y en minúsculas) para que «Ana@Correo.com» y
     * «ana@correo.com» sean la misma cuenta.
     *
     * @throws DatosInvalidosException si el formato no es de un correo
     */
    public static String normalizarCorreo(String email) {
        String limpio = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (!FORMATO_CORREO.matcher(limpio).matches()) {
            throw new DatosInvalidosException("Ingresa un correo electrónico válido.");
        }
        return limpio;
    }

    /**
     * Devuelve la misma cuenta con una contraseña nueva (RF-02). El usuario no se modifica:
     * el dominio no tiene setters.
     *
     * @param nuevoHash contraseña ya validada con {@link #validarContrasena(String)} y codificada
     */
    public Usuario cambiarContrasena(String nuevoHash, Instant ahora) {
        return new Usuario(id, nombre, apellido, email, telefono,
                Objects.requireNonNull(nuevoHash, "nuevoHash"), rol, activo, fechaCreacion, ahora);
    }

    /** Solo una cuenta activa y con contraseña definida puede iniciar sesión. */
    public boolean puedeIniciarSesion() {
        return activo && hashContrasena != null;
    }

    private static String requerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new DatosInvalidosException("El campo " + campo + " es obligatorio.");
        }
        return valor.trim();
    }

    private static String vacioComoNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
    public String getHashContrasena() { return hashContrasena; }
    public Rol getRol() { return rol; }
    public boolean isActivo() { return activo; }
    public Instant getFechaCreacion() { return fechaCreacion; }
    public Instant getFechaActualizacion() { return fechaActualizacion; }
}
