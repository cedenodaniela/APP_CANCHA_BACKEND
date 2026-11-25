package com.canchapp.usuarios.infrastructure;

import com.canchapp.usuarios.domain.EnviadorCorreoRecuperacion;
import com.canchapp.usuarios.domain.TokenRecuperacion;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;

/**
 * Envía el correo de recuperación por SMTP (RF-02), en texto y en HTML con un botón.
 * <p>
 * Si el envío falla, solo lo registra en el log: la API responde lo mismo exista o no la
 * cuenta, y un error aquí revelaría que sí existe. El enlace nunca se escribe en el log.
 */
@Component
public class CorreoRecuperacionSmtp implements EnviadorCorreoRecuperacion {

    private static final Logger LOG = LoggerFactory.getLogger(CorreoRecuperacionSmtp.class);
    private static final String ASUNTO = "Restablece tu contraseña de CanchApp";

    private final JavaMailSender smtp;
    private final String remitente;

    public CorreoRecuperacionSmtp(JavaMailSender smtp, @Value("${canchapp.correo.remitente}") String remitente) {
        this.smtp = smtp;
        this.remitente = remitente;
    }

    @Override
    public void enviar(String email, String nombre, String enlace) {
        try {
            MimeMessage mensaje = smtp.createMimeMessage();
            MimeMessageHelper ayuda = new MimeMessageHelper(mensaje, true, StandardCharsets.UTF_8.name());
            ayuda.setFrom(remitente);
            ayuda.setTo(email);
            ayuda.setSubject(ASUNTO);
            ayuda.setText(texto(nombre, enlace), html(nombre, enlace));
            smtp.send(mensaje);
        } catch (MessagingException | MailException e) {
            LOG.error("No se pudo enviar el correo de recuperación: {}", e.getMessage());
        }
    }

    private static long minutos() {
        return TokenRecuperacion.VIGENCIA.toMinutes();
    }

    static String texto(String nombre, String enlace) {
        return """
                Hola, %s:

                Recibimos una solicitud para restablecer tu contraseña de CanchApp.
                Abre este enlace desde el celular donde tienes la app CanchApp:

                %s

                El enlace vence en %d minutos y sirve una sola vez.
                Si no fuiste tú, ignora este correo: tu contraseña no cambia.

                Complejo Deportivo Calceta
                """.formatted(nombre, enlace, minutos());
    }

    static String html(String nombre, String enlace) {
        String enlaceSeguro = HtmlUtils.htmlEscape(enlace);
        return """
                <div style="font-family:Roboto,Arial,sans-serif;color:#1c2521;max-width:480px">
                  <h2 style="color:#0f6b3e">Restablece tu contraseña</h2>
                  <p>Hola, %s:</p>
                  <p>Recibimos una solicitud para restablecer tu contraseña de CanchApp.
                     Abre este enlace desde el celular donde tienes la app CanchApp.</p>
                  <p><a href="%s" target="_blank" rel="noopener noreferrer" style="display:inline-block;padding:12px 20px;border-radius:24px;
                     background:#0f6b3e;color:#ffffff;text-decoration:none;font-weight:600">Crear contraseña nueva</a></p>
                  <p style="color:#4d5a52">El enlace vence en %d minutos y sirve una sola vez.
                     Si no fuiste tú, ignora este correo: tu contraseña no cambia.</p>
                  <p style="color:#4d5a52">Complejo Deportivo Calceta</p>
                </div>
                """.formatted(HtmlUtils.htmlEscape(nombre), enlaceSeguro, minutos());
    }
}
