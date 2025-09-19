# Arquitectura y Stack Tecnológico

*Esta documentación fue generada a partir de un análisis de IA (Gemini) el 17 de septiembre de 2025.*

## Stack Tecnológico
* **Lenguaje:** Java (versión a confirmar en `pom.xml`, probablemente 11, 17 o superior).
* **Framework Principal:** Spring Boot.
* **Acceso a Datos:** Spring Data JPA con Hibernate como proveedor.
* **Base de Datos:** Compatible con la mayoría de bases de datos SQL como PostgreSQL o MySQL.
* **Seguridad:** Spring Security, con autenticación basada en JSON Web Tokens (JWT).
* **Build Tool:** Apache Maven.
* **Librerías Notables:**
    * **ModelMapper:** Para la conversión entre entidades JPA (model) y DTOs.
    * **jjwt:** Para la creación y validación de JWT.
* **Frontend:** No existe en este repositorio (es una API REST).

## Arquitectura
* **Estilo Arquitectónico:** Monolito con Arquitectura en Capas (Layered Architecture).
    * `controller`: Expone los endpoints de la API REST.
    * `service`: Orquesta la lógica de negocio.
    * `repository`: Define la interfaz de acceso a datos.
    * `model`: Representa las entidades de la base de datos.
    * `dto`: Objeto para transferencia de datos.
* **Patrones de Diseño:**
    * Inyección de Dependencias
    * Data Transfer Object (DTO)
    * Repositorio
    * Interceptor (clave para la lógica multi-tenant en `EntityInterceptor.java`).

## Estructura de Archivos
La estructura es lógica, escalable y sigue las convenciones estándar de un proyecto Spring Boot. La separación por funcionalidad es clara. La gestión de migraciones SQL parece manual.