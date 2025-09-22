package com.gestion.tramites.controller;

import com.gestion.tramites.dto.reportes.DashboardMetricasDTO;
import com.gestion.tramites.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "Dashboard de métricas y reportes para gestión de trámites")
@SecurityRequirement(name = "Bearer Authentication")
public class ReporteController {

    private final ReporteService reporteService;

    @Autowired
    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @Operation(
            summary = "Dashboard de métricas principales",
            description = "Obtiene métricas agregadas del sistema de trámites filtradas por la entidad del usuario autenticado. " +
                         "Incluye totales, distribución por estados, tiempo promedio de procesamiento, carga de trabajo y tasas de aprobación."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Métricas obtenidas exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DashboardMetricasDTO.class),
                            examples = @ExampleObject(
                                    name = "Dashboard ejemplo",
                                    value = """
                                            {
                                              "totalTramites": 142,
                                              "tramitesUltimoMes": 23,
                                              "tramitesHoy": 3,
                                              "distribucionPorEstado": {
                                                "RADICADO": 15,
                                                "EN_REVISION": 28,
                                                "APROBADO": 45,
                                                "RECHAZADO": 12,
                                                "PENDIENTE_DOCUMENTOS": 8
                                              },
                                              "tiempoPromedioProcesamientoDias": 12.5,
                                              "tramitesVencidos": 5,
                                              "cargaPorRevisor": {
                                                "Ana García López": 12,
                                                "Carlos Martínez Ruiz": 8,
                                                "María Elena Torres": 15
                                              },
                                              "tramitesPorTipo": {
                                                "Licencia de Construcción": 67,
                                                "Permiso de Uso del Suelo": 35,
                                                "Certificado de Habitabilidad": 40
                                              },
                                              "tasaAprobacionUltimoMes": 78.5,
                                              "fechaGeneracion": "2024-01-15T14:30:00",
                                              "nombreEntidad": "Alcaldía de Bogotá"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token JWT inválido o expirado",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuario sin permisos para acceder a reportes",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error permisos",
                                    value = """
                                            {
                                              "timestamp": "2024-01-15T14:30:00",
                                              "status": 403,
                                              "error": "Forbidden",
                                              "message": "Solo administradores y revisores pueden acceder a reportes"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN_GLOBAL') or hasRole('ADMIN_ENTIDAD') or hasRole('REVISOR')")
    public ResponseEntity<DashboardMetricasDTO> getDashboardMetricas() {
        DashboardMetricasDTO metricas = reporteService.generarDashboardMetricas();
        return ResponseEntity.ok(metricas);
    }
}