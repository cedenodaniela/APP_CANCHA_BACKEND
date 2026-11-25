# AGENTS.md — CanchApp · Backend (APP_CANCHA_BACKEND)

API REST en **Spring Boot 3 (Java 17)** del proyecto CanchApp. Esta es la guía **breve** de este repositorio.

> **La guía completa vive fuera de este repositorio**, en la carpeta de trabajo, un nivel arriba:
> [`../AGENTS.md`](../AGENTS.md) · DERCAS: [`../DERCAS_CanchApp.pdf`](../DERCAS_CanchApp.pdf) · plan: [`../docs/PLANIFICACION.md`](../docs/PLANIFICACION.md).
>
> Si este archivo y la guía completa se contradicen, manda la guía completa y, sobre todo, el **DERCAS**.

## Qué se construye aquí

Todo lo que es **regla de negocio**: dominio, casos de uso, persistencia, tareas programadas y API REST. Los dos clientes (`../APP_CANCHA_MOVIL`, la app Android, y `../APP_CANCHA_WEB`, el panel web) **no calculan reglas**: consumen esta API.

## Estructura

Un proyecto Maven y un paquete por módulo, cada uno con sus cuatro capas:

```
src/main/java/com/canchapp/
├── usuarios/         domain · application · infrastructure · web
├── configuracion/
├── canchas/
├── horarios/
├── reservas/
├── pagos/
├── notificaciones/
├── reportes/
└── shared/           errores, seguridad y zona horaria
```

- `domain` **no importa** Spring, JPA, Jackson ni Firebase. Solo Java puro.
- `application` tiene un caso de uso por clase y depende solo de `domain`.
- `infrastructure` implementa los puertos (JPA, Firebase, SMTP, WebP).
- `web` tiene los controladores y los DTOs (`record`). **Nunca** expongas entidades del dominio ni de JPA.

## Reglas que no se rompen

- Nombres del dominio en **español** (`Reserva`, `cancelar()`, `saldoFavor`); los términos técnicos en inglés.
- Dinero con `BigDecimal` y escala 2; **nunca** `double`. Fechas con `java.time` en `America/Guayaquil`.
- Los valores numéricos son parámetros de `PoliticaReserva`; nunca los escribas fijos.
- Las transiciones de `Reserva` se validan **dentro de la entidad**.
- **Nunca doble reserva (RN-04)**: la restricción única viaja en **la misma migración** que crea la tabla de bloques reservados.
- Cada cambio de esquema es una migración **nueva** de Flyway en `src/main/resources/db/migration` (`V{n}__descripcion.sql`). Nunca edites una ya aplicada.
- **Javadoc obligatorio** con `@see RN-xx`. API bajo `/api/v1`, recursos en plural.
- Errores: excepciones de dominio traducidas en un `@RestControllerAdvice` con `codigo` y `mensaje` en español.
- CORS es solo para el panel web; la app Android no lo necesita.

## Comandos

```bash
cp .env.example .env   # una vez, y completar los valores
docker compose up -d db
./mvnw spring-boot:run
./mvnw test
./mvnw verify          # pruebas + cobertura
./mvnw javadoc:javadoc
```

En Windows: `mvnw.cmd`. Se necesita el JDK 17 en `JAVA_HOME`; Maven lo descarga el wrapper.

## Pruebas y cierre

- Cada RN tocada tiene al menos una prueba en `domain` o `application`, sin Spring ni base de datos.
- Cobertura mínima del dominio: **70 %**.
- RN-04 exige prueba de concurrencia: 10 solicitudes simultáneas sobre el mismo bloque producen una sola reserva.
- Cada endpoint se prueba con su autorización por rol.

## Git

Ramas `main`, `calidad` y `desarrollo`. Cada RF va en `feature/RF-XX_nombre-breve` desde `desarrollo` y vuelve por Pull Request (`../docs/PLANIFICACION.md` §12). Antes de cada commit: `./mvnw test` en verde. **Los issues y milestones de los tres repositorios viven aquí.**

## Contrato con los clientes

Este repositorio publica el `openapi.json` (springdoc). Es el **contrato** que `APP_CANCHA_MOVIL` y `APP_CANCHA_WEB` validan en su CI: si cambias un DTO, el cambio se detecta en el Pull Request.

## Seguridad

Contraseñas con BCrypt, JWT HS256, autorización por rol en cada endpoint y HTTPS. Un comprobante solo lo ven su dueño y el administrador, servido por un endpoint autenticado. **Nunca** subas `.env`, contraseñas de base de datos, la clave JWT ni credenciales de Firebase.
