package com.canchapp.usuarios.domain;

/**
 * Puerto para codificar y comprobar contraseñas (BCrypt en infraestructura, RNF-02).
 * <p>
 * Existe para que la contraseña se codifique <b>una sola vez</b>, en el caso de uso,
 * y nunca al guardar: volver a codificar un hash dejaría la cuenta sin acceso.
 */
public interface CodificadorContrasena {

    /** Devuelve el hash de una contraseña en texto plano. */
    String codificar(String contrasenaPlana);

    /** Indica si la contraseña en texto plano corresponde al hash guardado. */
    boolean coincide(String contrasenaPlana, String hash);
}
