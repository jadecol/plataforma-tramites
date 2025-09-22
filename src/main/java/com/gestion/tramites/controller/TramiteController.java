package com.gestion.tramites.controller;

import com.gestion.tramites.dto.tramite.TramiteEstadoUpdateDTO;
import com.gestion.tramites.dto.tramite.TramiteRequestDTO;
import com.gestion.tramites.dto.tramite.TramiteResponseDTO;
import com.gestion.tramites.exception.ResourceNotFoundException;
import com.gestion.tramites.model.Tramite; // Import Tramite to access EstadoTramite enum
import com.gestion.tramites.service.TramiteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tramites")
@Tag(name = "Trámites", description = "Gestión completa del ciclo de vida de trámites urbanísticos")
@SecurityRequirement(name = "Bearer Authentication")
public class TramiteController {

    private final TramiteService tramiteService;

    @Autowired
    public TramiteController(TramiteService tramiteService) {
        this.tramiteService = tramiteService;
    }

    @Operation(
            summary = "Listar todos los trámites",
            description = "Obtiene la lista completa de trámites filtrados por la entidad del usuario autenticado (multi-tenant)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de trámites obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TramiteResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token JWT inválido o expirado",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuario sin permisos para acceder a trámites",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping
    public ResponseEntity<List<TramiteResponseDTO>> getAllTramites() {
        List<TramiteResponseDTO> tramites = tramiteService.obtenerTramites();
        return ResponseEntity.ok(tramites);
    }

    @Operation(
            summary = "Obtener trámite por ID",
            description = "Obtiene los detalles completos de un trámite específico por su identificador"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Trámite encontrado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TramiteResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Trámite ejemplo",
                                    value = """
                                            {
                                              "id": 1,
                                              "numeroRadicacion": "11001-123-2024-00001",
                                              "descripcion": "Licencia de construcción para vivienda unifamiliar",
                                              "estado": "EN_REVISION",
                                              "fechaCreacion": "2024-01-15T10:30:00",
                                              "fechaUltimaActualizacion": "2024-01-16T14:20:00",
                                              "tipoTramite": {
                                                "id": 1,
                                                "nombre": "Licencia de Construcción"
                                              },
                                              "solicitante": {
                                                "id": 2,
                                                "nombreCompleto": "Juan Pérez García",
                                                "correoElectronico": "juan.perez@email.com"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trámite no encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error no encontrado",
                                    value = """
                                            {
                                              "timestamp": "2024-01-01T10:00:00",
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Tramite no encontrado con id: 999"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token JWT inválido o expirado",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<TramiteResponseDTO> getTramiteById(
            @Parameter(description = "ID único del trámite", required = true, example = "1")
            @PathVariable Long id) {
        TramiteResponseDTO tramite = tramiteService.obtenerTramitePorId(id);
        return ResponseEntity.ok(tramite);
    }

    @Operation(
            summary = "Crear nuevo trámite",
            description = "Registra un nuevo trámite en el sistema generando automáticamente el número de radicación"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Trámite creado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TramiteResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error validación",
                                    value = """
                                            {
                                              "timestamp": "2024-01-01T10:00:00",
                                              "status": 400,
                                              "error": "Validation Failed",
                                              "message": "descripcion: no debe estar vacío"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token JWT inválido o expirado",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping
    public ResponseEntity<TramiteResponseDTO> createTramite(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del trámite a crear",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TramiteRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "Nuevo trámite",
                                    value = """
                                            {
                                              "descripcion": "Licencia de construcción para edificio de apartamentos",
                                              "idTipoTramite": 1,
                                              "idSubtipoTramite": 2,
                                              "idModalidadTramite": 1,
                                              "idSolicitante": 3
                                            }
                                            """
                            )
                    )
            )
            TramiteRequestDTO tramiteRequestDTO) {
        TramiteResponseDTO createdTramite = tramiteService.crearTramite(tramiteRequestDTO);
        return new ResponseEntity<>(createdTramite, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Actualizar trámite",
            description = "Actualiza los datos de un trámite existente"
    )
    @PutMapping("/{id}")
    public ResponseEntity<TramiteResponseDTO> updateTramite(
            @Parameter(description = "ID del trámite a actualizar", required = true)
            @PathVariable Long id,
            @Valid @RequestBody TramiteRequestDTO tramiteRequestDTO) {
        TramiteResponseDTO updatedTramite = tramiteService.actualizarTramite(id, tramiteRequestDTO);
        return ResponseEntity.ok(updatedTramite);
    }

    @Operation(
            summary = "Eliminar trámite",
            description = "Elimina un trámite del sistema de forma permanente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Trámite eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Trámite no encontrado"),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTramite(
            @Parameter(description = "ID del trámite a eliminar", required = true)
            @PathVariable Long id) {
        tramiteService.eliminarTramite(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Actualizar estado del trámite",
            description = "Cambia el estado de un trámite en su ciclo de vida (RECIBIDO → EN_REVISION → APROBADO/RECHAZADO)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado actualizado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TramiteResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Estado inválido o transición no permitida",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error estado",
                                    value = """
                                            {
                                              "timestamp": "2024-01-01T10:00:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Transición de estado inválida"
                                            }
                                            """
                            )
                    )
            )
    })
    @PatchMapping("/{id}/estado")
    public ResponseEntity<TramiteResponseDTO> updateTramiteEstado(
            @Parameter(description = "ID del trámite", required = true)
            @PathVariable Long id,
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nuevo estado y comentarios opcionales",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Cambio estado",
                                    value = """
                                            {
                                              "nuevoEstado": "APROBADO",
                                              "comentariosRevisor": "Cumple con todos los requisitos técnicos"
                                            }
                                            """
                            )
                    )
            )
            TramiteEstadoUpdateDTO estadoUpdateDTO) {
        // Corrected: Convert String to Tramite.EstadoTramite enum
        Tramite.EstadoTramite nuevoEstadoEnum = Tramite.EstadoTramite.valueOf(estadoUpdateDTO.getNuevoEstado());
        TramiteResponseDTO updatedTramite = tramiteService.actualizarEstado(id, nuevoEstadoEnum, estadoUpdateDTO.getComentariosRevisor());
        return ResponseEntity.ok(updatedTramite);
    }

    @Operation(
            summary = "Asignar revisor al trámite",
            description = "Asigna un funcionario revisor responsable de evaluar el trámite"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Revisor asignado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TramiteResponseDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Trámite o revisor no encontrado"),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido")
    })
    @PatchMapping("/{id}/revisor/{revisorId}")
    public ResponseEntity<TramiteResponseDTO> assignRevisor(
            @Parameter(description = "ID del trámite", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "ID del usuario revisor", required = true, example = "5")
            @PathVariable Long revisorId) {
        TramiteResponseDTO updatedTramite = tramiteService.asignarRevisor(id, revisorId);
        return ResponseEntity.ok(updatedTramite);
    }
}
