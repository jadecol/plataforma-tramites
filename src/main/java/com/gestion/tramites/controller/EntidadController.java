package com.gestion.tramites.controller;

import com.gestion.tramites.model.Entidad; // Sigue siendo necesario para la clase
import com.gestion.tramites.service.EntidadService;
import com.gestion.tramites.exception.ResourceNotFoundException;
import com.gestion.tramites.dto.EntidadDTO; // <-- Importa el EntidadDTO
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/entidades")
@Tag(name = "Entidades", description = "Gestión de entidades municipales (multi-tenant)")
@SecurityRequirement(name = "Bearer Authentication")
public class EntidadController {

    private final EntidadService entidadService;

    @Autowired
    public EntidadController(EntidadService entidadService) {
        this.entidadService = entidadService;
    }

    @Operation(
            summary = "Crear nueva entidad",
            description = "Registra una nueva entidad municipal en el sistema. Requiere permisos de ADMIN_GLOBAL."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Entidad creada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EntidadDTO.class),
                            examples = @ExampleObject(
                                    name = "Entidad creada",
                                    value = """
                                            {
                                              "id": 1,
                                              "nombre": "Alcaldía de Bogotá",
                                              "nit": "899999999-9",
                                              "direccion": "Carrera 8 No. 10-65",
                                              "telefono": "3553000",
                                              "email": "contacto@bogota.gov.co",
                                              "sitioWeb": "www.bogota.gov.co",
                                              "activo": true
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido"),
            @ApiResponse(responseCode = "403", description = "Sin permisos ADMIN_GLOBAL"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PreAuthorize("hasRole('ADMIN_GLOBAL')")
    @PostMapping
    public ResponseEntity<EntidadDTO> crearEntidad(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de la nueva entidad",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EntidadDTO.class),
                            examples = @ExampleObject(
                                    name = "Nueva entidad",
                                    value = """
                                            {
                                              "nombre": "Alcaldía de Medellín",
                                              "nit": "890905211-1",
                                              "direccion": "Calle 44 No. 52-165",
                                              "telefono": "3855555",
                                              "email": "contacto@medellin.gov.co",
                                              "sitioWeb": "www.medellin.gov.co",
                                              "activo": true
                                            }
                                            """
                            )
                    )
            )
            @RequestBody EntidadDTO entidadDto) {
        EntidadDTO nuevaEntidad = entidadService.crearEntidad(entidadDto);
        return new ResponseEntity<>(nuevaEntidad, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Listar todas las entidades",
            description = "Obtiene la lista completa de entidades municipales. Requiere permisos de ADMIN_GLOBAL o ADMIN_ENTIDAD."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de entidades obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EntidadDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido"),
            @ApiResponse(responseCode = "403", description = "Sin permisos administrativos")
    })
    @PreAuthorize("hasAnyRole('ADMIN_GLOBAL', 'ADMIN_ENTIDAD')")
    @GetMapping
    public ResponseEntity<List<EntidadDTO>> obtenerTodasLasEntidades() {
        List<EntidadDTO> entidades = entidadService.obtenerTodasLasEntidades();
        return new ResponseEntity<>(entidades, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN_GLOBAL', 'ADMIN_ENTIDAD')")
    @GetMapping("/{id}")
    public ResponseEntity<EntidadDTO> obtenerEntidadPorId(@PathVariable Long id) { // Devuelve DTO
        try {
            return entidadService.obtenerEntidadPorId(id)
                    .map(entidadDto -> new ResponseEntity<>(entidadDto, HttpStatus.OK)) // Mapea a
                                                                                        // DTO
                    .orElseThrow(() -> new ResourceNotFoundException("Entidad", "id", id));
        } catch (ResourceNotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PreAuthorize("hasRole('ADMIN_GLOBAL')")
    @PutMapping("/{id}")
    public ResponseEntity<EntidadDTO> actualizarEntidad(@PathVariable Long id,
            @RequestBody EntidadDTO entidadDto) { // Recibe y devuelve DTO
        try {
            EntidadDTO entidadActualizada = entidadService.actualizarEntidad(id, entidadDto); // Servicio
                                                                                              // devuelve
                                                                                              // DTO
            return new ResponseEntity<>(entidadActualizada, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(
            summary = "Cambiar estado de entidad",
            description = "Activa o desactiva una entidad municipal. Requiere permisos de ADMIN_GLOBAL."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado de entidad actualizado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EntidadDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Entidad no encontrada"),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido"),
            @ApiResponse(responseCode = "403", description = "Sin permisos ADMIN_GLOBAL")
    })
    @PreAuthorize("hasRole('ADMIN_GLOBAL')")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<EntidadDTO> cambiarEstadoEntidad(
            @Parameter(description = "ID de la entidad", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nuevo estado activo/inactivo", required = true, example = "true")
            @RequestParam boolean activo) {
        try {
            EntidadDTO entidadActualizada = entidadService.cambiarEstadoEntidad(id, activo);
            return new ResponseEntity<>(entidadActualizada, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PreAuthorize("hasRole('ADMIN_GLOBAL')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEntidad(@PathVariable Long id) {
        try {
            entidadService.eliminarEntidad(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ResourceNotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
