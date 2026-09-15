-- ============================================================================
-- NEXO SUPPORT — Esquema PostgreSQL 16
-- Empresa: Dilaser S.A. (soporte técnico Colombia)
-- ============================================================================
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "unaccent";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- ----------------------------------------------------------------------------
-- SEGURIDAD / USUARIOS
-- ----------------------------------------------------------------------------
CREATE TABLE usuarios (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombres             VARCHAR(120) NOT NULL,
    apellidos           VARCHAR(120) NOT NULL,
    email               VARCHAR(180) NOT NULL UNIQUE,
    password_hash       VARCHAR(255) NOT NULL,
    rol                 VARCHAR(30) NOT NULL,
    telefono            VARCHAR(30),
    whatsapp            VARCHAR(30),
    sede                VARCHAR(40),
    cargo               VARCHAR(80),
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    debe_cambiar_pass   BOOLEAN NOT NULL DEFAULT TRUE,
    ultimo_login        TIMESTAMPTZ,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en      TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por          UUID,
    actualizado_por     UUID
);

CREATE TABLE refresh_tokens (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id      UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    token_hash      VARCHAR(255) NOT NULL,
    expira_en       TIMESTAMPTZ NOT NULL,
    revocado        BOOLEAN NOT NULL DEFAULT FALSE,
    user_agent      VARCHAR(255),
    ip              VARCHAR(60)
);

CREATE TABLE password_reset_tokens (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id      UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    token_hash      VARCHAR(255) NOT NULL,
    expira_en       TIMESTAMPTZ NOT NULL,
    usado           BOOLEAN NOT NULL DEFAULT FALSE,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ----------------------------------------------------------------------------
-- CATÁLOGOS
-- ----------------------------------------------------------------------------
CREATE TABLE ciudades (
    id          SERIAL PRIMARY KEY,
    nombre      VARCHAR(80) NOT NULL,
    departamento VARCHAR(80) NOT NULL,
    UNIQUE (nombre, departamento)
);

CREATE TABLE marcas (
    id          SERIAL PRIMARY KEY,
    codigo      VARCHAR(20) UNIQUE,
    nombre      VARCHAR(80) NOT NULL UNIQUE,
    pais_origen VARCHAR(80),
    activo      BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE tecnologias (
    id          SERIAL PRIMARY KEY,
    nombre      VARCHAR(120) NOT NULL UNIQUE,
    descripcion TEXT
);

CREATE TABLE lineas_producto (
    id          SERIAL PRIMARY KEY,
    marca_id    INT NOT NULL REFERENCES marcas(id),
    codigo      VARCHAR(20),
    nombre      VARCHAR(80) NOT NULL,
    tecnologia_id INT REFERENCES tecnologias(id),
    UNIQUE (marca_id, nombre)
);

CREATE TABLE modelos_equipo (
    id              SERIAL PRIMARY KEY,
    marca_id        INT NOT NULL REFERENCES marcas(id),
    linea_id        INT REFERENCES lineas_producto(id),
    tecnologia_id   INT REFERENCES tecnologias(id),
    nombre          VARCHAR(120) NOT NULL,
    peso_aprox_kg   NUMERIC(8,2),
    voltage         VARCHAR(40),
    registro_sanitario VARCHAR(80),
    periodicidad_default VARCHAR(20) NOT NULL DEFAULT 'ANUAL',
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (marca_id, nombre)
);

CREATE TABLE fabricantes_contacto (
    id              SERIAL PRIMARY KEY,
    marca_id        INT NOT NULL REFERENCES marcas(id),
    nombre_area     VARCHAR(80),
    emails          TEXT[] NOT NULL,
    telefono        VARCHAR(40),
    notas           TEXT
);

CREATE TABLE bodegas (
    id              SERIAL PRIMARY KEY,
    codigo          VARCHAR(20) UNIQUE NOT NULL,
    nombre          VARCHAR(80) NOT NULL,
    ciudad          VARCHAR(40) NOT NULL,
    direccion       VARCHAR(180),
    activa          BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE ubicaciones_bodega (
    id              SERIAL PRIMARY KEY,
    bodega_id       INT NOT NULL REFERENCES bodegas(id),
    codigo          VARCHAR(40) NOT NULL,
    descripcion     VARCHAR(120),
    UNIQUE (bodega_id, codigo)
);

-- ----------------------------------------------------------------------------
-- CLIENTES
-- ----------------------------------------------------------------------------
CREATE TABLE clientes (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    razon_social        VARCHAR(180) NOT NULL,
    nombre_comercial    VARCHAR(180),
    nit                 VARCHAR(30),
    tipo                VARCHAR(30) NOT NULL DEFAULT 'CLINICA',
    direccion           VARCHAR(220),
    ciudad              VARCHAR(80),
    departamento        VARCHAR(80),
    telefono            VARCHAR(40),
    whatsapp            VARCHAR(40),
    email_principal     VARCHAR(180),
    emails_copia        TEXT[],
    contacto_nombre     VARCHAR(120),
    contacto_cargo      VARCHAR(80),
    observaciones       TEXT,
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en      TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por          UUID REFERENCES usuarios(id),
    actualizado_por     UUID REFERENCES usuarios(id)
);

CREATE INDEX idx_clientes_razon ON clientes USING gin (razon_social gin_trgm_ops);
CREATE INDEX idx_clientes_ciudad ON clientes (ciudad);

CREATE TABLE cliente_contactos (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cliente_id      UUID NOT NULL REFERENCES clientes(id) ON DELETE CASCADE,
    nombre          VARCHAR(120) NOT NULL,
    cargo           VARCHAR(80),
    email           VARCHAR(180),
    telefono        VARCHAR(40),
    whatsapp        VARCHAR(40),
    recibe_alertas  BOOLEAN NOT NULL DEFAULT TRUE,
    principal       BOOLEAN NOT NULL DEFAULT FALSE
);

-- ----------------------------------------------------------------------------
-- EQUIPOS + HOJA DE VIDA
-- ----------------------------------------------------------------------------
CREATE TABLE equipos (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    serial                  VARCHAR(80) NOT NULL UNIQUE,
    modelo_id               INT NOT NULL REFERENCES modelos_equipo(id),
    cliente_id              UUID REFERENCES clientes(id),
    propiedad               VARCHAR(30) NOT NULL DEFAULT 'CLIENTE',
    estado                  VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    institucion_nombre      VARCHAR(180),
    pais_origen             VARCHAR(80),
    voltage_alimentacion    VARCHAR(40),
    fecha_importacion       DATE,
    peso_declarado          VARCHAR(40),
    registro_sanitario      VARCHAR(80),
    fecha_instalacion       DATE,
    numero_acta_entrega     VARCHAR(40),
    garantia_inicio         DATE,
    garantia_fin            DATE,
    ciudad_ubicacion        VARCHAR(80),
    direccion_ubicacion     VARCHAR(220),
    pulsos_actuales         BIGINT,
    potencia_salida_hp      VARCHAR(40),
    observaciones           TEXT,
    creado_en               TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en          TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por              UUID REFERENCES usuarios(id),
    actualizado_por         UUID REFERENCES usuarios(id)
);

CREATE INDEX idx_equipos_serial_trgm ON equipos USING gin (serial gin_trgm_ops);
CREATE INDEX idx_equipos_cliente ON equipos (cliente_id);
CREATE INDEX idx_equipos_estado ON equipos (estado);

CREATE TABLE equipo_accesorios (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    equipo_id       UUID NOT NULL REFERENCES equipos(id) ON DELETE CASCADE,
    descripcion     VARCHAR(180) NOT NULL,
    cantidad        NUMERIC(10,2) NOT NULL DEFAULT 1,
    serial_accesorio VARCHAR(80),
    orden           INT NOT NULL DEFAULT 0
);

CREATE TABLE hoja_vida_mantenimientos (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    equipo_id           UUID NOT NULL REFERENCES equipos(id) ON DELETE CASCADE,
    fecha_revision      DATE NOT NULL,
    actividades         TEXT NOT NULL,
    tipo                VARCHAR(40) NOT NULL DEFAULT 'PREVENTIVO',
    informe_codigo      VARCHAR(40),
    ingeniero_id        UUID REFERENCES usuarios(id),
    ingeniero_nombre    VARCHAR(160),
    pdf_adjunto_id      UUID,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por          UUID REFERENCES usuarios(id)
);

CREATE TABLE actas_entrega (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    numero              VARCHAR(40) UNIQUE,
    equipo_id           UUID NOT NULL REFERENCES equipos(id),
    cliente_id          UUID NOT NULL REFERENCES clientes(id),
    fecha_acta          DATE NOT NULL,
    politicas_garantia  TEXT,
    pdf_id              UUID,
    firmado             BOOLEAN NOT NULL DEFAULT FALSE,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por          UUID REFERENCES usuarios(id)
);

CREATE TABLE adjuntos (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entidad         VARCHAR(40) NOT NULL,
    entidad_id      UUID NOT NULL,
    nombre_archivo  VARCHAR(180) NOT NULL,
    content_type    VARCHAR(80) NOT NULL,
    ruta_storage    VARCHAR(255) NOT NULL,
    tipo            VARCHAR(40),
    subido_por      UUID REFERENCES usuarios(id),
    subido_en       TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ----------------------------------------------------------------------------
-- RMA / GARANTÍAS / DEVOLUCIONES
-- ----------------------------------------------------------------------------
CREATE TABLE rma_casos (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    numero_interno          VARCHAR(30) NOT NULL UNIQUE,
    numero_rma_fabricante   VARCHAR(60),
    marca_id                INT REFERENCES marcas(id),
    equipo_id               UUID REFERENCES equipos(id),
    cliente_id              UUID REFERENCES clientes(id),
    customer_snapshot       VARCHAR(220),
    descripcion_item        VARCHAR(220),
    serial_reportado        VARCHAR(80),
    part_number             VARCHAR(80),
    purchase_order          VARCHAR(80),
    en_garantia             BOOLEAN,
    fecha_instalacion       DATE,
    problema_reportado      TEXT,
    numero_pulsos           BIGINT,
    hp_output_power         VARCHAR(60),
    detalles_adicionales    TEXT,
    motivo                  VARCHAR(40) NOT NULL DEFAULT 'GARANTIA',
    estado                  VARCHAR(30) NOT NULL DEFAULT 'BORRADOR',
    destinatarios_extra     TEXT[],
    creado_en               TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en          TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por              UUID NOT NULL REFERENCES usuarios(id),
    actualizado_por         UUID REFERENCES usuarios(id)
);

CREATE INDEX idx_rma_serial ON rma_casos (serial_reportado);
CREATE INDEX idx_rma_part ON rma_casos (part_number);
CREATE INDEX idx_rma_estado ON rma_casos (estado);

CREATE TABLE rma_historial (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rma_id          UUID NOT NULL REFERENCES rma_casos(id) ON DELETE CASCADE,
    usuario_id      UUID NOT NULL REFERENCES usuarios(id),
    accion          VARCHAR(40) NOT NULL,
    estado_anterior VARCHAR(30),
    estado_nuevo    VARCHAR(30),
    cambios_json    JSONB,
    comentario      TEXT,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE rma_envios (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rma_id          UUID NOT NULL REFERENCES rma_casos(id) ON DELETE CASCADE,
    canal           VARCHAR(20) NOT NULL,
    destinatarios   TEXT[] NOT NULL,
    asunto          VARCHAR(220),
    cuerpo          TEXT,
    pdf_id          UUID,
    exitoso         BOOLEAN NOT NULL DEFAULT FALSE,
    error_msg       TEXT,
    enviado_por     UUID REFERENCES usuarios(id),
    enviado_en      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ----------------------------------------------------------------------------
-- INVENTARIO
-- ----------------------------------------------------------------------------
CREATE TABLE repuestos (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    referencia          VARCHAR(40) NOT NULL UNIQUE,
    descripcion         VARCHAR(220) NOT NULL,
    marca_id            INT REFERENCES marcas(id),
    linea_id            INT REFERENCES lineas_producto(id),
    unidad_medida       VARCHAR(10) NOT NULL DEFAULT 'UND',
    costo_promedio      NUMERIC(14,2) NOT NULL DEFAULT 0,
    precio_sugerido     NUMERIC(14,2) NOT NULL DEFAULT 0,
    stock_minimo        NUMERIC(12,2) NOT NULL DEFAULT 0,
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    observaciones       TEXT,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_repuestos_ref ON repuestos USING gin (referencia gin_trgm_ops);
CREATE INDEX idx_repuestos_desc ON repuestos USING gin (descripcion gin_trgm_ops);

CREATE TABLE stock_bodega (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    repuesto_id     UUID NOT NULL REFERENCES repuestos(id),
    bodega_id       INT NOT NULL REFERENCES bodegas(id),
    ubicacion_id    INT REFERENCES ubicaciones_bodega(id),
    existencia      NUMERIC(12,2) NOT NULL DEFAULT 0,
    fecha_primera_entrada DATE,
    fecha_ultima_entrada  DATE,
    fecha_ultima_salida   DATE,
    UNIQUE (repuesto_id, bodega_id)
);

CREATE TABLE inventario_movimientos (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo                VARCHAR(40) NOT NULL,
    repuesto_id         UUID NOT NULL REFERENCES repuestos(id),
    bodega_origen_id    INT REFERENCES bodegas(id),
    bodega_destino_id   INT REFERENCES bodegas(id),
    ubicacion_id        INT REFERENCES ubicaciones_bodega(id),
    cantidad            NUMERIC(12,2) NOT NULL CHECK (cantidad > 0),
    costo_unitario      NUMERIC(14,2),
    cliente_id          UUID REFERENCES clientes(id),
    equipo_id           UUID REFERENCES equipos(id),
    rma_id              UUID REFERENCES rma_casos(id),
    recibido_por_nombre VARCHAR(160),
    recibido_por_id     UUID REFERENCES usuarios(id),
    entregado_por_id    UUID REFERENCES usuarios(id),
    documento_ref       VARCHAR(60),
    observaciones       TEXT,
    fecha_movimiento    DATE NOT NULL DEFAULT CURRENT_DATE,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por          UUID NOT NULL REFERENCES usuarios(id)
);

CREATE INDEX idx_mov_repuesto_fecha ON inventario_movimientos (repuesto_id, fecha_movimiento);
CREATE INDEX idx_mov_tipo ON inventario_movimientos (tipo);

CREATE TABLE conteos_fisicos (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bodega_id       INT NOT NULL REFERENCES bodegas(id),
    fecha           DATE NOT NULL,
    estado          VARCHAR(20) NOT NULL DEFAULT 'ABIERTO',
    creado_por      UUID REFERENCES usuarios(id),
    cerrado_por     UUID REFERENCES usuarios(id),
    cerrado_en      TIMESTAMPTZ
);

CREATE TABLE conteo_detalle (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conteo_id       UUID NOT NULL REFERENCES conteos_fisicos(id) ON DELETE CASCADE,
    repuesto_id     UUID NOT NULL REFERENCES repuestos(id),
    existencia_sistema NUMERIC(12,2) NOT NULL,
    conteo          NUMERIC(12,2) NOT NULL,
    diferencia      NUMERIC(12,2) GENERATED ALWAYS AS (conteo - existencia_sistema) STORED,
    observacion     TEXT
);

-- ----------------------------------------------------------------------------
-- CRONOGRAMA / ALERTAS / COTIZACIONES
-- ----------------------------------------------------------------------------
CREATE TABLE planes_mantenimiento (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    equipo_id           UUID NOT NULL REFERENCES equipos(id) ON DELETE CASCADE,
    periodicidad        VARCHAR(20) NOT NULL DEFAULT 'ANUAL',
    mes_ancla           INTEGER CHECK (mes_ancla BETWEEN 1 AND 12),
    dia_ancla           INTEGER CHECK (dia_ancla BETWEEN 1 AND 28),
    ultimo_realizado    DATE,
    proximo_programado  DATE NOT NULL,
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    notas               TEXT,
    UNIQUE (equipo_id)
);

CREATE TABLE ordenes_servicio (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    numero              VARCHAR(30) UNIQUE,
    equipo_id           UUID NOT NULL REFERENCES equipos(id),
    cliente_id          UUID NOT NULL REFERENCES clientes(id),
    tipo                VARCHAR(40) NOT NULL,
    fecha_programada    DATE,
    fecha_ejecucion     DATE,
    ingeniero_id        UUID REFERENCES usuarios(id),
    estado              VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    informe_codigo      VARCHAR(40),
    valor_cotizado      NUMERIC(14,2),
    valor_facturado     NUMERIC(14,2),
    observaciones       TEXT,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE alertas_mantenimiento (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_id             UUID NOT NULL REFERENCES planes_mantenimiento(id),
    equipo_id           UUID NOT NULL REFERENCES equipos(id),
    cliente_id          UUID NOT NULL REFERENCES clientes(id),
    anio                INT NOT NULL,
    mes                 INTEGER NOT NULL,
    estado              VARCHAR(30) NOT NULL DEFAULT 'PROGRAMADA',
    fecha_objetivo      DATE NOT NULL,
    fecha_envio_email   TIMESTAMPTZ,
    fecha_envio_whatsapp TIMESTAMPTZ,
    fecha_respuesta     TIMESTAMPTZ,
    orden_servicio_id   UUID REFERENCES ordenes_servicio(id),
    UNIQUE (equipo_id, anio)
);

CREATE TABLE notificaciones (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    alerta_id       UUID REFERENCES alertas_mantenimiento(id),
    rma_id          UUID REFERENCES rma_casos(id),
    canal           VARCHAR(20) NOT NULL,
    destinatario    VARCHAR(180) NOT NULL,
    asunto          VARCHAR(220),
    cuerpo          TEXT,
    payload_json    JSONB,
    estado          VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    error_msg       TEXT,
    enviado_en      TIMESTAMPTZ,
    creado_por      UUID REFERENCES usuarios(id)
);

CREATE TABLE cotizaciones (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    numero          VARCHAR(30) UNIQUE,
    cliente_id      UUID NOT NULL REFERENCES clientes(id),
    equipo_id       UUID REFERENCES equipos(id),
    alerta_id       UUID REFERENCES alertas_mantenimiento(id),
    tipo            VARCHAR(40) NOT NULL DEFAULT 'PREVENTIVO',
    items_json      JSONB NOT NULL,
    subtotal        NUMERIC(14,2) NOT NULL,
    iva             NUMERIC(14,2) NOT NULL DEFAULT 0,
    total           NUMERIC(14,2) NOT NULL,
    vigencia_hasta  DATE,
    estado          VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
    pdf_id          UUID,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por      UUID REFERENCES usuarios(id)
);

-- ----------------------------------------------------------------------------
-- AUDITORÍA GENÉRICA
-- ----------------------------------------------------------------------------
CREATE TABLE auditoria (
    id              BIGSERIAL PRIMARY KEY,
    entidad         VARCHAR(40) NOT NULL,
    entidad_id      VARCHAR(60) NOT NULL,
    accion          VARCHAR(20) NOT NULL,
    usuario_id      UUID REFERENCES usuarios(id),
    antes           JSONB,
    despues         JSONB,
    ip              VARCHAR(60),
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_aud_entidad ON auditoria (entidad, entidad_id);

-- ----------------------------------------------------------------------------
-- VISTAS PARA DASHBOARD
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW v_stock_valorizado AS
SELECT r.referencia, r.descripcion, m.nombre AS marca, l.nombre AS linea,
       b.nombre AS bodega, s.existencia, r.costo_promedio,
       (s.existencia * r.costo_promedio) AS valor_bodega,
       r.precio_sugerido, s.fecha_ultima_entrada, s.fecha_ultima_salida,
       CASE WHEN s.existencia <= r.stock_minimo THEN TRUE ELSE FALSE END AS bajo_minimo
FROM stock_bodega s
JOIN repuestos r ON r.id = s.repuesto_id
JOIN bodegas b ON b.id = s.bodega_id
LEFT JOIN marcas m ON m.id = r.marca_id
LEFT JOIN lineas_producto l ON l.id = r.linea_id;

CREATE OR REPLACE VIEW v_alertas_mes AS
SELECT a.id, a.anio, a.mes, a.estado, a.fecha_objetivo,
       e.serial, mo.nombre AS modelo, ma.nombre AS marca,
       c.razon_social, c.ciudad, c.whatsapp, c.email_principal,
       p.periodicidad
FROM alertas_mantenimiento a
JOIN equipos e ON e.id = a.equipo_id
JOIN modelos_equipo mo ON mo.id = e.modelo_id
JOIN marcas ma ON ma.id = mo.marca_id
JOIN clientes c ON c.id = a.cliente_id
JOIN planes_mantenimiento p ON p.id = a.plan_id;