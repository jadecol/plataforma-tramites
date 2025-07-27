-- Asegúrate de que las contraseñas están hasheadas con BCrypt.
-- Para 'password123': $2a$10$w8M.2F9A9B.1C4D5E6F7G8H9I0J1K2L3M4N5O6P7Q8R9S0T1U2V3W4X5Y6Z7a8b9c0d
-- Puedes generar los hashes en tu aplicación con BCryptPasswordEncoder.encode("password123")
-- o usar un main temporal:
-- public static void main(String[] args) {
--    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
--    System.out.println(encoder.encode("password123"));
-- }

INSERT INTO usuarios (id_usuario, nombre_completo, tipo_documento, numero_documento, correo_electronico, telefono, rol, contrasena_hash, fecha_creacion, esta_activo, matricula_profesional, experiencia_acreditada) VALUES
(1, 'Administrador Demo', 'CC', '100000000', 'admin@demo.com', '3001234567', 'ADMIN', '$2a$10$oaDAZ9ZQPGS2NT0q2tNV1OjWztBGT3sIxDDmD84sgMQUGE0F4Mqxu', NOW(), TRUE, NULL, NULL)
ON CONFLICT (id_usuario) DO UPDATE SET
    nombre_completo = EXCLUDED.nombre_completo,
    correo_electronico = EXCLUDED.correo_electronico,
    rol = EXCLUDED.rol,
    contrasena_hash = EXCLUDED.contrasena_hash,
    esta_activo = EXCLUDED.esta_activo;

INSERT INTO usuarios (id_usuario, nombre_completo, tipo_documento, numero_documento, correo_electronico, telefono, rol, contrasena_hash, fecha_creacion, esta_activo, matricula_profesional, experiencia_acreditada) VALUES
(2, 'Solicitante Demo', 'CC', '100000001', 'solicitante@demo.com', '3002223344', 'SOLICITANTE', '$2a$10$r5foMBurzgbH86EBFupph.0Ks0kRfO2Ve177yZhkV73n5bIt7qihK', NOW(), TRUE, NULL, NULL)
ON CONFLICT (id_usuario) DO UPDATE SET
    nombre_completo = EXCLUDED.nombre_completo,
    correo_electronico = EXCLUDED.correo_electronico,
    rol = EXCLUDED.rol,
    contrasena_hash = EXCLUDED.contrasena_hash,
    esta_activo = EXCLUDED.esta_activo;

INSERT INTO usuarios (id_usuario, nombre_completo, tipo_documento, numero_documento, correo_electronico, telefono, rol, contrasena_hash, fecha_creacion, esta_activo, matricula_profesional, experiencia_acreditada) VALUES
(3, 'Revisor Demo', 'CC', '100000002', 'revisor@demo.com', '3003334455', 'REVISOR', '$2a$10$V9Ph3w/vhN58bx6PDWT9HuJ1OwBYJTiZPqwzcOc9yuLBQx3yUEBVK', NOW(), TRUE, 'MAT-R123', '5 años en ingeniería civil')
ON CONFLICT (id_usuario) DO UPDATE SET
    nombre_completo = EXCLUDED.nombre_completo,
    correo_electronico = EXCLUDED.correo_electronico,
    rol = EXCLUDED.rol,
    contrasena_hash = EXCLUDED.contrasena_hash,
    esta_activo = EXCLUDED.esta_activo,
    matricula_profesional = EXCLUDED.matricula_profesional,
    experiencia_acreditada = EXCLUDED.experiencia_acreditada;

-- Inserta catálogos base (con ON CONFLICT para evitar duplicados si ya existen)
INSERT INTO modalidades_tramite (id_modalidad_tramite, nombre) VALUES
(1, 'En Línea') ON CONFLICT (id_modalidad_tramite) DO UPDATE SET nombre = EXCLUDED.nombre;
INSERT INTO modalidades_tramite (id_modalidad_tramite, nombre) VALUES
(2, 'Presencial') ON CONFLICT (id_modalidad_tramite) DO UPDATE SET nombre = EXCLUDED.nombre;

INSERT INTO tipos_tramite (id_tipo_tramite, nombre) VALUES
(1, 'Licencia de Construcción') ON CONFLICT (id_tipo_tramite) DO UPDATE SET nombre = EXCLUDED.nombre;
INSERT INTO tipos_tramite (id_tipo_tramite, nombre) VALUES
(2, 'Permiso de Uso de Suelo') ON CONFLICT (id_tipo_tramite) DO UPDATE SET nombre = EXCLUDED.nombre;

INSERT INTO subtipos_tramite (id_subtipo_tramite, nombre) VALUES
(1, 'Obra Nueva') ON CONFLICT (id_subtipo_tramite) DO UPDATE SET nombre = EXCLUDED.nombre;
INSERT INTO subtipos_tramite (id_subtipo_tramite, nombre) VALUES
(2, 'Ampliación') ON CONFLICT (id_subtipo_tramite) DO UPDATE SET nombre = EXCLUDED.nombre;