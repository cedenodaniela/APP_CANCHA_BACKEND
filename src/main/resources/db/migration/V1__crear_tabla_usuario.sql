-- V1: tabla de usuarios (RF-01).
-- Contraseñas guardadas solo como hash BCrypt (RNF-02).
-- El registro público crea cuentas CLIENTE; la primera cuenta ADMINISTRADOR se siembra
-- al instalar el sistema y las siguientes se crean desde el panel (RF-22).

CREATE TABLE usuario (
    id                  BIGSERIAL    PRIMARY KEY,
    nombre              VARCHAR(100) NOT NULL,
    apellido            VARCHAR(100) NOT NULL,
    email               VARCHAR(150) NOT NULL,
    password            VARCHAR(255) NOT NULL,
    rol                 VARCHAR(20)  NOT NULL,
    telefono            VARCHAR(20),
    fecha_creacion      TIMESTAMP    NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP    NOT NULL DEFAULT NOW(),
    activo              BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_usuario_email UNIQUE (email),
    CONSTRAINT ck_usuario_rol CHECK (rol IN ('CLIENTE', 'ADMINISTRADOR'))
);
