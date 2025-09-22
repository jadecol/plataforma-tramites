package com.gestion.tramites.controller;

import com.gestion.tramites.service.RadicacionService;
import com.gestion.tramites.service.ValidacionRadicacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/radicacion")
@Tag(name = "Radicación", description = "Gestión de radicación de trámites con numeración oficial")
@SecurityRequirement(name = "bearerAuth")
public class RadicacionController {

    @Autowired
    private RadicacionService radicacionService;

    @Autowired
    private ValidacionRadicacionService validacionService;

    @Autowired
    private com.gestion.tramites.repository.EntidadRepository entidadRepository;

    @PostMapping("/radicar")
    @Operation(summary = "Radicar nuevo trámite",
               description = "Radica un nuevo trámite asignando automáticamente el número de radicación oficial")
    @ApiResponse(responseCode = "201", description = "Trámite radicado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos de solicitud inválidos")
    @ApiResponse(responseCode = "403", description = "No autorizado para radicar en esta entidad")
    @PreAuthorize("hasRole('VENTANILLA_UNICA') or hasRole('ADMIN_ENTIDAD') or hasRole('ADMIN_GLOBAL')")
    public ResponseEntity<RadicacionService.SolicitudRadicacion> radicarTramite(
            @Valid @RequestBody RadicacionService.SolicitudRadicacionTramite solicitud) {

        RadicacionService.SolicitudRadicacion radicacion = radicacionService.radicarTramite(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(radicacion);
    }

    @PostMapping("/generar-numero/{entidadId}")
    @Operation(summary = "Generar siguiente número de radicación",
               description = "Genera el siguiente número de radicación para una entidad sin crear el trámite")
    @ApiResponse(responseCode = "200", description = "Número generado exitosamente")
    @ApiResponse(responseCode = "404", description = "Entidad no encontrada")
    @ApiResponse(responseCode = "400", description = "Entidad inactiva o error en generación")
    @PreAuthorize("hasRole('VENTANILLA_UNICA') or hasRole('ADMIN_ENTIDAD') or hasRole('ADMIN_GLOBAL')")
    public ResponseEntity<Map<String, String>> generarNumeroRadicacion(
            @Parameter(description = "ID de la entidad") @PathVariable Long entidadId) {

        String numeroRadicacion = radicacionService.generarSiguienteNumeroRadicacion(entidadId);

        return ResponseEntity.ok(Map.of(
            "numeroRadicacion", numeroRadicacion,
            "entidadId", entidadId.toString(),
            "mensaje", "Número de radicación generado exitosamente"
        ));
    }

    @PostMapping("/validar")
    @Operation(summary = "Validar número de radicación",
               description = "Valida formato, unicidad y secuencia de un número de radicación")
    @ApiResponse(responseCode = "200", description = "Validación completada")
    @ApiResponse(responseCode = "400", description = "Datos de validación inválidos")
    @PreAuthorize("hasRole('VENTANILLA_UNICA') or hasRole('ADMIN_ENTIDAD') or hasRole('ADMIN_GLOBAL')")
    public ResponseEntity<ValidacionRadicacionService.ResultadoValidacionRadicacion> validarRadicacion(
            @RequestBody ValidarRadicacionRequest request) {

        ValidacionRadicacionService.ResultadoValidacionRadicacion resultado =
                validacionService.validarNumeroRadicacion(request.getNumeroRadicacion(), obtenerEntidadPorId(request.getEntidadId()));

        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/validar/lote")
    @Operation(summary = "Validar múltiples números de radicación",
               description = "Valida un lote de números de radicación para una entidad")
    @ApiResponse(responseCode = "200", description = "Validación de lote completada")
    @ApiResponse(responseCode = "400", description = "Datos de validación inválidos")
    @PreAuthorize("hasRole('ADMIN_ENTIDAD') or hasRole('ADMIN_GLOBAL')")
    public ResponseEntity<List<ValidacionRadicacionService.ResultadoValidacionRadicacion>> validarLoteRadicacion(
            @RequestBody ValidarLoteRadicacionRequest request) {

        List<ValidacionRadicacionService.ResultadoValidacionRadicacion> resultados =
                validacionService.validarLoteRadicaciones(request.getNumerosRadicacion(),
                                                         obtenerEntidadPorId(request.getEntidadId()));

        return ResponseEntity.ok(resultados);
    }

    @GetMapping("/estadisticas/{entidadId}")
    @Operation(summary = "Obtener estadísticas de radicación",
               description = "Obtiene estadísticas de radicación para una entidad en un año específico")
    @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente")
    @ApiResponse(responseCode = "404", description = "Entidad no encontrada")
    @PreAuthorize("hasRole('ADMIN_ENTIDAD') or hasRole('REVISOR') or hasRole('ADMIN_GLOBAL')")
    public ResponseEntity<RadicacionService.EstadisticasRadicacion> obtenerEstadisticas(
            @Parameter(description = "ID de la entidad") @PathVariable Long entidadId,
            @Parameter(description = "Año (opcional, por defecto año actual)") @RequestParam(required = false) Integer ano) {

        RadicacionService.EstadisticasRadicacion estadisticas =
                radicacionService.obtenerEstadisticasRadicacion(entidadId, ano);

        return ResponseEntity.ok(estadisticas);
    }

    @GetMapping("/validar/formato/{numeroRadicacion}")
    @Operation(summary = "Validar solo formato de número de radicación",
               description = "Valida únicamente el formato de un número de radicación")
    @ApiResponse(responseCode = "200", description = "Validación de formato completada")
    @PreAuthorize("hasRole('VENTANILLA_UNICA') or hasRole('ADMIN_ENTIDAD') or hasRole('ADMIN_GLOBAL')")
    public ResponseEntity<ValidacionRadicacionService.ResultadoValidacionRadicacion> validarFormato(
            @Parameter(description = "Número de radicación a validar") @PathVariable String numeroRadicacion) {

        ValidacionRadicacionService.ResultadoValidacionRadicacion resultado =
                validacionService.validarFormato(numeroRadicacion);

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/problemas-secuencia/{entidadId}")
    @Operation(summary = "Detectar problemas en secuencia de consecutivos",
               description = "Identifica saltos, duplicados y otros problemas en la secuencia de radicación")
    @ApiResponse(responseCode = "200", description = "Análisis de secuencia completado")
    @ApiResponse(responseCode = "404", description = "Entidad no encontrada")
    @PreAuthorize("hasRole('ADMIN_ENTIDAD') or hasRole('ADMIN_GLOBAL')")
    public ResponseEntity<List<ValidacionRadicacionService.ProblemaConsecutivo>> detectarProblemas(
            @Parameter(description = "ID de la entidad") @PathVariable Long entidadId,
            @Parameter(description = "Año a analizar (opcional, por defecto año actual)") @RequestParam(required = false) Integer ano) {

        if (ano == null) {
            ano = java.time.LocalDate.now().getYear();
        }

        List<ValidacionRadicacionService.ProblemaConsecutivo> problemas =
                validacionService.detectarProblemasSecuencia(obtenerEntidadPorId(entidadId), ano);

        return ResponseEntity.ok(problemas);
    }

    @GetMapping("/reporte-validacion/{entidadId}")
    @Operation(summary = "Generar reporte completo de validación",
               description = "Genera un reporte completo de validación para auditoría")
    @ApiResponse(responseCode = "200", description = "Reporte generado exitosamente")
    @ApiResponse(responseCode = "404", description = "Entidad no encontrada")
    @PreAuthorize("hasRole('ADMIN_ENTIDAD') or hasRole('ADMIN_GLOBAL')")
    public ResponseEntity<ValidacionRadicacionService.ReporteValidacionRadicacion> generarReporte(
            @Parameter(description = "ID de la entidad") @PathVariable Long entidadId,
            @Parameter(description = "Año del reporte (opcional, por defecto año actual)") @RequestParam(required = false) Integer ano) {

        if (ano == null) {
            ano = java.time.LocalDate.now().getYear();
        }

        ValidacionRadicacionService.ReporteValidacionRadicacion reporte =
                validacionService.generarReporteValidacion(obtenerEntidadPorId(entidadId), ano);

        return ResponseEntity.ok(reporte);
    }

    @PostMapping("/reservar/{entidadId}")
    @Operation(summary = "Reservar número de radicación",
               description = "Reserva un número de radicación para uso posterior")
    @ApiResponse(responseCode = "200", description = "Número reservado exitosamente")
    @ApiResponse(responseCode = "404", description = "Entidad no encontrada")
    @ApiResponse(responseCode = "400", description = "Error en reserva")
    @PreAuthorize("hasRole('ADMIN_ENTIDAD') or hasRole('ADMIN_GLOBAL')")
    public ResponseEntity<RadicacionService.ReservaRadicacion> reservarNumero(
            @Parameter(description = "ID de la entidad") @PathVariable Long entidadId,
            @Parameter(description = "Motivo de la reserva") @RequestParam String motivo) {

        RadicacionService.ReservaRadicacion reserva =
                radicacionService.reservarNumeroRadicacion(entidadId, motivo);

        return ResponseEntity.ok(reserva);
    }

    @GetMapping("/tipo-entidad/{entidadId}")
    @Operation(summary = "Determinar tipo de entidad para radicación",
               description = "Determina si una entidad es secretaría o curaduría para efectos de numeración")
    @ApiResponse(responseCode = "200", description = "Tipo determinado exitosamente")
    @ApiResponse(responseCode = "404", description = "Entidad no encontrada")
    @PreAuthorize("hasRole('ADMIN_ENTIDAD') or hasRole('ADMIN_GLOBAL')")
    public ResponseEntity<Map<String, Object>> determinarTipoEntidad(
            @Parameter(description = "ID de la entidad") @PathVariable Long entidadId) {

        com.gestion.tramites.model.Entidad entidad = obtenerEntidadPorId(entidadId);
        com.gestion.tramites.model.ConsecutivoRadicacion.TipoEntidadRadicacion tipo =
                validacionService.determinarTipoEntidad(entidad);

        return ResponseEntity.ok(Map.of(
            "entidadId", entidadId,
            "nombreEntidad", entidad.getNombre(),
            "tipoEntidad", tipo.name(),
            "codigoTipo", tipo.getCodigo(),
            "descripcion", tipo.getDescripcion()
        ));
    }

    @GetMapping("/siguiente-numero/{entidadId}")
    @Operation(summary = "Consultar siguiente número disponible",
               description = "Consulta cuál sería el siguiente número de radicación sin generarlo")
    @ApiResponse(responseCode = "200", description = "Número consultado exitosamente")
    @ApiResponse(responseCode = "404", description = "Entidad no encontrada")
    @PreAuthorize("hasRole('VENTANILLA_UNICA') or hasRole('ADMIN_ENTIDAD') or hasRole('ADMIN_GLOBAL')")
    public ResponseEntity<Map<String, Object>> consultarSiguienteNumero(
            @Parameter(description = "ID de la entidad") @PathVariable Long entidadId) {

        com.gestion.tramites.model.Entidad entidad = obtenerEntidadPorId(entidadId);
        com.gestion.tramites.model.ConsecutivoRadicacion.TipoEntidadRadicacion tipo =
                validacionService.determinarTipoEntidad(entidad);

        // Simular generación sin persistir
        java.time.LocalDate hoy = java.time.LocalDate.now();
        String patron = String.format("%s-%s-%02d-XXXX",
                entidad.getCodigoDane(),
                tipo.getCodigo(),
                hoy.getYear() % 100);

        return ResponseEntity.ok(Map.of(
            "entidadId", entidadId,
            "tipoEntidad", tipo.name(),
            "ano", hoy.getYear(),
            "patronNumero", patron,
            "mensaje", "Patrón del siguiente número (sin generar)"
        ));
    }

    // Método auxiliar para obtener entidad
    private com.gestion.tramites.model.Entidad obtenerEntidadPorId(Long entidadId) {
        return entidadRepository.findById(entidadId)
                .orElseThrow(() -> new com.gestion.tramites.exception.ResourceNotFoundException(
                    "Entidad", "id", entidadId));
    }

    // DTOs para requests
    public static class ValidarRadicacionRequest {
        private String numeroRadicacion;
        private Long entidadId;

        public String getNumeroRadicacion() { return numeroRadicacion; }
        public void setNumeroRadicacion(String numeroRadicacion) { this.numeroRadicacion = numeroRadicacion; }

        public Long getEntidadId() { return entidadId; }
        public void setEntidadId(Long entidadId) { this.entidadId = entidadId; }
    }

    public static class ValidarLoteRadicacionRequest {
        private List<String> numerosRadicacion;
        private Long entidadId;

        public List<String> getNumerosRadicacion() { return numerosRadicacion; }
        public void setNumerosRadicacion(List<String> numerosRadicacion) { this.numerosRadicacion = numerosRadicacion; }

        public Long getEntidadId() { return entidadId; }
        public void setEntidadId(Long entidadId) { this.entidadId = entidadId; }
    }
}