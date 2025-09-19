-- ============================================================================
-- SCRIPT DE MIGRACIÓN MULTI-TENANT
-- Plataforma de Gestión de Trámites Urbanísticos
-- Migración de Solicitudes a Trámites + Implementación Multi-tenant
-- ============================================================================

-- PASO 1: Crear tabla tramites con estructura unificada
CREATE TABLE IF NOT EXISTS tramites (
    id_tramite BIGSERIAL PRIMARY KEY,
    
    -- CAMPOS MULTI-TENANT
    id_entidad BIGINT NOT NULL,
    
    -- NUMERACIÓN Y RADICACIÓN
    numero_radicacion VARCHAR(50) NOT NULL UNIQUE,
    fecha_radicacion DATE NOT NULL DEFAULT CURRENT_DATE,
    
    -- INFORMACIÓN DEL TRÁMITE
    objeto_tramite VARCHAR(500),
    descripcion_proyecto TEXT,
    direccion_inmueble VARCHAR(255),
    condicion_radicacion VARCHAR(255),
    
    -- GESTIÓN DE ESTADOS
    estado_actual VARCHAR(50) NOT NULL DEFAULT 'RADICADO',
    comentarios_revisor TEXT,
    
    -- FECHAS DE GESTIÓN
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_ultimo_cambio_estado TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_limite_proximo DATE,
    fecha_limite_completar DATE,
    fecha_finalizacion TIMESTAMP,
    
    -- RELACIONES CON USUARIOS
    id_solicitante BIGINT NOT NULL,
    id_revisor_asignado BIGINT,
    
    -- CLASIFICACIÓN DE TRÁMITES
    id_tipo_tramite BIGINT NOT NULL,
    id_modalidad_tramite BIGINT,
    id_subtipo_tramite BIGINT,
    
    -- CONSTRAINTS
    CONSTRAINT fk_tramite_entidad FOREIGN KEY (id_entidad) REFERENCES entidades(id),
    CONSTRAINT fk_tramite_solicitante FOREIGN KEY (id_solicitante) REFERENCES usuarios(id_usuario),
    CONSTRAINT fk_tramite_revisor FOREIGN KEY (id_revisor_asignado) REFERENCES usuarios(id_usuario),
    CONSTRAINT fk_tramite_tipo FOREIGN KEY (id_tipo_tramite) REFERENCES tipos_tramite(id),
    CONSTRAINT fk_tramite_modalidad FOREIGN KEY (id_modalidad_tramite) REFERENCES modalidades_tramite(id),
    CONSTRAINT fk_tramite_subtipo FOREIGN KEY (id_subtipo_tramite) REFERENCES subtipos_tramite(id),
    
    -- CHECKS
    CONSTRAINT chk_estado_valido CHECK (estado_actual IN (
        'RADICADO', 'ASIGNADO', 'EN_REVISION', 'PENDIENTE_DOCUMENTOS',
        'EN_ESPERA_CONCEPTO', 'CONCEPTO_FAVORABLE', 'CONCEPTO_DESFAVORABLE',
        'APROBADO', 'RECHAZADO', 'ARCHIVADO', 'CANCELADO'
    ))
);

-- PASO 2: Crear índices para performance multi-tenant
CREATE INDEX idx_tramites_entidad ON tramites(id_entidad);
CREATE INDEX idx_tramites_estado ON tramites(estado_actual);
CREATE INDEX idx_tramites_solicitante ON tramites(id_solicitante);
CREATE INDEX idx_tramites_revisor ON tramites(id_revisor_asignado);
CREATE INDEX idx_tramites_fecha_radicacion ON tramites(fecha_radicacion);
CREATE INDEX idx_tramites_numero_radicacion ON tramites(numero_radicacion);

-- Índice compuesto para consultas multi-tenant
CREATE INDEX idx_tramites_entidad_estado ON tramites(id_entidad, estado_actual);
CREATE INDEX idx_tramites_entidad_fecha ON tramites(id_entidad, fecha_radicacion);

-- PASO 3: Migrar datos de solicitudes a tramites (si existe tabla solicitudes)
INSERT INTO tramites (
    id_entidad,
    numero_radicacion,
    fecha_radicacion,
    objeto_tramite,
    descripcion_proyecto,
    direccion_inmueble,
    condicion_radicacion,
    estado_actual,
    comentarios_revisor,
    fecha_creacion,
    fecha_ultimo_cambio_estado,
    fecha_limite_proximo,
    fecha_limite_completar,
    id_solicitante,
    id_revisor_asignado,
    id_tipo_tramite,
    id_modalidad_tramite,
    id_subtipo_tramite
)
SELECT 
    -- Determinar entidad basado en el solicitante
    COALESCE(u.id_entidad, 1) as id_entidad, -- Default a entidad 1 si no tiene
    s.numero_radicacion,
    s.fecha_radicacion,
    s.objeto_tramite,
    s.descripcion_proyecto,
    s.direccion_inmueble,
    s.condicion_radicacion,
    -- Mapear estados antiguos a nuevos
    CASE 
        WHEN s.estado_actual = 'PENDIENTE' THEN 'RADICADO'
        WHEN s.estado_actual = 'EN_PROCESO' THEN 'EN_REVISION'
        WHEN s.estado_actual = 'FINALIZADO' THEN 'APROBADO'
        ELSE s.estado_actual
    END as estado_actual,
    NULL as comentarios_revisor, -- No existe en solicitudes
    COALESCE(s.fecha_creacion, CURRENT_TIMESTAMP) as fecha_creacion,
    s.fecha_ultimo_cambio_estado,
    s.fecha_limite_proximo,
    s.fecha_limite_completar,
    s.id_solicitante,
    s.id_revisor_asignado,
    s.id_tipo_tramite,
    s.id_modalidad_tramite,
    s.id_subtipo_tramite
FROM solicitudes s
LEFT JOIN usuarios u ON s.id_solicitante = u.id_usuario
WHERE NOT EXISTS (
    SELECT 1 FROM tramites t WHERE t.numero_radicacion = s.numero_radicacion
);

-- PASO 4: Actualizar usuarios sin entidad (SOLICITANTES externos)
-- Los SOLICITANTES que no tienen entidad mantienen id_entidad = NULL
-- Los demás roles deben tener entidad obligatoriamente

UPDATE usuarios 
SET id_entidad = NULL 
WHERE rol = 'SOLICITANTE' 
  AND id_entidad IS NOT NULL 
  AND NOT EXISTS (
    SELECT 1 FROM entidades e WHERE e.id = usuarios.id_entidad
  );

-- PASO 5: Crear función para generar números de radicación
CREATE OR REPLACE FUNCTION generar_numero_radicacion(
    p_entidad_id BIGINT,
    p_codigo_dane VARCHAR(5) DEFAULT '11001'
) RETURNS VARCHAR(50) AS $$
DECLARE
    v_year VARCHAR(4);
    v_consecutivo INT;
    v_numero_radicacion VARCHAR(50);
BEGIN
    -- Obtener año actual
    v_year := EXTRACT(YEAR FROM CURRENT_DATE)::VARCHAR;
    
    -- Contar trámites del año para esta entidad
    SELECT COALESCE(COUNT(*), 0) + 1
    INTO v_consecutivo
    FROM tramites
    WHERE id_entidad = p_entidad_id
      AND EXTRACT(YEAR FROM fecha_radicacion) = EXTRACT(YEAR FROM CURRENT_DATE);
    
    -- Generar número con formato: DANE-ENTIDAD-AÑO-CONSECUTIVO
    v_numero_radicacion := p_codigo_dane || '-' || 
                          p_entidad_id::VARCHAR || '-' || 
                          v_year || '-' || 
                          LPAD(v_consecutivo::VARCHAR, 5, '0');
    
    RETURN v_numero_radicacion;
END;
$$ LANGUAGE plpgsql;

-- PASO 6: Crear trigger para auto-generar números de radicación
CREATE OR REPLACE FUNCTION trigger_generar_radicacion()
RETURNS TRIGGER AS $$
BEGIN
    -- Solo generar si no viene numero_radicacion
    IF NEW.numero_radicacion IS NULL OR NEW.numero_radicacion = '' THEN
        NEW.numero_radicacion := generar_numero_radicacion(NEW.id_entidad);
    END IF;
    
    -- Establecer fechas automáticamente
    IF NEW.fecha_radicacion IS NULL THEN
        NEW.fecha_radicacion := CURRENT_DATE;
    END IF;
    
    IF NEW.fecha_creacion IS NULL THEN
        NEW.fecha_creacion := CURRENT_TIMESTAMP;
    END IF;
    
    NEW.fecha_ultimo_cambio_estado := CURRENT_TIMESTAMP;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_tramite_before_insert
    BEFORE INSERT ON tramites
    FOR EACH ROW
    EXECUTE FUNCTION trigger_generar_radicacion();

-- PASO 7: Crear trigger para actualizar fecha_ultimo_cambio_estado
CREATE OR REPLACE FUNCTION trigger_actualizar_fecha_cambio()
RETURNS TRIGGER AS $$
BEGIN
    -- Si cambió el estado, actualizar fecha
    IF OLD.estado_actual != NEW.estado_actual THEN
        NEW.fecha_ultimo_cambio_estado := CURRENT_TIMESTAMP;
        
        -- Si es un estado final, establecer fecha_finalizacion
        IF NEW.estado_actual IN ('APROBADO', 'RECHAZADO', 'ARCHIVADO', 'CANCELADO') THEN
            NEW.fecha_finalizacion := CURRENT_TIMESTAMP;
        END IF;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_tramite_before_update
    BEFORE UPDATE ON tramites
    FOR EACH ROW
    EXECUTE FUNCTION trigger_actualizar_fecha_cambio();

-- PASO 8: Crear vista para consultas públicas (datos no sensibles)
CREATE OR REPLACE VIEW vista_tramites_publicos AS
SELECT 
    numero_radicacion,
    fecha_radicacion,
    objeto_tramite,
    estado_actual,
    fecha_ultimo_cambio_estado,
    fecha_limite_proximo,
    fecha_limite_completar,
    e.nombre as nombre_entidad,
    tt.nombre as tipo_tramite
FROM tramites t
LEFT JOIN entidades e ON t.id_entidad = e.id
LEFT JOIN tipos_tramite tt ON t.id_tipo_tramite = tt.id
WHERE t.estado_actual NOT IN ('CANCELADO'); -- No mostrar cancelados en consulta pública

-- PASO 9: Crear función para validar transiciones de estado
CREATE OR REPLACE FUNCTION validar_transicion_estado(
    estado_actual VARCHAR(50),
    nuevo_estado VARCHAR(50)
) RETURNS BOOLEAN AS $
BEGIN
    RETURN CASE estado_actual
        WHEN 'RADICADO' THEN nuevo_estado IN ('ASIGNADO', 'CANCELADO')
        WHEN 'ASIGNADO' THEN nuevo_estado IN ('EN_REVISION', 'CANCELADO')
        WHEN 'EN_REVISION' THEN nuevo_estado IN ('PENDIENTE_DOCUMENTOS', 'EN_ESPERA_CONCEPTO', 'APROBADO', 'RECHAZADO')
        WHEN 'PENDIENTE_DOCUMENTOS' THEN nuevo_estado IN ('EN_REVISION', 'ARCHIVADO')
        WHEN 'EN_ESPERA_CONCEPTO' THEN nuevo_estado IN ('CONCEPTO_FAVORABLE', 'CONCEPTO_DESFAVORABLE')
        WHEN 'CONCEPTO_FAVORABLE' THEN nuevo_estado IN ('APROBADO')
        WHEN 'CONCEPTO_DESFAVORABLE' THEN nuevo_estado IN ('RECHAZADO')
        ELSE FALSE -- Estados finales no pueden cambiar
    END;
END;
$ LANGUAGE plpgsql;

-- PASO 10: Crear trigger para validar transiciones de estado
CREATE OR REPLACE FUNCTION trigger_validar_estado()
RETURNS TRIGGER AS $
BEGIN
    -- Validar transición solo si el estado cambió
    IF OLD.estado_actual != NEW.estado_actual THEN
        IF NOT validar_transicion_estado(OLD.estado_actual, NEW.estado_actual) THEN
            RAISE EXCEPTION 'Transición de estado no válida: de % a %', OLD.estado_actual, NEW.estado_actual;
        END IF;
    END IF;
    
    RETURN NEW;
END;
$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_validar_transicion_estado
    BEFORE UPDATE ON tramites
    FOR EACH ROW
    EXECUTE FUNCTION trigger_validar_estado();

-- PASO 11: Insertar datos de prueba para multi-tenancy
-- Crear entidades de prueba si no existen
INSERT INTO entidades (nombre, nit, direccion, telefono, email, activo) VALUES
('Curaduría Urbana Primera de Bogotá', '900123456-1', 'Calle 72 # 10-07', '601-2345678', 'info@curaduria1.gov.co', true),
('Secretaría de Planeación Municipal - Medellín', '890234567-2', 'Carrera 55 # 42-180', '604-3456789', 'planeacion@medellin.gov.co', true),
('Curaduría Urbana de Cali', '890345678-3', 'Avenida 6N # 23-61', '602-4567890', 'curaduria@cali.gov.co', true)
ON CONFLICT (nit) DO NOTHING;

-- Crear usuarios de prueba multi-tenant si no existen
-- Admin Global
INSERT INTO usuarios (nombre_completo, tipo_documento, numero_documento, correo_electronico, telefono, rol, contrasena_hash, fecha_creacion, esta_activo, id_entidad) VALUES
('Administrador Global', 'CC', '12345678', 'admin.global@gestion.com', '3001234567', 'ADMIN_GLOBAL', '$2a$10$encrypted_password_here', CURRENT_TIMESTAMP, true, NULL)
ON CONFLICT (correo_electronico) DO NOTHING;

-- Admin Entidad para cada curaduría
INSERT INTO usuarios (nombre_completo, tipo_documento, numero_documento, correo_electronico, telefono, rol, contrasena_hash, fecha_creacion, esta_activo, id_entidad) VALUES
('Admin Curaduría 1', 'CC', '23456789', 'admin.curaduria1@gestion.com', '3012345678', 'ADMIN_ENTIDAD', '$2a$10$encrypted_password_here', CURRENT_TIMESTAMP, true, 1),
('Admin Secretaría Medellín', 'CC', '34567890', 'admin.medellin@gestion.com', '3123456789', 'ADMIN_ENTIDAD', '$2a$10$encrypted_password_here', CURRENT_TIMESTAMP, true, 2),
('Admin Curaduría Cali', 'CC', '45678901', 'admin.cali@gestion.com', '3234567890', 'ADMIN_ENTIDAD', '$2a$10$encrypted_password_here', CURRENT_TIMESTAMP, true, 3)
ON CONFLICT (correo_electronico) DO NOTHING;

-- Revisores para cada entidad
INSERT INTO usuarios (nombre_completo, tipo_documento, numero_documento, correo_electronico, telefono, rol, contrasena_hash, fecha_creacion, esta_activo, id_entidad, matricula_profesional) VALUES
('Arq. Juan Pérez', 'CC', '56789012', 'arquitecto1@curaduria1.gov.co', '3345678901', 'REVISOR', '$2a$10$encrypted_password_here', CURRENT_TIMESTAMP, true, 1, 'ARQ-12345'),
('Ing. María González', 'CC', '67890123', 'ingeniera1@medellin.gov.co', '3456789012', 'REVISOR', '$2a$10$encrypted_password_here', CURRENT_TIMESTAMP, true, 2, 'ING-23456'),
('Arq. Carlos Ramírez', 'CC', '78901234', 'arquitecto1@cali.gov.co', '3567890123', 'REVISOR', '$2a$10$encrypted_password_here', CURRENT_TIMESTAMP, true, 3, 'ARQ-34567')
ON CONFLICT (correo_electronico) DO NOTHING;

-- Solicitantes (sin entidad específica)
INSERT INTO usuarios (nombre_completo, tipo_documento, numero_documento, correo_electronico, telefono, rol, contrasena_hash, fecha_creacion, esta_activo, id_entidad) VALUES
('Pedro Ciudadano', 'CC', '89012345', 'pedro.ciudadano@email.com', '3678901234', 'SOLICITANTE', '$2a$10$encrypted_password_here', CURRENT_TIMESTAMP, true, NULL),
('Ana Constructora', 'CC', '90123456', 'ana.constructora@email.com', '3789012345', 'SOLICITANTE', '$2a$10$encrypted_password_here', CURRENT_TIMESTAMP, true, NULL)
ON CONFLICT (correo_electronico) DO NOTHING;

-- PASO 12: Crear trámites de prueba para cada entidad
-- Trámites para Curaduría 1 (Bogotá)
INSERT INTO tramites (id_entidad, numero_radicacion, fecha_radicacion, objeto_tramite, descripcion_proyecto, direccion_inmueble, estado_actual, id_solicitante, id_tipo_tramite) VALUES
(1, '11001-1-2024-00001', CURRENT_DATE, 'Licencia de Construcción', 'Construcción de vivienda unifamiliar', 'Calle 80 # 15-20', 'RADICADO', (SELECT id_usuario FROM usuarios WHERE correo_electronico = 'pedro.ciudadano@email.com'), 1),
(1, '11001-1-2024-00002', CURRENT_DATE - 5, 'Licencia de Urbanización', 'Desarrollo urbanístico 50 lotes', 'Autopista Norte Km 25', 'EN_REVISION', (SELECT id_usuario FROM usuarios WHERE correo_electronico = 'ana.constructora@email.com'), 1);

-- Trámites para Secretaría Medellín
INSERT INTO tramites (id_entidad, numero_radicacion, fecha_radicacion, objeto_tramite, descripcion_proyecto, direccion_inmueble, estado_actual, id_solicitante, id_tipo_tramite) VALUES
(2, '05001-2-2024-00001', CURRENT_DATE - 2, 'Concepto de Norma Urbanística', 'Consulta sobre uso del suelo', 'Carrera 70 # 45-32', 'ASIGNADO', (SELECT id_usuario FROM usuarios WHERE correo_electronico = 'pedro.ciudadano@email.com'), 2);

-- Trámites para Curaduría Cali
INSERT INTO tramites (id_entidad, numero_radicacion, fecha_radicacion, objeto_tramite, descripcion_proyecto, direccion_inmueble, estado_actual, id_solicitante, id_tipo_tramite) VALUES
(3, '76001-3-2024-00001', CURRENT_DATE - 10, 'Licencia de Construcción', 'Edificio de oficinas 10 pisos', 'Avenida 5ta # 12-50', 'APROBADO', (SELECT id_usuario FROM usuarios WHERE correo_electronico = 'ana.constructora@email.com'), 1);

-- PASO 13: Crear políticas de seguridad a nivel de base de datos (RLS)
-- Habilitar Row Level Security en la tabla tramites
ALTER TABLE tramites ENABLE ROW LEVEL SECURITY;

-- Política para ADMIN_GLOBAL (puede ver todos los trámites)
CREATE POLICY admin_global_policy ON tramites
    FOR ALL
    TO admin_global_role
    USING (true);

-- Política para usuarios de entidad (solo ven trámites de su entidad)
CREATE POLICY entidad_policy ON tramites
    FOR ALL
    TO entidad_user_role
    USING (id_entidad = current_setting('app.current_entity_id')::bigint);

-- PASO 14: Crear stored procedures para operaciones comunes
-- Procedure para cambiar estado de trámite
CREATE OR REPLACE FUNCTION cambiar_estado_tramite(
    p_tramite_id BIGINT,
    p_nuevo_estado VARCHAR(50),
    p_comentarios TEXT DEFAULT NULL,
    p_usuario_id BIGINT DEFAULT NULL
) RETURNS BOOLEAN AS $
DECLARE
    v_estado_actual VARCHAR(50);
    v_entidad_id BIGINT;
BEGIN
    -- Obtener estado actual
    SELECT estado_actual, id_entidad INTO v_estado_actual, v_entidad_id
    FROM tramites WHERE id_tramite = p_tramite_id;
    
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Trámite no encontrado: %', p_tramite_id;
    END IF;
    
    -- Validar transición
    IF NOT validar_transicion_estado(v_estado_actual, p_nuevo_estado) THEN
        RAISE EXCEPTION 'Transición no válida: de % a %', v_estado_actual, p_nuevo_estado;
    END IF;
    
    -- Actualizar trámite
    UPDATE tramites 
    SET estado_actual = p_nuevo_estado,
        comentarios_revisor = COALESCE(p_comentarios, comentarios_revisor),
        id_revisor_asignado = CASE 
            WHEN p_usuario_id IS NOT NULL AND p_nuevo_estado = 'ASIGNADO' 
            THEN p_usuario_id 
            ELSE id_revisor_asignado 
        END
    WHERE id_tramite = p_tramite_id;
    
    RETURN TRUE;
END;
$ LANGUAGE plpgsql;

-- PASO 15: Crear índices adicionales para performance
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_tramites_consulta_publica 
ON tramites(numero_radicacion, estado_actual) 
WHERE estado_actual != 'CANCELADO';

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_tramites_por_revisor 
ON tramites(id_revisor_asignado, estado_actual) 
WHERE id_revisor_asignado IS NOT NULL;

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_tramites_pendientes 
ON tramites(id_entidad, estado_actual, fecha_radicacion) 
WHERE estado_actual IN ('RADICADO', 'ASIGNADO', 'EN_REVISION', 'PENDIENTE_DOCUMENTOS');

-- PASO 16: Crear backup de tabla solicitudes antes de eliminar
CREATE TABLE solicitudes_backup AS 
SELECT *, CURRENT_TIMESTAMP as fecha_backup 
FROM solicitudes;

-- PASO 17: Verificaciones finales
-- Verificar que todos los trámites tienen entidad
SELECT COUNT(*) as tramites_sin_entidad 
FROM tramites 
WHERE id_entidad IS NULL;

-- Verificar números de radicación únicos
SELECT numero_radicacion, COUNT(*) 
FROM tramites 
GROUP BY numero_radicacion 
HAVING COUNT(*) > 1;

-- Verificar distribución por entidad
SELECT e.nombre, COUNT(t.id_tramite) as total_tramites
FROM entidades e
LEFT JOIN tramites t ON e.id = t.id_entidad
GROUP BY e.id, e.nombre
ORDER BY total_tramites DESC;

-- PASO 18: Comentarios finales y documentación
COMMENT ON TABLE tramites IS 'Tabla unificada de trámites urbanísticos con soporte multi-tenant';
COMMENT ON COLUMN tramites.id_entidad IS 'ID de la entidad responsable (curaduría/secretaría) - CRÍTICO para multi-tenancy';
COMMENT ON COLUMN tramites.numero_radicacion IS 'Número único de radicación formato: DANE-ENTIDAD-AÑO-CONSECUTIVO';
COMMENT ON COLUMN tramites.estado_actual IS 'Estado actual del trámite según flujo definido';

-- Mensaje final
DO $
BEGIN
    RAISE NOTICE '==========================================';
    RAISE NOTICE 'MIGRACIÓN MULTI-TENANT COMPLETADA';
    RAISE NOTICE '==========================================';
    RAISE NOTICE 'Total trámites migrados: %', (SELECT COUNT(*) FROM tramites);
    RAISE NOTICE 'Total entidades: %', (SELECT COUNT(*) FROM entidades WHERE activo = true);
    RAISE NOTICE 'Usuarios por rol:';
    RAISE NOTICE '- ADMIN_GLOBAL: %', (SELECT COUNT(*) FROM usuarios WHERE rol = 'ADMIN_GLOBAL');
    RAISE NOTICE '- ADMIN_ENTIDAD: %', (SELECT COUNT(*) FROM usuarios WHERE rol = 'ADMIN_ENTIDAD');
    RAISE NOTICE '- REVISOR: %', (SELECT COUNT(*) FROM usuarios WHERE rol = 'REVISOR');
    RAISE NOTICE '- SOLICITANTE: %', (SELECT COUNT(*) FROM usuarios WHERE rol = 'SOLICITANTE');
    RAISE NOTICE '==========================================';
    RAISE NOTICE 'La tabla solicitudes se mantuvo como backup';
    RAISE NOTICE 'Elimínela cuando confirme que la migración es exitosa';
    RAISE NOTICE '==========================================';
END $;