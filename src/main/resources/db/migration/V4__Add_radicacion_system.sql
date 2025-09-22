-- Migración V4: Sistema de Radicación Completo
-- Agrega tabla de consecutivos de radicación y campo código DANE

-- Agregar campo codigo_dane a la tabla entidades
ALTER TABLE entidades
ADD COLUMN codigo_dane VARCHAR(5);

-- Actualizar entidades existentes con código DANE por defecto (Bogotá)
UPDATE entidades
SET codigo_dane = '11001'
WHERE codigo_dane IS NULL;

-- Crear tabla de consecutivos de radicación
CREATE TABLE consecutivos_radicacion (
    id_consecutivo BIGSERIAL PRIMARY KEY,
    entidad_id BIGINT NOT NULL,
    codigo_dane VARCHAR(5) NOT NULL,
    tipo_entidad VARCHAR(10) NOT NULL CHECK (tipo_entidad IN ('SECRETARIA', 'CURADURIA')),
    ano INTEGER NOT NULL,
    ultimo_consecutivo INTEGER NOT NULL DEFAULT 0,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    -- Constraints
    CONSTRAINT fk_consecutivo_entidad FOREIGN KEY (entidad_id) REFERENCES entidades(id) ON DELETE CASCADE,
    CONSTRAINT uk_consecutivo_entidad_ano UNIQUE (entidad_id, tipo_entidad, ano),
    CONSTRAINT chk_codigo_dane_format CHECK (codigo_dane ~ '^[0-9]{5}$'),
    CONSTRAINT chk_ano_valido CHECK (ano >= 2020 AND ano <= 2050),
    CONSTRAINT chk_consecutivo_positivo CHECK (ultimo_consecutivo >= 0)
);

-- Índices para optimizar consultas
CREATE INDEX idx_consecutivo_entidad ON consecutivos_radicacion(entidad_id);
CREATE INDEX idx_consecutivo_ano ON consecutivos_radicacion(ano);
CREATE INDEX idx_consecutivo_busqueda ON consecutivos_radicacion(entidad_id, tipo_entidad, ano);
CREATE INDEX idx_consecutivo_activo ON consecutivos_radicacion(activo) WHERE activo = TRUE;

-- Crear función para actualizar fecha_actualizacion automáticamente
CREATE OR REPLACE FUNCTION update_fecha_actualizacion()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_actualizacion = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para actualizar fecha_actualizacion en consecutivos_radicacion
CREATE TRIGGER tr_consecutivo_update_fecha
    BEFORE UPDATE ON consecutivos_radicacion
    FOR EACH ROW
    EXECUTE FUNCTION update_fecha_actualizacion();

-- Comentarios en las tablas y columnas para documentación
COMMENT ON TABLE consecutivos_radicacion IS 'Control de consecutivos para numeración oficial de radicación';
COMMENT ON COLUMN consecutivos_radicacion.codigo_dane IS 'Código DANE del municipio (5 dígitos)';
COMMENT ON COLUMN consecutivos_radicacion.tipo_entidad IS 'Tipo de entidad: SECRETARIA (0) o CURADURIA (CUR)';
COMMENT ON COLUMN consecutivos_radicacion.ano IS 'Año del consecutivo';
COMMENT ON COLUMN consecutivos_radicacion.ultimo_consecutivo IS 'Último número consecutivo generado en el año';

COMMENT ON COLUMN entidades.codigo_dane IS 'Código DANE del municipio donde opera la entidad';

-- Crear datos iniciales para el año actual si hay entidades existentes
INSERT INTO consecutivos_radicacion (entidad_id, codigo_dane, tipo_entidad, ano, ultimo_consecutivo, activo)
SELECT
    e.id,
    COALESCE(e.codigo_dane, '11001'),
    CASE
        WHEN LOWER(e.nombre) LIKE '%curadur%' OR LOWER(e.nombre) LIKE '%curador%' THEN 'CURADURIA'
        ELSE 'SECRETARIA'
    END,
    EXTRACT(YEAR FROM CURRENT_DATE),
    0,
    TRUE
FROM entidades e
WHERE e.activo = TRUE
AND NOT EXISTS (
    SELECT 1 FROM consecutivos_radicacion cr
    WHERE cr.entidad_id = e.id
    AND cr.ano = EXTRACT(YEAR FROM CURRENT_DATE)
);

-- Verificar integridad de los datos creados
DO $$
DECLARE
    total_entidades INTEGER;
    total_consecutivos INTEGER;
BEGIN
    SELECT COUNT(*) INTO total_entidades FROM entidades WHERE activo = TRUE;
    SELECT COUNT(*) INTO total_consecutivos FROM consecutivos_radicacion WHERE ano = EXTRACT(YEAR FROM CURRENT_DATE);

    RAISE NOTICE 'Migración V4 completada:';
    RAISE NOTICE '- Entidades activas: %', total_entidades;
    RAISE NOTICE '- Consecutivos creados para año %: %', EXTRACT(YEAR FROM CURRENT_DATE), total_consecutivos;

    IF total_consecutivos = 0 AND total_entidades > 0 THEN
        RAISE WARNING 'No se crearon consecutivos automáticamente. Verificar configuración.';
    END IF;
END $$;