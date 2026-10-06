-- Campos extra remisión + trazabilidad envíos
ALTER TABLE remision_items ADD COLUMN IF NOT EXISTS motivo VARCHAR(40);
ALTER TABLE remisiones ADD COLUMN IF NOT EXISTS firma_elaboro TEXT;
ALTER TABLE remisiones ADD COLUMN IF NOT EXISTS firma_recibio TEXT;
ALTER TABLE remisiones ADD COLUMN IF NOT EXISTS email_destino VARCHAR(180);

CREATE TABLE IF NOT EXISTS envios_trazabilidad (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    modulo          VARCHAR(40) NOT NULL,
    entidad_id      UUID,
    canal           VARCHAR(20) NOT NULL,
    destinatario    VARCHAR(180) NOT NULL,
    asunto          VARCHAR(220),
    cuerpo          TEXT,
    enviado_por     UUID,
    enviado_por_email VARCHAR(180),
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_envios_mod ON envios_trazabilidad (modulo, entidad_id);
