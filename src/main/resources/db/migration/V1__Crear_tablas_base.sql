-- ============================================================================
-- CREAR TABLAS BASE PARA PLATAFORMA TRÁMITES
-- ============================================================================

-- TABLA: entidades (curadurías/secretarías)
CREATE TABLE IF NOT EXISTS entidades (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL UNIQUE,
    nit VARCHAR(50) NOT NULL UNIQUE,
    direccion VARCHAR(255),
    telefono VARCHAR(20),
    email VARCHAR(100),
    sitio_web VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- TABLA: tipos_tramite
CREATE TABLE IF NOT EXISTS tipos_tramite (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    activo BOOLEAN DEFAULT true
);

-- TABLA: modalidades_tramite
CREATE TABLE IF NOT EXISTS modalidades_tramite (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    id_tipo_tramite BIGINT REFERENCES tipos_tramite(id),
    activo BOOLEAN DEFAULT true
);

-- TABLA: subtipos_tramite
CREATE TABLE IF NOT EXISTS subtipos_tramite (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    id_tipo_tramite BIGINT REFERENCES tipos_tramite(id),
    activo BOOLEAN DEFAULT true
);

-- TABLA: usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario BIGSERIAL PRIMARY KEY,
    nombre_completo VARCHAR(255) NOT NULL,
    tipo_documento VARCHAR(10) NOT NULL,
    numero_documento VARCHAR(20) NOT NULL UNIQUE,
    correo_electronico VARCHAR(100) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    rol VARCHAR(50) NOT NULL,
    contrasena_hash VARCHAR(255),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_ultima_sesion TIMESTAMP,
    esta_activo BOOLEAN DEFAULT true,
    matricula_profesional VARCHAR(50),
    experiencia_acreditada TEXT,
    id_entidad BIGINT REFERENCES entidades(id)
);

-- Insertar datos básicos de tipos de trámite
INSERT INTO tipos_tramite (nombre, descripcion) VALUES
('Licencia Urbanística', 'Licencias de construcción, urbanización, etc.'),
('Concepto Técnico', 'Conceptos de norma urbanística'),
('Solicitud General', 'Solicitudes de nomenclatura, uso del suelo, etc.')
ON CONFLICT DO NOTHING;

-- Insertar modalidades básicas
INSERT INTO modalidades_tramite (nombre, descripcion, id_tipo_tramite) VALUES
('Urbanización', 'Licencia de urbanización', 1),
('Construcción', 'Licencia de construcción', 1),
('Ampliación', 'Licencia de ampliación', 1)
ON CONFLICT DO NOTHING;

-- Crear índices básicos
CREATE INDEX IF NOT EXISTS idx_usuarios_correo ON usuarios(correo_electronico);
CREATE INDEX IF NOT EXISTS idx_usuarios_entidad ON usuarios(id_entidad);
CREATE INDEX IF NOT EXISTS idx_usuarios_rol ON usuarios(rol);

-- Comentarios
COMMENT ON TABLE entidades IS 'Curadurías urbanas y secretarías de planeación';
COMMENT ON TABLE usuarios IS 'Usuarios del sistema con roles específicos';
COMMENT ON COLUMN usuarios.id_entidad IS 'Entidad a la que pertenece el usuario (NULL para SOLICITANTES)';

-- Verificación final
SELECT 'Tablas base creadas correctamente' AS resultado;