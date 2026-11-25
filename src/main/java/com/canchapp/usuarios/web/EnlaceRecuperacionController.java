package com.canchapp.usuarios.web;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Página a la que lleva el botón del correo de recuperación (RF-02) y que abre la app Android.
 * <p>
 * El correo no puede enlazar directo a {@code canchapp://}: los lectores de correo bloquean los
 * enlaces que no son web. Por eso el correo apunta aquí, con {@code http(s)}, y esta página tiene
 * un botón con un enlace {@code intent://} de Android, que Chrome sí abre en la app. La página no
 * valida ni gasta el token: eso pasa cuando la persona guarda la contraseña nueva.
 */
@RestController
public class EnlaceRecuperacionController {

    /** Ruta pública de la página. La URL completa es la de {@code CANCHAPP_URL_RECUPERACION}. */
    public static final String RUTA = "/recuperar-contrasena";

    /** Debe coincidir con el {@code intent-filter} y el {@code applicationId} de APP_CANCHA_MOVIL. */
    static final String ESQUEMA_APP = "canchapp";
    static final String HOST_APP = "restablecer-contrasena";
    static final String PAQUETE_APP = "com.canchapp.movil";

    /** Los tokens son Base64 apto para URL (ver {@code GeneradorTokenSeguro}). */
    private static final Pattern FORMATO_TOKEN = Pattern.compile("^[A-Za-z0-9_-]{20,100}$");

    /**
     * @return 200 con la página que abre la app; 400 con una página de error si el enlace está
     *         incompleto. Sin caché y sin {@code Referer}, para que el token no quede en otros sitios.
     */
    @GetMapping(value = RUTA, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> pagina(@RequestParam(name = "token", required = false) String token) {
        boolean valido = token != null && FORMATO_TOKEN.matcher(token).matches();
        String html = valido ? paginaAbrir(token) : paginaInvalida();
        return ResponseEntity.status(valido ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .cacheControl(CacheControl.noStore())
                .header("Referrer-Policy", "no-referrer")
                .header("Content-Security-Policy", "default-src 'none'; style-src 'unsafe-inline'")
                .contentType(new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8))
                .body(html);
    }

    /** Enlace que Chrome para Android abre en la app; el token ya pasó {@link #FORMATO_TOKEN}. */
    static String enlaceIntent(String token) {
        return "intent://" + HOST_APP + "?token=" + token
                + "#Intent;scheme=" + ESQUEMA_APP + ";package=" + PAQUETE_APP + ";end";
    }

    /** Enlace directo, para navegadores que no conocen {@code intent://}. */
    static String enlaceDirecto(String token) {
        return ESQUEMA_APP + "://" + HOST_APP + "?token=" + token;
    }

    private static String paginaAbrir(String token) {
        return plantilla("Crea tu contraseña nueva", """
                <p>Toca el botón para abrir CanchApp y crear tu contraseña nueva.</p>
                <a class="boton" href="%s">Abrir CanchApp</a>
                <p class="nota">Abre este enlace en el celular donde tienes instalada la app.
                   Si el botón no responde, <a href="%s">prueba este enlace</a>.</p>
                <p class="nota">El enlace vence a los 30 minutos y sirve una sola vez.</p>
                """.formatted(enlaceIntent(token), enlaceDirecto(token)));
    }

    private static String paginaInvalida() {
        return plantilla("El enlace no es válido", """
                <p>El enlace está incompleto. Pide uno nuevo desde «¿Olvidaste tu contraseña?» en la app.</p>
                """);
    }

    private static String plantilla(String titulo, String contenido) {
        return """
                <!doctype html>
                <html lang="es">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <meta name="referrer" content="no-referrer">
                  <title>%1$s · CanchApp</title>
                  <style>
                    body { margin: 0; background: #f4f6f1; color: #1c2521;
                           font: 16px/1.5 Roboto, Arial, sans-serif; }
                    header { background: #0f6b3e; color: #fff; padding: 32px 16px 64px;
                             border-radius: 0 0 32px 32px; text-align: center; font-weight: 700; font-size: 22px; }
                    main { max-width: 440px; margin: -40px auto 24px; padding: 24px 20px; background: #fff;
                           border: 1px solid #d5dccf; border-radius: 20px; }
                    h1 { margin: 0 0 8px; font-size: 22px; }
                    .boton { display: block; margin: 20px 0; padding: 14px; border-radius: 24px; background: #0f6b3e;
                             color: #fff; text-align: center; text-decoration: none; font-weight: 600; }
                    .nota { color: #4d5a52; font-size: 14px; }
                    .nota a { color: #0f6b3e; }
                  </style>
                </head>
                <body>
                  <header>CanchApp</header>
                  <main>
                    <h1>%1$s</h1>
                    %2$s
                  </main>
                </body>
                </html>
                """.formatted(titulo, contenido);
    }
}
