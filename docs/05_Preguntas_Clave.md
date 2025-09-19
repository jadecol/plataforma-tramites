# Preguntas Clave para Aclarar

*Listado de preguntas generadas por IA (Gemini) para poder avanzar con el desarrollo.*

1.  **Lógica Multi-Tenant:** ¿Cuál es la regla de negocio exacta para el aislamiento de datos? ¿Se asigna un usuario a una única `Entidad`? ¿Hay usuarios que puedan ver datos de varias `Entidades`?
Regla de negocio para aislamiento:

Separación total entre entidades: Los trámites de una entidad son completamente invisibles para otras entidades
Asignación única: Cada usuario pertenece a una única entidad (secretaría o curaduría)
Excepción de consulta pública: Cualquier ciudadano puede consultar el estado de un trámite de forma gratuita sin registro (solo con email o número de radicación)

Jerarquía de usuarios confirmada:
ADMIN_GLOBAL (Plataforma SaaS)
├── ADMIN_ENTIDAD (Ingeniero Sistemas por entidad)
    ├── VENTANILLA_UNICA (Recepción)
    ├── REVISOR_OTRAS_ACTUACIONES
    ├── REVISOR_OTRAS_SOLICITUDES
    └── ADMIN_LICENCIAS (Curador/Secretario)
        ├── REVISOR_ARQUITECTONICO
        ├── REVISOR_JURIDICO
        ├── REVISOR_ESTRUCTURAL
        └── OTROS_REVISORES
SOLICITANTE (Titular del trámite)

2.  **Base de Datos:** ¿Cómo se están gestionando y aplicando las migraciones SQL actualmente en los diferentes entornos (desarrollo, producción)? ¿Qué motor de base de datos se está utilizando (PostgreSQL, MySQL, etc.)?
Arquitectura recomendada: Base de datos compartida con particionamiento híbrido

Motor: PostgreSQL (por volumen y capacidades avanzadas)
Estrategia: Tablas compartidas con tenant_id + particionamiento por entidad para tablas de alto volumen
Justificación: 1M+ documentos anuales × 1000+ entidades = necesidad de optimización seria
Migraciones: Flyway para control de versiones SQL en múltiples entornos

Estructura sugerida:
-- Tablas compartidas: usuarios, entidades, configuraciones
-- Particionadas: tramites, documentos, notificaciones, seguimientos
3.  **Frontend:** ¿Existe ya una aplicación frontend o se planea desarrollar una? Conocerla ayudaría a entender mejor cómo se consume la API.
Recomendación: Aplicación web React + TypeScript

Portal público: Consulta de estado de trámites (sin autenticación)
Panel administrativo: Por cada tipo de rol
Interface responsiva: Para uso en oficinas y móvil
PWA: Para funcionamiento offline básico
4.  **Flujo Crítico:** ¿Cuál es el flujo de negocio o el trámite más importante que debe funcionar sin fallos? Conocer la prioridad ayudará a enfocar las primeras pruebas.

Existen tres tramites principales que atendera la plataforma asi:

# Jerarquía de Trámites Urbanísticos

Este documento describe la estructura y clasificación de los trámites manejados por la plataforma.

## 1. Licencias Urbanísticas

Este es el trámite principal y se divide en varios tipos y modalidades.

* **LICENCIA DE URBANIZACIÓN**
    * **Modalidades:**
        * Desarrollo
        * Saneamiento
        * Reurbanización

* **LICENCIA DE PARCELACIÓN**
    * *(Sin modalidades específicas)*

* **LICENCIA DE SUBDIVISIÓN**
    * **Modalidades:**
        * Subdivisión rural
        * Subdivisión urbana
        * Reloteo

* **LICENCIA DE CONSTRUCCIÓN**
    * **Modalidades:**
        * Obra nueva
        * Ampliación
        * Adecuación
        * Modificación
        * Restauración
        * Reforzamiento estructural
        * Demolición
            * Parcial
            * Total
        * Reconstrucción
        * Cerramiento

* **INTERVENCIÓN Y OCUPACIÓN DEL ESPACIO PÚBLICO**
    * *(Sin modalidades específicas)*

* **RECONOCIMIENTO DE LA EXISTENCIA DE UNA EDIFICACIÓN**
    * *(Sin modalidades específicas)*

* **OTRAS ACTUACIONES**
    * *(Sin modalidades específicas)*


## 2. Otras Actuaciones (Relacionadas con Licencias)

Estos son trámites complementarios o derivados del proceso de licenciamiento urbanístico.

* **Concepto de norma**
* **Movimiento de tierras** *(Nota: Solo aplica a predios urbanos. Si es rural, debe estar asociado a una licencia urbanística).*
* **Planos de propiedad horizontal**
* **Aprobación de piscinas**
* **Ajuste de cotas**
* **Copia de planos**


## 3. Otras Solicitudes

Estos son trámites y certificaciones diversas que puede expedir la entidad.

* **Nomenclaturas**
* **Uso de suelo**
* **Certificación de riesgo**
* **Estratificación**
* *(Nota: Se agregarán más solicitudes a futuro).*

El más importante: Radicación de licencias urbanísticas

Formato de radicación obligatorio:

Secretarías: COD_DANE-0-YY-NNNN (ej: 11001-0-25-0001)
Curadurías: COD_DANE-CUR-YY-NNNN (ej: 11001-1-25-0001)

Sin fallos permitidos: Pérdida de numeración consecutiva o duplicados
Flujo crítico: Recepción → Revisión técnica → Aprobación/Negación → Notificación

5.  **Entorno de Despliegue:** ¿Dónde se está desplegando o se planea desplegar esta aplicación (ej. un servidor propio, AWS, Azure)?
Recomendación AWS:
Producción:
├── RDS PostgreSQL (Multi-AZ)
├── ElastiCache Redis (sesiones/caché)
├── S3 + CloudFront (documentos)
├── ECS/Fargate (aplicación)
├── Application Load Balancer
└── Route 53 (DNS)

Desarrollo:
├── RDS PostgreSQL (instancia menor)
├── EC2 para testing
└── S3 bucket de desarrollo

6. Integración DANE:
Datos requeridos:

Municipios: Código DANE, nombre, departamento
Uso del suelo: Clasificación urbanística
Áreas: Ampliada, construida, lote
Estratificación: Para cálculo de costos


MVP Confirmado (5 funcionalidades):

✅ Gestión de entidades y suscripciones
✅ Sistema de usuarios jerárquico
✅ Radicación de licencias urbanísticas (solo construcción-obra nueva)
✅ Seguimiento básico de estados
✅ Dashboard básico por rol