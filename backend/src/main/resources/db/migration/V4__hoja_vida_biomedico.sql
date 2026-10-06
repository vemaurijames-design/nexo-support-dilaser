-- Campos ampliados hoja de vida biomédica (formato real)
ALTER TABLE equipos ADD COLUMN IF NOT EXISTS nombre_equipo VARCHAR(180);
ALTER TABLE equipos ADD COLUMN IF NOT EXISTS servicio_ubicacion VARCHAR(120);
ALTER TABLE equipos ADD COLUMN IF NOT EXISTS codigo_interno VARCHAR(60);
ALTER TABLE equipos ADD COLUMN IF NOT EXISTS version_ficha VARCHAR(20);
ALTER TABLE equipos ADD COLUMN IF NOT EXISTS fecha_ficha DATE;
ALTER TABLE equipos ADD COLUMN IF NOT EXISTS anio_fabricacion INT;
ALTER TABLE equipos ADD COLUMN IF NOT EXISTS fecha_adquisicion DATE;
ALTER TABLE equipos ADD COLUMN IF NOT EXISTS fecha_puesta_funcionamiento DATE;
ALTER TABLE equipos ADD COLUMN IF NOT EXISTS representante VARCHAR(160);
ALTER TABLE equipos ADD COLUMN IF NOT EXISTS representante_direccion VARCHAR(220);
ALTER TABLE equipos ADD COLUMN IF NOT EXISTS representante_telefono VARCHAR(40);
ALTER TABLE equipos ADD COLUMN IF NOT EXISTS representante_email VARCHAR(120);
ALTER TABLE equipos ADD COLUMN IF NOT EXISTS corriente_operacion VARCHAR(40);
ALTER TABLE equipos ADD COLUMN IF NOT EXISTS potencia_va VARCHAR(40);
ALTER TABLE equipos ADD COLUMN IF NOT EXISTS frecuencia_hz VARCHAR(40);
ALTER TABLE equipos ADD COLUMN IF NOT EXISTS presion VARCHAR(40);
ALTER TABLE equipos ADD COLUMN IF NOT EXISTS capacidad VARCHAR(40);
ALTER TABLE equipos ADD COLUMN IF NOT EXISTS tecnologia_predominante VARCHAR(80);
ALTER TABLE equipos ADD COLUMN IF NOT EXISTS fuente_alimentacion VARCHAR(80);
ALTER TABLE equipos ADD COLUMN IF NOT EXISTS clasificacion_biomedica VARCHAR(40);
ALTER TABLE equipos ADD COLUMN IF NOT EXISTS nivel_riesgo VARCHAR(20);
ALTER TABLE equipos ADD COLUMN IF NOT EXISTS uso_clinico VARCHAR(40);
ALTER TABLE equipos ADD COLUMN IF NOT EXISTS requiere_calibracion BOOLEAN DEFAULT FALSE;
ALTER TABLE equipos ADD COLUMN IF NOT EXISTS periodicidad_calibracion VARCHAR(40);
ALTER TABLE equipos ADD COLUMN IF NOT EXISTS periodicidad_mantenimiento VARCHAR(40);
ALTER TABLE equipos ADD COLUMN IF NOT EXISTS manuales TEXT;
ALTER TABLE equipos ADD COLUMN IF NOT EXISTS planos TEXT;
ALTER TABLE equipos ADD COLUMN IF NOT EXISTS recomendaciones_fabricante TEXT;

-- Historial calibraciones / verificaciones
CREATE TABLE IF NOT EXISTS equipo_calibraciones (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    equipo_id       UUID NOT NULL REFERENCES equipos(id) ON DELETE CASCADE,
    tipo            VARCHAR(40) NOT NULL DEFAULT 'CALIBRACION',
    fecha           DATE,
    proxima_fecha   DATE,
    certificado     VARCHAR(120),
    resultado       VARCHAR(80),
    observaciones   TEXT,
    orden           INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS equipo_verificaciones (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    equipo_id       UUID NOT NULL REFERENCES equipos(id) ON DELETE CASCADE,
    fecha           DATE,
    proxima_fecha   DATE,
    resultado       VARCHAR(80),
    observaciones   TEXT,
    orden           INT DEFAULT 0
);
