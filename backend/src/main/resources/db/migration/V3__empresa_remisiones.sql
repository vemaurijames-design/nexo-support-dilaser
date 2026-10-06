-- Configuración de empresa (logo + datos para PDF/login)
CREATE TABLE IF NOT EXISTS empresa_config (
    id                  INT PRIMARY KEY DEFAULT 1 CHECK (id = 1),
    razon_social        VARCHAR(180) NOT NULL DEFAULT 'DILASER S.A.',
    nombre_comercial    VARCHAR(180) DEFAULT 'Dilaser',
    nit                 VARCHAR(40) DEFAULT '811.046.078-4',
    direccion_medellin  VARCHAR(220) DEFAULT 'Cra. 33 No. 7-77',
    ciudad_medellin     VARCHAR(80) DEFAULT 'Medellín',
    telefono_medellin   VARCHAR(40) DEFAULT '(4) 311 2280',
    direccion_bogota    VARCHAR(220) DEFAULT 'Cra 7A # 123A-14 Sur · Oficina 403',
    ciudad_bogota       VARCHAR(80) DEFAULT 'Bogotá',
    telefono_bogota     VARCHAR(40) DEFAULT '(601) 622 3358',
    email               VARCHAR(180) DEFAULT 'iris.p@example.org',
    web                 VARCHAR(180) DEFAULT 'www.dilaser.com.co',
    logo_ruta           VARCHAR(255),
    color_primario      VARCHAR(20) DEFAULT '#03738C',
    color_secundario    VARCHAR(20) DEFAULT '#1F736A',
    actualizado_en      TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_por     UUID REFERENCES usuarios(id)
);

INSERT INTO empresa_config (id) VALUES (1) ON CONFLICT (id) DO NOTHING;

-- Remisiones (plantilla Dilaser)
CREATE TABLE IF NOT EXISTS remisiones (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    numero              VARCHAR(40) NOT NULL UNIQUE,
    fecha               DATE NOT NULL DEFAULT CURRENT_DATE,
    telefono_contacto   VARCHAR(40),
    numero_factura      VARCHAR(60),
    guia_numero         VARCHAR(60),
    cliente_id          UUID REFERENCES clientes(id),
    cliente_nombre      VARCHAR(180) NOT NULL,
    cliente_direccion   VARCHAR(220),
    empresa_destino     VARCHAR(180),
    transportadora      VARCHAR(40) DEFAULT 'OTROS',
    motivo              VARCHAR(40) NOT NULL DEFAULT 'VENTA',
    observaciones       TEXT,
    elaboro_nombre      VARCHAR(160),
    recibio_nombre      VARCHAR(160),
    estado              VARCHAR(30) NOT NULL DEFAULT 'BORRADOR',
    pdf_id              UUID,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en      TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por          UUID REFERENCES usuarios(id),
    actualizado_por     UUID REFERENCES usuarios(id)
);

CREATE INDEX IF NOT EXISTS idx_remisiones_fecha ON remisiones (fecha);
CREATE INDEX IF NOT EXISTS idx_remisiones_cliente ON remisiones (cliente_id);

CREATE TABLE IF NOT EXISTS remision_items (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    remision_id         UUID NOT NULL REFERENCES remisiones(id) ON DELETE CASCADE,
    referencia          VARCHAR(80),
    serial_lote         VARCHAR(80),
    descripcion         VARCHAR(220) NOT NULL,
    cantidad            NUMERIC(12,2) NOT NULL DEFAULT 1,
    orden               INT NOT NULL DEFAULT 0
);
