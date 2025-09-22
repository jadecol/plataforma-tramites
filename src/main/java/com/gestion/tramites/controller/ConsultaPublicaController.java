package com.gestion.tramites.controller;

import com.gestion.tramites.service.SeguimientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador público para consulta de trámites sin autenticación
 * Portal Ciudadano - Acceso público para consultar estado de trámites
 */
@RestController
@RequestMapping("/api/public/consulta")
@Tag(name = "Portal Ciudadano", description = "Consultas públicas de trámites sin autenticación")
@Validated
public class ConsultaPublicaController {

    private static final Logger logger = LoggerFactory.getLogger(ConsultaPublicaController.class);

    @Autowired
    private SeguimientoService seguimientoService;

    @GetMapping("/tramite/{numeroRadicacion}")
    @Operation(summary = "Consultar trámite por número de radicación",
               description = "Permite a cualquier ciudadano consultar el estado de un trámite usando el número de radicación. No requiere autenticación.")
    @ApiResponse(responseCode = "200", description = "Trámite encontrado")
    @ApiResponse(responseCode = "404", description = "Trámite no encontrado")
    @ApiResponse(responseCode = "400", description = "Número de radicación inválido")
    public ResponseEntity<SeguimientoService.ConsultaTramitePublico> consultarPorNumeroRadicacion(
            @Parameter(description = "Número de radicación del trámite (formato: DANE-TIPO-YY-NNNN)",
                      example = "11001-0-25-0001")
            @PathVariable
            @NotBlank(message = "El número de radicación es obligatorio")
            @Size(min = 10, max = 20, message = "El número de radicación debe tener entre 10 y 20 caracteres")
            String numeroRadicacion) {

        logger.info("Consulta pública de trámite: {}", numeroRadicacion);

        SeguimientoService.ConsultaTramitePublico tramite =
            seguimientoService.consultarPorNumeroRadicacion(numeroRadicacion);

        return ResponseEntity.ok(tramite);
    }

    @GetMapping("/tramites/email/{emailSolicitante}")
    @Operation(summary = "Consultar trámites por email del solicitante",
               description = "Permite consultar todos los trámites asociados a un email de solicitante. No requiere autenticación.")
    @ApiResponse(responseCode = "200", description = "Lista de trámites (puede estar vacía)")
    @ApiResponse(responseCode = "400", description = "Email inválido")
    public ResponseEntity<List<SeguimientoService.ConsultaTramitePublico>> consultarPorEmail(
            @Parameter(description = "Email del solicitante", example = "ciudadano@ejemplo.com")
            @PathVariable
            @NotBlank(message = "El email es obligatorio")
            @Email(message = "Debe ser un email válido")
            String emailSolicitante) {

        logger.info("Consulta pública por email: {}", emailSolicitante);

        List<SeguimientoService.ConsultaTramitePublico> tramites =
            seguimientoService.consultarPorEmailSolicitante(emailSolicitante);

        return ResponseEntity.ok(tramites);
    }

    @PostMapping("/tramite/validar-acceso")
    @Operation(summary = "Validar acceso a un trámite específico",
               description = "Valida que un email específico tenga acceso a consultar un trámite. Útil para verificaciones adicionales.")
    @ApiResponse(responseCode = "200", description = "Validación completada")
    @ApiResponse(responseCode = "400", description = "Datos de validación inválidos")
    public ResponseEntity<Map<String, Object>> validarAccesoTramite(
            @RequestBody ValidarAccesoRequest request) {

        logger.debug("Validando acceso para radicación: {} y email: {}",
                    request.getNumeroRadicacion(), request.getEmailSolicitante());

        boolean tieneAcceso = seguimientoService.validarAccesoPublico(
            request.getNumeroRadicacion(),
            request.getEmailSolicitante()
        );

        return ResponseEntity.ok(Map.of(
            "tieneAcceso", tieneAcceso,
            "numeroRadicacion", request.getNumeroRadicacion(),
            "mensaje", tieneAcceso ?
                "Acceso autorizado al trámite" :
                "No se encontró trámite asociado al email proporcionado"
        ));
    }

    @GetMapping("/entidad/{entidadId}/tramites-recientes")
    @Operation(summary = "Consultar trámites recientes de una entidad",
               description = "Muestra un resumen de los trámites recientes de una entidad para transparencia pública.")
    @ApiResponse(responseCode = "200", description = "Lista de trámites recientes")
    @ApiResponse(responseCode = "400", description = "ID de entidad inválido")
    public ResponseEntity<List<SeguimientoService.ResumenTramitePublico>> consultarTramitesRecientes(
            @Parameter(description = "ID de la entidad")
            @PathVariable Long entidadId,
            @Parameter(description = "Número máximo de trámites a mostrar (1-20)", example = "10")
            @RequestParam(defaultValue = "10") int limite) {

        logger.debug("Consultando trámites recientes para entidad: {}, límite: {}", entidadId, limite);

        // Validar límite
        if (limite < 1 || limite > 20) {
            limite = 10;
        }

        List<SeguimientoService.ResumenTramitePublico> tramitesRecientes =
            seguimientoService.consultarTramitesRecientesPorEntidad(entidadId, limite);

        return ResponseEntity.ok(tramitesRecientes);
    }

    @GetMapping("/entidad/{entidadId}/estadisticas")
    @Operation(summary = "Obtener estadísticas públicas de una entidad",
               description = "Proporciona estadísticas generales de una entidad para transparencia pública.")
    @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas")
    @ApiResponse(responseCode = "400", description = "ID de entidad inválido")
    public ResponseEntity<SeguimientoService.EstadisticasPublicasEntidad> obtenerEstadisticasPublicas(
            @Parameter(description = "ID de la entidad")
            @PathVariable Long entidadId) {

        logger.debug("Obteniendo estadísticas públicas para entidad: {}", entidadId);

        SeguimientoService.EstadisticasPublicasEntidad estadisticas =
            seguimientoService.obtenerEstadisticasPublicas(entidadId);

        return ResponseEntity.ok(estadisticas);
    }

    @GetMapping("/estados-tramite")
    @Operation(summary = "Obtener descripción de estados de trámites",
               description = "Proporciona información sobre los diferentes estados que puede tener un trámite.")
    @ApiResponse(responseCode = "200", description = "Descripción de estados")
    public ResponseEntity<Map<String, String>> obtenerDescripcionEstados() {

        Map<String, String> estados = Map.of(
            "RADICADO", "Su trámite ha sido recibido y está en cola para revisión",
            "EN_REVISION", "Su trámite está siendo revisado por nuestro equipo técnico",
            "OBSERVADO", "Su trámite requiere información adicional o correcciones",
            "APROBADO", "¡Felicitaciones! Su trámite ha sido aprobado",
            "RECHAZADO", "Su trámite no pudo ser aprobado. Consulte las observaciones",
            "ARCHIVADO", "Su trámite ha sido archivado"
        );

        return ResponseEntity.ok(estados);
    }

    @GetMapping("/ayuda/formatos-radicacion")
    @Operation(summary = "Obtener información sobre formatos de números de radicación",
               description = "Proporciona ejemplos y explicaciones sobre los formatos de números de radicación.")
    @ApiResponse(responseCode = "200", description = "Información de formatos")
    public ResponseEntity<Map<String, Object>> obtenerFormatosRadicacion() {

        Map<String, Object> formatos = Map.of(
            "secretarias", Map.of(
                "formato", "DANE-0-YY-NNNN",
                "ejemplo", "11001-0-25-0001",
                "descripcion", "Código DANE del municipio, seguido de 0 para secretarías, año de 2 dígitos y número consecutivo"
            ),
            "curadurias", Map.of(
                "formato", "DANE-CUR-YY-NNNN",
                "ejemplo", "11001-CUR-25-0001",
                "descripcion", "Código DANE del municipio, seguido de CUR para curadurías, año de 2 dígitos y número consecutivo"
            ),
            "instrucciones", List.of(
                "El código DANE corresponde al municipio donde opera la entidad",
                "El año corresponde a los dos últimos dígitos del año de radicación",
                "El número consecutivo es asignado automáticamente y es único por entidad y año"
            )
        );

        return ResponseEntity.ok(formatos);
    }

    @GetMapping("/salud")
    @Operation(summary = "Verificar estado del servicio de consulta pública",
               description = "Endpoint de salud para verificar que el servicio de consulta pública está funcionando.")
    @ApiResponse(responseCode = "200", description = "Servicio funcionando correctamente")
    public ResponseEntity<Map<String, Object>> verificarSalud() {

        Map<String, Object> salud = Map.of(
            "servicio", "Portal Ciudadano",
            "estado", "ACTIVO",
            "version", "1.0",
            "timestamp", java.time.LocalDateTime.now(),
            "descripcion", "Servicio de consulta pública de trámites funcionando correctamente"
        );

        return ResponseEntity.ok(salud);
    }

    // DTOs para requests

    public static class ValidarAccesoRequest {
        @NotBlank(message = "El número de radicación es obligatorio")
        private String numeroRadicacion;

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Debe ser un email válido")
        private String emailSolicitante;

        // Getters y Setters
        public String getNumeroRadicacion() { return numeroRadicacion; }
        public void setNumeroRadicacion(String numeroRadicacion) { this.numeroRadicacion = numeroRadicacion; }

        public String getEmailSolicitante() { return emailSolicitante; }
        public void setEmailSolicitante(String emailSolicitante) { this.emailSolicitante = emailSolicitante; }
    }

    // Manejo de excepciones específico para consultas públicas
    @ExceptionHandler(com.gestion.tramites.exception.ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> manejarTramiteNoEncontrado(
            com.gestion.tramites.exception.ResourceNotFoundException ex) {

        logger.warn("Trámite no encontrado en consulta pública: {}", ex.getMessage());

        return ResponseEntity.status(404).body(Map.of(
            "error", "TRAMITE_NO_ENCONTRADO",
            "mensaje", "No se encontró un trámite con el número de radicación proporcionado",
            "sugerencia", "Verifique que el número de radicación esté correcto y sea válido"
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarArgumentoInvalido(IllegalArgumentException ex) {

        logger.warn("Argumento inválido en consulta pública: {}", ex.getMessage());

        return ResponseEntity.status(400).body(Map.of(
            "error", "DATOS_INVALIDOS",
            "mensaje", ex.getMessage(),
            "sugerencia", "Verifique los datos proporcionados y intente nuevamente"
        ));
    }
}