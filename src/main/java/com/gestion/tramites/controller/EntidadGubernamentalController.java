package com.gestion.tramites.controller;

import com.gestion.tramites.model.EntidadGubernamental;
import com.gestion.tramites.service.VerificacionEntidadGubernamentalService;
import com.gestion.tramites.service.ValidacionDominiosGubernamentalesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/entidades-gubernamentales")
@Tag(name = "Entidades Gubernamentales", description = "Gestión de entidades gubernamentales verificadas")
@SecurityRequirement(name = "bearerAuth")
public class EntidadGubernamentalController {

    @Autowired
    private VerificacionEntidadGubernamentalService verificacionService;

    @Autowired
    private ValidacionDominiosGubernamentalesService validacionDominiosService;

    @PostMapping("/registrar")
    @Operation(summary = "Registrar nueva entidad gubernamental",
               description = "Registra una nueva entidad gubernamental e inicia el proceso de verificación")
    @ApiResponse(responseCode = "201", description = "Entidad registrada exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos de registro inválidos")
    @ApiResponse(responseCode = "409", description = "Entidad ya existe")
    @PreAuthorize("hasRole('ADMIN_ENTIDAD') or hasRole('ADMIN_GLOBAL')")
    public ResponseEntity<EntidadGubernamental> registrarEntidad(
            @Valid @RequestBody VerificacionEntidadGubernamentalService.SolicitudRegistroEntidad solicitud) {

        EntidadGubernamental entidad = verificacionService.registrarEntidadGubernamental(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(entidad);
    }

    @PostMapping("/{entidadId}/verificar")
    @Operation(summary = "Verificar entidad gubernamental",
               description = "Verifica o rechaza una entidad gubernamental pendiente")
    @ApiResponse(responseCode = "200", description = "Entidad verificada exitosamente")
    @ApiResponse(responseCode = "404", description = "Entidad no encontrada")
    @ApiResponse(responseCode = "403", description = "No autorizado para verificar")
    @PreAuthorize("hasRole('ADMIN_GLOBAL')")
    public ResponseEntity<EntidadGubernamental> verificarEntidad(
            @Parameter(description = "ID de la entidad") @PathVariable Long entidadId,
            @Valid @RequestBody VerificacionEntidadGubernamentalService.SolicitudVerificacion solicitud) {

        EntidadGubernamental entidad = verificacionService.verificarEntidad(entidadId, solicitud);
        return ResponseEntity.ok(entidad);
    }

    @PostMapping("/{entidadId}/suspender")
    @Operation(summary = "Suspender entidad gubernamental",
               description = "Suspende una entidad gubernamental verificada")
    @ApiResponse(responseCode = "200", description = "Entidad suspendida exitosamente")
    @ApiResponse(responseCode = "404", description = "Entidad no encontrada")
    @ApiResponse(responseCode = "403", description = "No autorizado para suspender")
    @PreAuthorize("hasRole('ADMIN_GLOBAL')")
    public ResponseEntity<EntidadGubernamental> suspenderEntidad(
            @Parameter(description = "ID de la entidad") @PathVariable Long entidadId,
            @Parameter(description = "Motivo de suspensión") @RequestParam String motivo) {

        EntidadGubernamental entidad = verificacionService.suspenderEntidad(entidadId, motivo);
        return ResponseEntity.ok(entidad);
    }

    @GetMapping("/pendientes")
    @Operation(summary = "Obtener entidades pendientes de verificación",
               description = "Lista todas las entidades pendientes de verificación")
    @ApiResponse(responseCode = "200", description = "Lista de entidades pendientes")
    @PreAuthorize("hasRole('ADMIN_GLOBAL')")
    public ResponseEntity<List<EntidadGubernamental>> obtenerEntidadesPendientes() {
        List<EntidadGubernamental> entidades = verificacionService.obtenerEntidadesPendientesVerificacion();
        return ResponseEntity.ok(entidades);
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Obtener entidades por estado",
               description = "Lista entidades filtradas por estado de verificación")
    @ApiResponse(responseCode = "200", description = "Lista de entidades filtradas")
    @PreAuthorize("hasRole('ADMIN_GLOBAL') or hasRole('ADMIN_ENTIDAD')")
    public ResponseEntity<List<EntidadGubernamental>> obtenerEntidadesPorEstado(
            @Parameter(description = "Estado de verificación") @PathVariable EntidadGubernamental.EstadoVerificacion estado) {

        List<EntidadGubernamental> entidades = verificacionService.obtenerEntidadesPorEstado(estado);
        return ResponseEntity.ok(entidades);
    }

    @GetMapping("/buscar/dominio/{dominio}")
    @Operation(summary = "Buscar entidad por dominio",
               description = "Busca una entidad gubernamental por su dominio oficial")
    @ApiResponse(responseCode = "200", description = "Entidad encontrada")
    @ApiResponse(responseCode = "404", description = "Entidad no encontrada")
    @PreAuthorize("hasRole('ADMIN_GLOBAL') or hasRole('ADMIN_ENTIDAD')")
    public ResponseEntity<EntidadGubernamental> buscarPorDominio(
            @Parameter(description = "Dominio oficial") @PathVariable String dominio) {

        return verificacionService.buscarPorDominioOficial(dominio)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar/dane/{codigoDane}")
    @Operation(summary = "Buscar entidad por código DANE",
               description = "Busca una entidad gubernamental por su código DANE")
    @ApiResponse(responseCode = "200", description = "Entidad encontrada")
    @ApiResponse(responseCode = "404", description = "Entidad no encontrada")
    @PreAuthorize("hasRole('ADMIN_GLOBAL') or hasRole('ADMIN_ENTIDAD')")
    public ResponseEntity<EntidadGubernamental> buscarPorCodigoDane(
            @Parameter(description = "Código DANE") @PathVariable String codigoDane) {

        return verificacionService.buscarPorCodigoDane(codigoDane)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/validar-dominio")
    @Operation(summary = "Validar dominio gubernamental",
               description = "Valida si un dominio pertenece a una entidad gubernamental legítima")
    @ApiResponse(responseCode = "200", description = "Resultado de validación")
    @PreAuthorize("hasRole('ADMIN_GLOBAL') or hasRole('ADMIN_ENTIDAD')")
    public ResponseEntity<ValidacionDominiosGubernamentalesService.ResultadoValidacionDominio> validarDominio(
            @RequestBody Map<String, String> request) {

        String dominio = request.get("dominio");
        String tipoEntidad = request.get("tipoEntidad");

        ValidacionDominiosGubernamentalesService.ResultadoValidacionDominio resultado =
                validacionDominiosService.validarDominioGubernamental(dominio, tipoEntidad);

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/dominios-oficiales")
    @Operation(summary = "Obtener dominios oficiales verificados",
               description = "Lista todos los dominios oficiales verificados en el sistema")
    @ApiResponse(responseCode = "200", description = "Lista de dominios oficiales")
    @PreAuthorize("hasRole('ADMIN_GLOBAL') or hasRole('ADMIN_ENTIDAD')")
    public ResponseEntity<Set<String>> obtenerDominiosOficiales() {
        Set<String> dominios = validacionDominiosService.obtenerDominiosOficialesVerificados();
        return ResponseEntity.ok(dominios);
    }

    @PostMapping("/{entidadId}/validar-operacion")
    @Operation(summary = "Validar entidad para operación crítica",
               description = "Valida que una entidad esté verificada y activa para operaciones críticas")
    @ApiResponse(responseCode = "200", description = "Entidad válida para operación")
    @ApiResponse(responseCode = "400", description = "Entidad no válida")
    @ApiResponse(responseCode = "404", description = "Entidad no encontrada")
    @PreAuthorize("hasRole('ADMIN_GLOBAL') or hasRole('ADMIN_ENTIDAD') or hasRole('REVISOR')")
    public ResponseEntity<Map<String, String>> validarParaOperacionCritica(
            @Parameter(description = "ID de la entidad") @PathVariable Long entidadId) {

        try {
            verificacionService.validarEntidadParaOperacionCritica(entidadId);
            return ResponseEntity.ok(Map.of("status", "valid", "message", "Entidad válida para operación crítica"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "invalid", "message", e.getMessage()));
        }
    }

    @GetMapping("/tipos-entidad")
    @Operation(summary = "Obtener tipos de entidad disponibles",
               description = "Lista todos los tipos de entidad gubernamental disponibles")
    @ApiResponse(responseCode = "200", description = "Lista de tipos de entidad")
    public ResponseEntity<EntidadGubernamental.TipoEntidadGubernamental[]> obtenerTiposEntidad() {
        return ResponseEntity.ok(EntidadGubernamental.TipoEntidadGubernamental.values());
    }

    @GetMapping("/estados-verificacion")
    @Operation(summary = "Obtener estados de verificación disponibles",
               description = "Lista todos los estados de verificación disponibles")
    @ApiResponse(responseCode = "200", description = "Lista de estados de verificación")
    public ResponseEntity<EntidadGubernamental.EstadoVerificacion[]> obtenerEstadosVerificacion() {
        return ResponseEntity.ok(EntidadGubernamental.EstadoVerificacion.values());
    }
}