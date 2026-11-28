-- V3: configuración única del complejo (RF-03): datos generales, datos bancarios y política de reservas.
-- La versión 1 maneja un solo complejo, así que la tabla tiene una sola fila (id = 1).
-- Los CHECK repiten los mínimos del dominio (PoliticaReserva) como última defensa.

CREATE TABLE configuracion_complejo (
    id                        SMALLINT     PRIMARY KEY DEFAULT 1,
    nombre                    VARCHAR(120) NOT NULL,
    direccion                 VARCHAR(200) NOT NULL,
    telefono                  VARCHAR(20)  NOT NULL,
    banco                     VARCHAR(100) NOT NULL,
    tipo_cuenta               VARCHAR(20)  NOT NULL,
    numero_cuenta             VARCHAR(30)  NOT NULL,
    titular_cuenta            VARCHAR(120) NOT NULL,
    minutos_expiracion        INTEGER      NOT NULL,
    horas_plazo_cancelacion   INTEGER      NOT NULL,
    max_reservas_efectivo     INTEGER      NOT NULL,
    dias_anticipacion         INTEGER      NOT NULL,
    limitar_tras_inasistencia BOOLEAN      NOT NULL,
    fecha_actualizacion       TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_configuracion_unica CHECK (id = 1),
    CONSTRAINT ck_configuracion_tipo_cuenta CHECK (tipo_cuenta IN ('AHORROS', 'CORRIENTE')),
    CONSTRAINT ck_configuracion_minutos_expiracion CHECK (minutos_expiracion >= 1),
    CONSTRAINT ck_configuracion_horas_cancelacion CHECK (horas_plazo_cancelacion >= 0),
    CONSTRAINT ck_configuracion_max_efectivo CHECK (max_reservas_efectivo >= 0),
    CONSTRAINT ck_configuracion_dias_anticipacion CHECK (dias_anticipacion >= 1)
);

-- Datos del DERCAS §4.1 (AGENTS.md §10). La cuenta bancaria es de ejemplo: el administrador la cambia.
INSERT INTO configuracion_complejo (
    id, nombre, direccion, telefono,
    banco, tipo_cuenta, numero_cuenta, titular_cuenta,
    minutos_expiracion, horas_plazo_cancelacion, max_reservas_efectivo, dias_anticipacion,
    limitar_tras_inasistencia
) VALUES (
    1, 'Complejo Deportivo Calceta', 'Calceta, Manabí', '0969908865',
    'Banco Pichincha', 'AHORROS', '2201234567', 'Complejo Deportivo Calceta',
    15, 3, 1, 3,
    TRUE
);
