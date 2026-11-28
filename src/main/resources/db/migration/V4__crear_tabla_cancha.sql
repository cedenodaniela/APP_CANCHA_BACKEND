-- V4: canchas del complejo (RF-04).
-- La foto no se guarda aquí: clave_foto apunta a la imagen WebP en el almacenamiento de imágenes.
-- Una cancha inactiva se conserva para su historial, pero no se ofrece al cliente.

CREATE TABLE cancha (
    id                  BIGSERIAL    PRIMARY KEY,
    nombre              VARCHAR(60)  NOT NULL,
    tipo_cesped         VARCHAR(40)  NOT NULL,
    ancho_metros        INTEGER      NOT NULL,
    largo_metros        INTEGER      NOT NULL,
    clave_foto          VARCHAR(100),
    activa              BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion      TIMESTAMP    NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_cancha_ancho CHECK (ancho_metros > 0),
    CONSTRAINT ck_cancha_largo CHECK (largo_metros > 0)
);

-- Dos canchas no pueden llamarse igual, sin distinguir mayúsculas: el cliente elige por el nombre.
CREATE UNIQUE INDEX uq_cancha_nombre ON cancha (LOWER(nombre));

-- Canchas del DERCAS §4.1 (AGENTS.md §10): césped sintético de 30 × 50 m. Las fotos las sube el administrador.
INSERT INTO cancha (nombre, tipo_cesped, ancho_metros, largo_metros) VALUES
    ('Cancha 1', 'Sintético', 30, 50),
    ('Cancha 2', 'Sintético', 30, 50);
