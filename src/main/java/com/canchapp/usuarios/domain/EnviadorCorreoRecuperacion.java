package com.canchapp.usuarios.domain;

/**
 * Puerto que envía el correo de recuperación de contraseña (RF-02). SMTP en infraestructura.
 * <p>
 * No lanza excepciones si el envío falla: el caso de uso responde siempre lo mismo para no
 * revelar si la cuenta existe, así que el adaptador registra el fallo en el log.
 */
public interface EnviadorCorreoRecuperacion {

    void enviar(String email, String nombre, String enlace);
}
