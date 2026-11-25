-- V2: enlaces de recuperación de contraseña (RF-02).
-- Se guarda solo el hash SHA-256 del token, nunca el token: el enlace en claro viaja una vez, por correo.
-- Cada enlace vence a los 30 minutos (fecha_vencimiento) y sirve una sola vez (fecha_uso).

CREATE TABLE recuperacion_contrasena (
    id                BIGSERIAL PRIMARY KEY,
    usuario_id        BIGINT    NOT NULL,
    hash_token        VARCHAR(64) NOT NULL,
    fecha_creacion    TIMESTAMP NOT NULL,
    fecha_vencimiento TIMESTAMP NOT NULL,
    fecha_uso         TIMESTAMP,
    CONSTRAINT fk_recuperacion_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT uq_recuperacion_hash_token UNIQUE (hash_token)
);

CREATE INDEX ix_recuperacion_usuario ON recuperacion_contrasena (usuario_id);
