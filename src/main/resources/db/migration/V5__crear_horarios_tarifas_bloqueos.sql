-- V5: horario de atención, tarifas por franja y bloqueos (RF-05).
-- Las horas se guardan en minutos desde las 00:00 (0 a 1440), en múltiplos de 30, porque el complejo
-- cierra a las 24:00 y TIME no admite ese valor. Los días de la semana van de 1 (lunes) a 7 (domingo).

CREATE TABLE horario_atencion (
    dia_semana       SMALLINT PRIMARY KEY,
    abierto          BOOLEAN  NOT NULL,
    apertura_minutos INTEGER,
    cierre_minutos   INTEGER,
    CONSTRAINT ck_horario_dia CHECK (dia_semana BETWEEN 1 AND 7),
    CONSTRAINT ck_horario_horas CHECK (
        (abierto AND apertura_minutos IS NOT NULL AND cierre_minutos IS NOT NULL
             AND apertura_minutos >= 0 AND cierre_minutos <= 1440
             AND apertura_minutos < cierre_minutos
             AND apertura_minutos % 30 = 0 AND cierre_minutos % 30 = 0)
        OR (NOT abierto AND apertura_minutos IS NULL AND cierre_minutos IS NULL))
);

-- dias_semana: un bit por día (lunes = 1, martes = 2, … domingo = 64). Que dos tarifas no se
-- superpongan lo valida el caso de uso GuardarTarifaUseCase.
CREATE TABLE tarifa (
    id             BIGSERIAL     PRIMARY KEY,
    dias_semana    SMALLINT      NOT NULL,
    inicio_minutos INTEGER       NOT NULL,
    fin_minutos    INTEGER       NOT NULL,
    precio_hora    NUMERIC(8, 2) NOT NULL,
    CONSTRAINT ck_tarifa_dias CHECK (dias_semana BETWEEN 1 AND 127),
    CONSTRAINT ck_tarifa_horas CHECK (inicio_minutos >= 0 AND fin_minutos <= 1440 AND inicio_minutos < fin_minutos
        AND inicio_minutos % 30 = 0 AND fin_minutos % 30 = 0),
    CONSTRAINT ck_tarifa_precio CHECK (precio_hora > 0)
);

-- cancha_id vacío: el bloqueo aplica a todas las canchas.
CREATE TABLE bloqueo (
    id             BIGSERIAL    PRIMARY KEY,
    cancha_id      BIGINT,
    fecha          DATE         NOT NULL,
    inicio_minutos INTEGER      NOT NULL,
    fin_minutos    INTEGER      NOT NULL,
    motivo         VARCHAR(200) NOT NULL,
    fecha_creacion TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_bloqueo_cancha FOREIGN KEY (cancha_id) REFERENCES cancha (id),
    CONSTRAINT ck_bloqueo_horas CHECK (inicio_minutos >= 0 AND fin_minutos <= 1440 AND inicio_minutos < fin_minutos
        AND inicio_minutos % 30 = 0 AND fin_minutos % 30 = 0)
);

CREATE INDEX ix_bloqueo_fecha ON bloqueo (fecha);

-- RN-02b: todos los días de 10:00 a 24:00.
INSERT INTO horario_atencion (dia_semana, abierto, apertura_minutos, cierre_minutos) VALUES
    (1, TRUE, 600, 1440), (2, TRUE, 600, 1440), (3, TRUE, 600, 1440), (4, TRUE, 600, 1440),
    (5, TRUE, 600, 1440), (6, TRUE, 600, 1440), (7, TRUE, 600, 1440);

-- RN-03: $20/h de 10:00 a 18:00 y $25/h de 18:00 a 24:00, todos los días (127 = los 7 bits).
INSERT INTO tarifa (dias_semana, inicio_minutos, fin_minutos, precio_hora) VALUES
    (127, 600, 1080, 20.00),
    (127, 1080, 1440, 25.00);
