-- Catálogos iniciales Dilaser / Alma y sedes
INSERT INTO ciudades (nombre, departamento) VALUES
('Medellín','Antioquia'),('Bogotá','Cundinamarca'),('Cali','Valle del Cauca'),
('Barranquilla','Atlántico'),('Bucaramanga','Santander'),('Pereira','Risaralda'),
('Cartagena','Bolívar'),('Manizales','Caldas');

INSERT INTO marcas (codigo, nombre, pais_origen) VALUES
('004','ALMA LASERS','Israel'),
('DAA','DAAVLIN','Estados Unidos'),
('DIN','DINO-LITE','Taiwan'),
('BRE','BRERA','Italia'),
('VIO','VIORA','Israel'),
('INV','INVASIX','Israel');

INSERT INTO tecnologias (nombre) VALUES
('Diodo laser'),
('Plataforma luz pulsada/laser'),
('Laser CO2 fraccionado'),
('Radiofrecuencia'),
('Luz pulsada'),
('Ultrasonido y Rf'),
('Camara de fototerapia'),
('Microscopio'),
('Sistema multiplataforma');

INSERT INTO lineas_producto (marca_id, codigo, nombre, tecnologia_id)
SELECT m.id, x.codigo, x.nombre, t.id
FROM (VALUES
    ('ALMA LASERS','032','HYBRID','Plataforma luz pulsada/laser'),
    ('ALMA LASERS','HXL','HARMONY XL PRO','Plataforma luz pulsada/laser'),
    ('ALMA LASERS','HLT','HARMONY LITE','Plataforma luz pulsada/laser'),
    ('ALMA LASERS','SOP','SOPRANO','Diodo laser'),
    ('ALMA LASERS','PIX','PIXEL CO2','Laser CO2 fraccionado'),
    ('ALMA LASERS','ACC','ACCENT PRIME','Radiofrecuencia'),
    ('DAAVLIN','FOT','FOTOTERAPIA','Camara de fototerapia'),
    ('DINO-LITE','MIC','MICROSCOPIO','Microscopio'),
    ('BRERA','IMP','IMPERIUM','Ultrasonido y Rf'),
    ('VIORA','TRI','TRIOS','Luz pulsada'),
    ('INVASIX','INM','INMODE','Radiofrecuencia')
) AS x(marca, codigo, nombre, tec)
JOIN marcas m ON m.nombre = x.marca
JOIN tecnologias t ON t.nombre = x.tec;

INSERT INTO modelos_equipo (marca_id, linea_id, tecnologia_id, nombre, voltage, registro_sanitario)
SELECT m.id, l.id, t.id, x.modelo, '120 V ac', x.reg
FROM (VALUES
    ('ALMA LASERS','HARMONY XL PRO','HARMONY XL PRO','Plataforma luz pulsada/laser','INVIMA 2008EBC-0001848'),
    ('ALMA LASERS','HARMONY LITE','Harmony Lite','Plataforma luz pulsada/laser','INVIMA 2008EBC-0001848'),
    ('ALMA LASERS','SOPRANO','Soprano Accord','Diodo laser','INVIMA 2008EBC-0001816'),
    ('ALMA LASERS','PIXEL CO2','PIXEL CO2','Laser CO2 fraccionado','2009EBC-0003969'),
    ('ALMA LASERS','ACCENT PRIME','Accent Prime','Radiofrecuencia','INVIMA2016EBC-0015526'),
    ('INVASIX','INMODE','IN MODE RF-BODY TITE','Radiofrecuencia','2018DM-0018058'),
    ('DAAVLIN','FOTOTERAPIA','2 series Custom Blue-8','Camara de fototerapia','Invima 2009EBC-0003822'),
    ('DINO-LITE','MICROSCOPIO','Dino-lite AD-413ZT','Microscopio','NA'),
    ('BRERA','IMPERIUM','Imperium','Ultrasonido y Rf','INVIMA 2009DM-0005056'),
    ('VIORA','TRIOS','Trios','Luz pulsada','Invima 2009 DM-0003789')
) AS x(marca, linea, modelo, tec, reg)
JOIN marcas m ON m.nombre = x.marca
JOIN lineas_producto l ON l.marca_id = m.id AND l.nombre = x.linea
JOIN tecnologias t ON t.nombre = x.tec;

INSERT INTO bodegas (codigo, nombre, ciudad, direccion) VALUES
('00105','SOPORTE TECNICO MEDELLIN','Medellín','Cra. 33 No. 7-77'),
('00101','SOPORTE TECNICO BOGOTA','Bogotá','Por confirmar');

INSERT INTO fabricantes_contacto (marca_id, nombre_area, emails)
SELECT id, 'RMA / Service', ARRAY['service@almalasers.com'] FROM marcas WHERE nombre='ALMA LASERS';