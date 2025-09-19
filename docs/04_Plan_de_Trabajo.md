# Plan de Trabajo para el Desarrollo

*Esta propuesta fue generada por IA (Gemini) el 17 de septiembre de 2025 y servirá como hoja de ruta inicial.*

### Fase 1: Estabilización, Seguridad y Refactorización Crítica
* **Seguridad:** Externalizar el secreto de JWT de `application.properties`.
* **Base de Datos:** Integrar Flyway o Liquibase para automatizar las migraciones.
* **Revisión Multi-Tenant:** Auditar a fondo el `EntityInterceptor` y la lógica relacionada.
* **Análisis de Dependencias:** Revisar y actualizar las dependencias en `pom.xml`.

### Fase 2: Estrategia de Pruebas
* **Pruebas Unitarias:** Iniciar con la capa de `Service`, mockeando repositorios.
* **Pruebas de Integración:** Crear pruebas para los `Controller` y la lógica multi-tenant.
* **Cobertura de Código:** Integrar JaCoCo para medir la cobertura de pruebas.

### Fase 3: Nuevas Funcionalidades
* *(Esta sección se llenará con los requerimientos específicos que definamos juntos).*

### Fase 4: Optimización y Preparación para Producción
* **Análisis de Rendimiento:** Detectar y corregir consultas ineficientes o problemas N+1.
* **CI/CD:** Crear un pipeline de Integración/Despliegue Continuo (ej. GitHub Actions).
* **Contenerización:** Escribir un `Dockerfile` para empaquetar la aplicación.