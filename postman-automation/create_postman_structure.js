require('dotenv').config(); // Carga las variables de entorno del archivo .env
const axios = require('axios');

const POSTMAN_API_KEY = process.env.POSTMAN_API_KEY;
const BASE_URL_POSTMAN_API = 'https://api.getpostman.com';

if (!POSTMAN_API_KEY) {
    console.error('Error: La variable de entorno POSTMAN_API_KEY no está configurada en el archivo .env');
    process.exit(1);
}

const COLLECTION_NAME = 'Flujo Completo Trámites (Automatizado)';
const ENVIRONMENT_NAME = 'Plataforma Tramites DEV (Automatizado)';

// ======================================================
// 1. Definición del Entorno
// ======================================================
const environmentData = {
    name: ENVIRONMENT_NAME,
    values: [
        { "key": "baseUrl", "value": "http://localhost:8080", "enabled": true },
        { "key": "adminGlobalUser", "value": "admin.global@gestion.com", "enabled": true },
        { "key": "adminGlobalPassword", "value": "AdminGlobal123!", "enabled": true },
        { "key": "jwtTokenAdmin", "value": "", "enabled": true },
        { "key": "idEntidadExistente", "value": "1", "enabled": true }, // Ajusta este ID a tu entidad base
        { "key": "adminEntidadPassword", "value": "AdminEntidadPass123!", "enabled": true },
        { "key": "jwtTokenAdminEntidad", "value": "", "enabled": true },
        { "key": "emailAdminEntidadCreado", "value": "", "enabled": true },
        { "key": "idAdminEntidadCreado", "value": "", "enabled": true },
        { "key": "adminLUPassword", "value": "AdminLUPass123!", "enabled": true },
        { "key": "jwtTokenAdminLU", "value": "", "enabled": true },
        { "key": "emailAdminLUCreado", "value": "", "enabled": true },
        { "key": "idAdminLUCreado", "value": "", "enabled": true },
        { "key": "recepcionistaPassword", "value": "RecepcionistaPass123!", "enabled": true },
        { "key": "jwtTokenRecepcionista", "value": "", "enabled": true },
        { "key": "emailRecepcionistaCreado", "value": "", "enabled": true },
        { "key": "idRecepcionistaCreado", "value": "", "enabled": true },
        { "key": "revisorArqPassword", "value": "RevisorArqPass123!", "enabled": true },
        { "key": "jwtTokenRevisorArq", "value": "", "enabled": true },
        { "key": "emailRevisorArqCreado", "value": "", "enabled": true },
        { "key": "idRevisorArqCreado", "value": "", "enabled": true },
        { "key": "revisorJurPassword", "value": "RevisorJurPass123!", "enabled": true },
        { "key": "jwtTokenRevisorJur", "value": "", "enabled": true },
        { "key": "emailRevisorJurCreado", "value": "", "enabled": true },
        { "key": "idRevisorJurCreado", "value": "", "enabled": true },
        { "key": "revisorEstPassword", "value": "RevisorEstPass123!", "enabled": true },
        { "key": "jwtTokenRevisorEst", "value": "", "enabled": true },
        { "key": "emailRevisorEstCreado", "value": "", "enabled": true },
        { "key": "idRevisorEstCreado", "value": "", "enabled": true },
        { "key": "solicitanteRegPassword", "value": "SolicitanteRegPass123!", "enabled": true },
        { "key": "jwtTokenSolicitanteReg", "value": "", "enabled": true },
        { "key": "emailSolicitanteRegCreado", "value": "", "enabled": true },
        { "key": "idSolicitanteRegCreado", "value": "", "enabled": true },
        { "key": "idTramiteRecepcionado", "value": "", "enabled": true },
        { "key": "idTramiteAsignado", "value": "", "enabled": true },
        { "key": "idOtroUsuarioParaDenegar", "value": "", "enabled": true }
    ]
};

// ======================================================
// 2. Definición de la Estructura de la Colección
//    Cada elemento puede ser una carpeta (con 'item' anidado) o una solicitud
// ======================================================
const collectionStructure = {
    info: {
        name: COLLECTION_NAME,
        schema: 'https://schema.getpostman.com/json/collection/v2.1.0/collection.json'
    },
    item: [
        {
            name: "01 - Configuración Inicial",
            item: [
                {
                    name: "01.1 - Login Admin Global",
                    request: {
                        method: "POST",
                        url: "{{baseUrl}}/api/v1/auth/login",
                        header: [{ key: "Content-Type", value: "application/json" }],
                        body: {
                            mode: "raw",
                            raw: JSON.stringify({
                                correoElectronico: "{{adminGlobalUser}}",
                                contrasena: "{{adminGlobalPassword}}"
                            })
                        }
                    }
                },
                {
                    name: "01.2 - Crear Entidad",
                    request: {
                        method: "POST",
                        url: "{{baseUrl}}/api/v1/entidades",
                        header: [
                            { key: "Content-Type", value: "application/json" },
                            { key: "Authorization", value: "Bearer {{jwtTokenAdmin}}" }
                        ],
                        body: {
                            mode: "raw",
                            raw: JSON.stringify({
                                nombre: "Entidad Automatizada {{ $randomInt }}",
                                nit: "90000000{{ $randomInt }}",
                                direccion: "Calle 100 # 20-30",
                                telefono: "6011234567",
                                correoElectronico: "entidad.auto.{{ $randomInt }}@gestion.com",
                                estaActiva: true
                            })
                        }
                    }
                }
            ]
        },
        {
            name: "07 - Pruebas de Seguridad y Roles",
            item: [
                {
                    name: "07.0 - Creación y Login de Roles Jerárquicos",
                    item: [
                        {
                            name: "07.0.1 - Crear Usuario Admin Entidad (por Admin Global)",
                            request: {
                                method: "POST",
                                url: "{{baseUrl}}/api/v1/usuarios",
                                header: [
                                    { key: "Content-Type", value: "application/json" },
                                    { key: "Authorization", value: "Bearer {{jwtTokenAdmin}}" }
                                ],
                                body: {
                                    mode: "raw",
                                    raw: JSON.stringify({
                                        nombreCompleto: "Admin Entidad Prueba {{idEntidadExistente}}",
                                        correoElectronico: "admin.entidad.{{$randomInt}}@gestion.com",
                                        contrasenaHash: "{{adminEntidadPassword}}", // Usamos contrasenaHash
                                        rol: "ADMIN_ENTIDAD",
                                        numeroDocumento: "9000000{{$randomInt}}",
                                        tipoDocumento: "CC",
                                        telefono: "3000000001",
                                        estaActivo: true,
                                        idEntidad: "{{idEntidadExistente}}"
                                    })
                                }
                            }
                        },
                        {
                            name: "07.0.2 - Login Admin Entidad",
                            request: {
                                method: "POST",
                                url: "{{baseUrl}}/api/v1/auth/login",
                                header: [{ key: "Content-Type", value: "application/json" }],
                                body: {
                                    mode: "raw",
                                    raw: JSON.stringify({
                                        correoElectronico: "{{emailAdminEntidadCreado}}",
                                        contrasena: "{{adminEntidadPassword}}"
                                    })
                                }
                            }
                        },
                        {
                            name: "07.0.3 - Crear Usuario Recepción (por Admin Entidad)",
                            request: {
                                method: "POST",
                                url: "{{baseUrl}}/api/v1/usuarios",
                                header: [
                                    { key: "Content-Type", value: "application/json" },
                                    { key: "Authorization", value: "Bearer {{jwtTokenAdminEntidad}}" }
                                ],
                                body: {
                                    mode: "raw",
                                    raw: JSON.stringify({
                                        nombreCompleto: "Recepcionista Prueba",
                                        correoElectronico: "recepcion.{{$randomInt}}@gestion.com",
                                        contrasenaHash: "{{recepcionistaPassword}}",
                                        rol: "RECEPCIONISTA",
                                        numeroDocumento: "6000000{{$randomInt}}",
                                        tipoDocumento: "CC",
                                        telefono: "3000000004",
                                        estaActivo: true,
                                        idEntidad: "{{idEntidadExistente}}"
                                    })
                                }
                            }
                        },
                        {
                            name: "07.0.4 - Login Recepcionista",
                            request: {
                                method: "POST",
                                url: "{{baseUrl}}/api/v1/auth/login",
                                header: [{ key: "Content-Type", value: "application/json" }],
                                body: {
                                    mode: "raw",
                                    raw: JSON.stringify({
                                        correoElectronico: "{{emailRecepcionistaCreado}}",
                                        contrasena: "{{recepcionistaPassword}}"
                                    })
                                }
                            }
                        },
                        {
                            name: "07.0.5 - Crear Usuario Administrador Licencias Urbanísticas (ADMIN_LU) (por Admin Entidad)",
                            request: {
                                method: "POST",
                                url: "{{baseUrl}}/api/v1/usuarios",
                                header: [
                                    { key: "Content-Type", value: "application/json" },
                                    { key: "Authorization", value: "Bearer {{jwtTokenAdminEntidad}}" }
                                ],
                                body: {
                                    mode: "raw",
                                    raw: JSON.stringify({
                                        nombreCompleto: "Admin LU Prueba",
                                        correoElectronico: "admin.lu.{{$randomInt}}@gestion.com",
                                        contrasenaHash: "{{adminLUPassword}}",
                                        rol: "ADMIN_LU",
                                        numeroDocumento: "8000000{{$randomInt}}",
                                        tipoDocumento: "CC",
                                        telefono: "3000000002",
                                        estaActivo: true,
                                        matriculaProfesional: "LU-MP-{{$randomInt}}",
                                        experienciaAcreditada: "15 años de experiencia en licencias urbanísticas",
                                        idEntidad: "{{idEntidadExistente}}"
                                    })
                                }
                            }
                        },
                        {
                            name: "07.0.6 - Login Administrador Licencias Urbanísticas (ADMIN_LU)",
                            request: {
                                method: "POST",
                                url: "{{baseUrl}}/api/v1/auth/login",
                                header: [{ key: "Content-Type", value: "application/json" }],
                                body: {
                                    mode: "raw",
                                    raw: JSON.stringify({
                                        correoElectronico: "{{emailAdminLUCreado}}",
                                        contrasena: "{{adminLUPassword}}"
                                    })
                                }
                            }
                        },
                        {
                            name: "07.0.7 - Crear Revisor Arquitectónico (por Admin LU)",
                            request: {
                                method: "POST",
                                url: "{{baseUrl}}/api/v1/usuarios",
                                header: [
                                    { key: "Content-Type", value: "application/json" },
                                    { key: "Authorization", value: "Bearer {{jwtTokenAdminLU}}" }
                                ],
                                body: {
                                    mode: "raw",
                                    raw: JSON.stringify({
                                        nombreCompleto: "Revisor Arq. Prueba",
                                        correoElectronico: "revisor.arq.{{$randomInt}}@gestion.com",
                                        contrasenaHash: "{{revisorArqPassword}}",
                                        rol: "REVISOR_ARQ",
                                        numeroDocumento: "7000000{{$randomInt}}",
                                        tipoDocumento: "CC",
                                        telefono: "3000000003",
                                        estaActivo: true,
                                        matriculaProfesional: "ARQ-MP-{{$randomInt}}",
                                        experienciaAcreditada: "5 años de experiencia en revisión arquitectónica",
                                        idEntidad: "{{idEntidadExistente}}"
                                    })
                                }
                            }
                        },
                        {
                            name: "07.0.8 - Login Revisor Arquitectónico",
                            request: {
                                method: "POST",
                                url: "{{baseUrl}}/api/v1/auth/login",
                                header: [{ key: "Content-Type", value: "application/json" }],
                                body: {
                                    mode: "raw",
                                    raw: JSON.stringify({
                                        correoElectronico: "{{emailRevisorArqCreado}}",
                                        contrasena: "{{revisorArqPassword}}"
                                    })
                                }
                            }
                        },
                        {
                            name: "07.0.9 - Crear Revisor Jurídico (por Admin LU)",
                            request: {
                                method: "POST",
                                url: "{{baseUrl}}/api/v1/usuarios",
                                header: [
                                    { key: "Content-Type", value: "application/json" },
                                    { key: "Authorization", value: "Bearer {{jwtTokenAdminLU}}" }
                                ],
                                body: {
                                    mode: "raw",
                                    raw: JSON.stringify({
                                        nombreCompleto: "Revisor Jur. Prueba",
                                        correoElectronico: "revisor.jur.{{$randomInt}}@gestion.com",
                                        contrasenaHash: "{{revisorJurPassword}}",
                                        rol: "REVISOR_JUR",
                                        numeroDocumento: "6500000{{$randomInt}}",
                                        tipoDocumento: "CC",
                                        telefono: "3000000005",
                                        estaActivo: true,
                                        matriculaProfesional: "JUR-MP-{{$randomInt}}",
                                        experienciaAcreditada: "8 años de experiencia en normativa urbanística",
                                        idEntidad: "{{idEntidadExistente}}"
                                    })
                                }
                            }
                        },
                        {
                            name: "07.0.10 - Login Revisor Jurídico",
                            request: {
                                method: "POST",
                                url: "{{baseUrl}}/api/v1/auth/login",
                                header: [{ key: "Content-Type", value: "application/json" }],
                                body: {
                                    mode: "raw",
                                    raw: JSON.stringify({
                                        correoElectronico: "{{emailRevisorJurCreado}}",
                                        contrasena: "{{revisorJurPassword}}"
                                    })
                                }
                            }
                        },
                        {
                            name: "07.0.11 - Crear Revisor Estructural (por Admin LU)",
                            request: {
                                method: "POST",
                                url: "{{baseUrl}}/api/v1/usuarios",
                                header: [
                                    { key: "Content-Type", value: "application/json" },
                                    { key: "Authorization", value: "Bearer {{jwtTokenAdminLU}}" }
                                ],
                                body: {
                                    mode: "raw",
                                    raw: JSON.stringify({
                                        nombreCompleto: "Revisor Est. Prueba",
                                        correoElectronico: "revisor.est.{{$randomInt}}@gestion.com",
                                        contrasenaHash: "{{revisorEstPassword}}",
                                        rol: "REVISOR_EST",
                                        numeroDocumento: "6700000{{$randomInt}}",
                                        tipoDocumento: "CC",
                                        telefono: "3000000006",
                                        estaActivo: true,
                                        matriculaProfesional: "EST-MP-{{$randomInt}}",
                                        experienciaAcreditada: "10 años de experiencia en cálculo estructural",
                                        idEntidad: "{{idEntidadExistente}}"
                                    })
                                }
                            }
                        },
                        {
                            name: "07.0.12 - Login Revisor Estructural",
                            request: {
                                method: "POST",
                                url: "{{baseUrl}}/api/v1/auth/login",
                                header: [{ key: "Content-Type", value: "application/json" }],
                                body: {
                                    mode: "raw",
                                    raw: JSON.stringify({
                                        correoElectronico: "{{emailRevisorEstCreado}}",
                                        contrasena: "{{revisorEstPassword}}"
                                    })
                                }
                            }
                        },
                        {
                            name: "07.0.13 - Auto-Registro Solicitante (Endpoint Público)",
                            request: {
                                method: "POST",
                                url: "{{baseUrl}}/api/v1/auth/register", // Asumiendo este endpoint
                                header: [{ key: "Content-Type", value: "application/json" }],
                                body: {
                                    mode: "raw",
                                    raw: JSON.stringify({
                                        nombreCompleto: "Solicitante Auto Registrado",
                                        correoElectronico: "solicitante.reg.{{$randomInt}}@gestion.com",
                                        contrasenaHash: "{{solicitanteRegPassword}}",
                                        rol: "SOLICITANTE", // El rol se asigna automáticamente al registrarse públicamente
                                        numeroDocumento: "1100000{{$randomInt}}",
                                        tipoDocumento: "CC",
                                        telefono: "3009876543",
                                        estaActivo: true,
                                        idEntidad: "{{idEntidadExistente}}"
                                    })
                                }
                            }
                        },
                        {
                            name: "07.0.14 - Login Solicitante (Auto-registrado)",
                            request: {
                                method: "POST",
                                url: "{{baseUrl}}/api/v1/auth/login",
                                header: [{ key: "Content-Type", value: "application/json" }],
                                body: {
                                    mode: "raw",
                                    raw: JSON.stringify({
                                        correoElectronico: "{{emailSolicitanteRegCreado}}",
                                        contrasena: "{{solicitanteRegPassword}}"
                                    })
                                }
                            }
                        }
                    ]
                },
                {
                    name: "07.1 - Pruebas de Permisos Permitidos (Positivos)",
                    item: [
                        {
                            name: "07.1.1 - Recepcionista puede Crear Trámite (Presencial)",
                            request: {
                                method: "POST",
                                url: "{{baseUrl}}/api/v1/tramites",
                                header: [
                                    { key: "Content-Type", value: "application/json" },
                                    { key: "Authorization", value: "Bearer {{jwtTokenRecepcionista}}" }
                                ],
                                body: {
                                    mode: "raw",
                                    // Este body deberá ser completado manualmente en Postman
                                    raw: JSON.stringify({
                                        nombreTramite: "Licencia Urb. de Prueba - Recepción {{ $randomInt }}",
                                        descripcion: "Solicitud de licencia de urbanización para automatización.",
                                        tipoTramite: "LICENCIA_DE_URBANIZACION", // Reemplaza con tus tipos de trámite
                                        modalidadTramite: "Desarrollo", // Reemplaza con tus modalidades
                                        estado: "RADICADO",
                                        idSolicitante: "{{idSolicitanteRegCreado}}",
                                        idEntidad: "{{idEntidadExistente}}"
                                    })
                                }
                            }
                        },
                        {
                            name: "07.1.2 - Admin LU puede Ver Todos los Trámites de su Entidad",
                            request: {
                                method: "GET",
                                url: "{{baseUrl}}/api/v1/tramites?idEntidad={{idEntidadExistente}}",
                                header: [
                                    { key: "Authorization", value: "Bearer {{jwtTokenAdminLU}}" }
                                ]
                            }
                        },
                        {
                            name: "07.1.3 - Solicitante puede Ver SOLO Sus Propios Trámites",
                            request: {
                                method: "GET",
                                url: "{{baseUrl}}/api/v1/tramites?idSolicitante={{idSolicitanteRegCreado}}",
                                header: [
                                    { key: "Authorization", value: "Bearer {{jwtTokenSolicitanteReg}}" }
                                ]
                            }
                        },
                        {
                            name: "07.1.4 - Admin LU puede Asignar Trámite a Revisor",
                            request: {
                                method: "PUT", // O POST, según tu API
                                url: "{{baseUrl}}/api/v1/tramites/{{idTramiteRecepcionado}}/asignar", // Ajusta el endpoint
                                header: [
                                    { key: "Content-Type", value: "application/json" },
                                    { key: "Authorization", value: "Bearer {{jwtTokenAdminLU}}" }
                                ],
                                body: {
                                    mode: "raw",
                                    raw: JSON.stringify({
                                        idTramite: "{{idTramiteRecepcionado}}",
                                        idRevisorAsignado: "{{idRevisorArqCreado}}"
                                    })
                                }
                            }
                        },
                        {
                            name: "07.1.5 - Revisor Arquitectónico puede Actualizar Estado y Comentar Trámite Asignado",
                            request: {
                                method: "PUT",
                                url: "{{baseUrl}}/api/v1/tramites/{{idTramiteRecepcionado}}",
                                header: [
                                    { key: "Content-Type", value: "application/json" },
                                    { key: "Authorization", value: "Bearer {{jwtTokenRevisorArq}}" }
                                ],
                                body: {
                                    mode: "raw",
                                    // Este body es una plantilla, deberá ser completado con los datos completos del trámite
                                    raw: JSON.stringify({
                                        // Aquí deberías poner la representación completa del TrámiteDTO
                                        // incluyendo los IDs de solicitante, entidad, etc.
                                        // Por simplicidad, solo muestro los campos que cambian.
                                        // Tendrás que obtener los datos del GET para hacer un PUT completo
                                        // o si tu API soporta PATCH, usarlo.
                                        estado: "OBSERVACION_ARQ",
                                        comentariosRevisor: "Se requieren ajustes en los planos arquitectónicos."
                                    })
                                }
                            }
                        }
                    ]
                },
                {
                    name: "07.2 - Pruebas de Permisos Denegados (Negativos)",
                    item: [
                        {
                            name: "07.2.1 - Admin Entidad NO puede Crear Entidad",
                            request: {
                                method: "POST",
                                url: "{{baseUrl}}/api/v1/entidades",
                                header: [
                                    { key: "Content-Type", value: "application/json" },
                                    { key: "Authorization", value: "Bearer {{jwtTokenAdminEntidad}}" }
                                ],
                                body: {
                                    mode: "raw",
                                    raw: JSON.stringify({
                                        nombre: "Entidad Ilegal {{ $randomInt }}",
                                        nit: "99999999{{ $randomInt }}",
                                        direccion: "Dirección ilegal",
                                        telefono: "1111111111",
                                        correoElectronico: "ilegal.auto.{{ $randomInt }}@gestion.com",
                                        estaActiva: true
                                    })
                                }
                            }
                        },
                        {
                            name: "07.2.2 - Recepcionista NO puede Eliminar Usuario",
                            request: {
                                method: "DELETE",
                                url: "{{baseUrl}}/api/v1/usuarios/{{idAdminEntidadCreado}}",
                                header: [
                                    { key: "Authorization", value: "Bearer {{jwtTokenRecepcionista}}" }
                                ]
                            }
                        },
                        {
                            name: "07.2.3 - Solicitante NO puede Ver Trámites de Otros Usuarios",
                            request: {
                                method: "GET",
                                url: "{{baseUrl}}/api/v1/tramites?idSolicitante={{idOtroUsuarioParaDenegar}}", // Necesitas un ID de otro usuario
                                header: [
                                    { key: "Authorization", value: "Bearer {{jwtTokenSolicitanteReg}}" }
                                ]
                            }
                        },
                        {
                            name: "07.2.4 - Admin LU NO puede Re-asignar Trámite una vez asignado",
                            request: {
                                method: "PUT", // O POST
                                url: "{{baseUrl}}/api/v1/tramites/{{idTramiteRecepcionado}}/asignar",
                                header: [
                                    { key: "Content-Type", value: "application/json" },
                                    { key: "Authorization", value: "Bearer {{jwtTokenAdminLU}}" }
                                ],
                                body: {
                                    mode: "raw",
                                    raw: JSON.stringify({
                                        idTramite: "{{idTramiteRecepcionado}}",
                                        idRevisorAsignado: "{{idRevisorJurCreado}}" // Intentar re-asignar a otro revisor
                                    })
                                }
                            }
                        },
                        {
                            name: "07.2.5 - Revisor Arquitectónico NO puede Eliminar Trámite",
                            request: {
                                method: "DELETE",
                                url: "{{baseUrl}}/api/v1/tramites/{{idTramiteRecepcionado}}",
                                header: [
                                    { key: "Authorization", value: "Bearer {{jwtTokenRevisorArq}}" }
                                ]
                            }
                        }
                    ]
                }
            ]
        },
        // Puedes añadir más carpetas y solicitudes aquí
        // { name: "08 - Gestión de Trámites LU", item: [...] }
    ]
};

// ======================================================
// 3. Funciones para Interactuar con la Postman API
// ======================================================

async function createOrUpdateEnvironment() {
    try {
        console.log(`Buscando entorno '${ENVIRONMENT_NAME}'...`);
        const environmentsRes = await axios.get(`${BASE_URL_POSTMAN_API}/environments`, {
            headers: { 'X-Api-Key': POSTMAN_API_KEY }
        });
        const existingEnv = environmentsRes.data.environments.find(env => env.name === ENVIRONMENT_NAME);

        if (existingEnv) {
            console.log(`Entorno '${ENVIRONMENT_NAME}' ya existe (ID: ${existingEnv.uid}). Actualizando...`);
            await axios.put(`${BASE_URL_POSTMAN_API}/environments/${existingEnv.uid}`, { environment: environmentData }, {
                headers: { 'X-Api-Key': POSTMAN_API_KEY }
            });
            console.log(`Entorno '${ENVIRONMENT_NAME}' actualizado exitosamente.`);
        } else {
            console.log(`Entorno '${ENVIRONMENT_NAME}' no encontrado. Creando nuevo entorno...`);
            const createEnvRes = await axios.post(`${BASE_URL_POSTMAN_API}/environments`, { environment: environmentData }, {
                headers: { 'X-Api-Key': POSTMAN_API_KEY }
            });
            console.log(`Entorno '${ENVIRONMENT_NAME}' creado exitosamente (ID: ${createEnvRes.data.environment.uid}).`);
        }
    } catch (error) {
        console.error('Error al crear/actualizar el entorno:', error.response?.data || error.message);
        throw error;
    }
}

async function createOrUpdateCollection() {
    try {
        console.log(`Buscando colección '${COLLECTION_NAME}'...`);
        const collectionsRes = await axios.get(`${BASE_URL_POSTMAN_API}/collections`, {
            headers: { 'X-Api-Key': POSTMAN_API_KEY }
        });
        const existingCol = collectionsRes.data.collections.find(col => col.name === COLLECTION_NAME);

        if (existingCol) {
            console.log(`Colección '${COLLECTION_NAME}' ya existe (ID: ${existingCol.uid}). Actualizando...`);
            await axios.put(`${BASE_URL_POSTMAN_API}/collections/${existingCol.uid}`, { collection: collectionStructure }, {
                headers: { 'X-Api-Key': POSTMAN_API_KEY }
            });
            console.log(`Colección '${COLLECTION_NAME}' actualizada exitosamente.`);
        } else {
            console.log(`Colección '${COLLECTION_NAME}' no encontrada. Creando nueva colección...`);
            const createColRes = await axios.post(`${BASE_URL_POSTMAN_API}/collections`, { collection: collectionStructure }, {
                headers: { 'X-Api-Key': POSTMAN_API_KEY }
            });
            console.log(`Colección '${COLLECTION_NAME}' creada exitosamente (ID: ${createColRes.data.collection.uid}).`);
        }
    } catch (error) {
        console.error('Error al crear/actualizar la colección:', error.response?.data || error.message);
        throw error;
    }
}

// ======================================================
// 4. Ejecución Principal
// ======================================================
async function main() {
    try {
        await createOrUpdateEnvironment();
        await createOrUpdateCollection();
        console.log('\n¡Proceso completado exitosamente! Revisa tu Postman.');
        console.log('Recuerda: Deberás añadir manualmente los scripts de Tests y Pre-request más complejos.');
    } catch (error) {
        console.error('\nOcurrió un error en el proceso principal:', error.message);
    }
}

main();