-- ============================================================================
-- CREAR TABLA TRAMITES UNIFICADA
-- ============================================================================

-- TABLA: tramites (unificación de solicitudes)
CREATE TABLE IF NOT EXISTS tramites (
    id_tramite BIGSERIAL PRIMARY KEY,
    
    -- CAMPOS MULTI-TENANT
    id_entidad BIGINT NOT NULL REFERENCES entidades(id),
    
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
    id_solicitante BIGINT NOT NULL REFERENCES usuarios(id_usuario),
    id_revisor_asignado BIGINT REFERENCES usuarios(id_usuario),
    
    -- CLASIFICACIÓN DE TRÁMITES
    id_tipo_tramite BIGINT NOT NULL REFERENCES tipos_tramite(id),
    id_modalidad_tramite BIGINT REFERENCES modalidades_tramite(id),
    id_subtipo_tramite BIGINT REFERENCES subtipos_tramite(id),
    
    -- CHECKS
    CONSTRAINT chk_estado_valido CHECK (estado_actual IN (
        'RADICADO', 'ASIGNADO', 'EN_REVISION', 'PENDIENTE_DOCUMENTOS',
        'EN_ESPERA_CONCEPTO', 'CONCEPTO_FAVORABLE', 'CONCEPTO_DESFAVORABLE',
        'APROBADO', 'RECHAZADO', 'ARCHIVADO', 'CANCELADO'
    ))
);

-- Crear índices para performance
CREATE INDEX IF NOT EXISTS idx_tramites_entidad ON tramites(id_entidad);
CREATE INDEX IF NOT EXISTS idx_tramites_estado ON tramites(estado_actual);
CREATE INDEX IF NOT EXISTS idx_tramites_solicitante ON tramites(id_solicitante);
CREATE INDEX IF NOT EXISTS idx_tramites_revisor ON tramites(id_revisor_asignado);
CREATE INDEX IF NOT EXISTS idx_tramites_numero_radicacion ON tramites(numero_radicacion);

-- Insertar entidades de prueba
INSERT INTO entidades (nombre, nit, direccion, telefono, email, activo) VALUES
('Curaduría Urbana Primera de Bogotá', '900123456-1', 'Calle 72 # 10-07', '601-2345678', 'info@curaduria1.gov.co', true),
('Secretaría de Planeación Municipal - Medellín', '890234567-2', 'Carrera 55 # 42-180', '604-3456789', 'planeacion@medellin.gov.co', true),
('Curaduría Urbana de Cali', '890345678-3', 'Avenida 6N # 23-61', '602-4567890', 'curaduria@cali.gov.co', true)
ON CONFLICT (nit) DO NOTHING;

-- Insertar usuarios de prueba (contraseñas hasheadas para 'password123')
INSERT INTO usuarios (nombre_completo, tipo_documento, numero_documento, correo_electronico, telefono, rol, contrasena_hash, fecha_creacion, esta_activo, id_entidad) VALUES
('Administrador Global', 'CC', '12345678', 'admin.global@gestion.com', '3001234567', 'ADMIN_GLOBAL', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', CURRENT_TIMESTAMP, true, NULL),
('Admin Curaduría 1', 'CC', '23456789', 'admin.curaduria1@gestion.com', '3012345678', 'ADMIN_ENTIDAD', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', CURRENT_TIMESTAMP, true, 1),
('Revisor Arquitecto', 'CC', '34567890', 'arquitecto.revisor@gestion.com', '3123456789', 'REVISOR', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', CURRENT_TIMESTAMP, true, 1),
('Ciudadano Ejemplo', 'CC', '45678901', 'ciudadano@email.com', '3234567890', 'SOLICITANTE', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', CURRENT_TIMESTAMP, true, NULL)
ON CONFLICT (correo_electronico) DO NOTHING;

-- Insertar trámites de prueba
INSERT INTO tramites (id_entidad, numero_radicacion, fecha_radicacion, objeto_tramite, descripcion_proyecto, direccion_inmueble, estado_actual, id_solicitante, id_tipo_tramite) VALUES
(1, '11001-1-2024-00001', CURRENT_DATE, 'Licencia de Construcción', 'Construcción de vivienda unifamiliar', 'Calle 80 # 15-20', 'RADICADO', 
 (SELECT id_usuario FROM usuarios WHERE correo_electronico = 'ciudadano@email.com'), 1),
(1, '11001-1-2024-00002', CURRENT_DATE - 5, 'Licencia de Urbanización', 'Desarrollo urbanístico 50 lotes', 'Autopista Norte Km 25', 'EN_REVISION', 
 (SELECT id_usuario FROM usuarios WHERE correo_electronico = 'ciudadano@email.com'), 1)
ON CONFLICT (numero_radicacion) DO NOTHING;

SELECT 'Tabla tramites y datos de prueba creados correctamente' AS resultado;