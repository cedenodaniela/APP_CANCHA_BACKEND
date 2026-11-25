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
./mvnw spring-boot:run      # API en http://localhost:8080/api/v1
```

`.env` no se versiona. Lo leen `docker-compose.yml` y Spring Boot. El esquema lo crean las migraciones de Flyway al arrancar.

Desde el emulador de Android, la API se ve en `http://10.0.2.2:8080/api/v1`.

## Pruebas

```bash
./mvnw test
./mvnw verify               # pruebas + cobertura
./mvnw javadoc:javadoc
```

## Flujo de trabajo

Ramas fijas `main`, `calidad` y `desarrollo`. Cada RF va en su rama `feature/RF-XX_nombre-breve`, creada desde `desarrollo`, y vuelve por Pull Request. **Los issues y milestones de los tres repositorios se gestionan aquí**, aunque el RF toque también a los clientes. Ver `../AGENTS.md` §12 y `../docs/PLANIFICACION.md` §12.
