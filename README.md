# CanchApp · Backend

API REST del sistema de reservas del **Complejo Deportivo Calceta** (Calceta, Manabí, Ecuador).

Spring Boot 3 · Java 17 · PostgreSQL · Flyway · Spring Security + JWT · Spring WebSocket (STOMP).

Este repositorio contiene **solo el backend**. La app Android vive en [`APP_CANCHA_MOVIL`](https://github.com/cedenodaniela/APP_CANCHA_MOVIL) y el panel de administración en [`APP_CANCHA_WEB`](https://github.com/cedenodaniela/APP_CANCHA_WEB).

## Documentación

La documentación general del proyecto **no vive aquí**: está en la carpeta de trabajo `app_cancha/`, un nivel arriba.

| Documento | Ruta |
|---|---|
| Guía para agentes y convenciones | `../AGENTS.md` |
| DERCAS (fuente de verdad) | `../DERCAS_CanchApp.pdf` |
| Planificación de las 5 iteraciones | `../docs/PLANIFICACION.md` |

## Requisitos

- JDK 17 en `JAVA_HOME`. Maven no hace falta: lo descarga el wrapper (`./mvnw`, o `mvnw.cmd` en Windows).
- Docker, para la base de datos PostgreSQL local.

## Puesta en marcha

```bash
cp .env.example .env        # una sola vez; completa la contraseña y la clave JWT
docker compose up -d db     # PostgreSQL 15 en localhost:5432
docker compose up -d correo # Mailpit: recibe los correos de la API
./mvnw spring-boot:run      # API en http://localhost:8080/api/v1
```

`.env` no se versiona. Lo leen `docker-compose.yml` y Spring Boot. El esquema lo crean las migraciones de Flyway al arrancar.

Desde el emulador de Android, la API se ve en `http://10.0.2.2:8080/api/v1`.

### Correos en desarrollo

Mailpit recibe en `localhost:1025` los correos que envía la API (recuperación de contraseña, RF-02) sin mandarlos a nadie, y los muestra en http://localhost:8025. Con `adb reverse tcp:8025 tcp:8025` se abren también en el celular, en `http://localhost:8025`.

El botón del correo lleva a `/recuperar-contrasena?token=…` de esta API (`CANCHAPP_URL_RECUPERACION`), una página con el botón «Abrir CanchApp» que abre la app en la pantalla de contraseña nueva. Los lectores de correo bloquean los enlaces `canchapp://`, por eso el correo usa una dirección web. Esa dirección tiene que ser alcanzable desde el celular: en desarrollo, `http://localhost:8080` con `adb reverse tcp:8080 tcp:8080`; en producción, la URL HTTPS pública del backend. En producción, `CANCHAPP_SMTP_*` apuntan a un servidor SMTP real.

## Pruebas

```bash
./mvnw test
./mvnw verify               # pruebas + cobertura
./mvnw javadoc:javadoc
```

## Flujo de trabajo

Ramas fijas `main`, `calidad` y `desarrollo`. Cada RF va en su rama `feature/RF-XX_nombre-breve`, creada desde `desarrollo`, y vuelve por Pull Request. **Los issues y milestones de los tres repositorios se gestionan aquí**, aunque el RF toque también a los clientes. Ver `../AGENTS.md` §12 y `../docs/PLANIFICACION.md` §12.
