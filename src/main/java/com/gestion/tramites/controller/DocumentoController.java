package com.gestion.tramites.controller;

import com.gestion.tramites.dto.documentos.DocumentoResponseDTO;
import com.gestion.tramites.dto.documentos.EstadisticasDocumentosDTO;
import com.gestion.tramites.model.Documento;
import com.gestion.tramites.service.DocumentoService;
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
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documentos")
@Tag(name = "Documentos", description = "Gestión de documentos asociados a trámites - Upload, download, versionado y control de acceso")
@SecurityRequirement(name = "Bearer Authentication")
public class DocumentoController {

    private final DocumentoService documentoService;

    @Autowired
    public DocumentoController(DocumentoService documentoService) {
        this.documentoService = documentoService;
    }

    @Operation(
            summary = "Subir un documento a un trámite",
            description = "Permite subir un archivo asociado a un trámite específico. " +
                         "Incluye validación de tipos de archivo, límites de tamaño y versionado automático. " +
                         "Solo usuarios autorizados pueden subir documentos al trámite."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Documento subido exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DocumentoResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Documento subido",
                                    value = """
                                            {
                                              "id": 1,
                                              "nombreOriginal": "cedula_ciudadania.pdf",
                                              "tipoMime": "application/pdf",
                                              "extension": "pdf",
                                              "tamanoBytes": 2048576,
                                              "tamanoLegible": "2.0 MB",
                                              "tipoDocumento": "CEDULA_CIUDADANIA",
                                              "descripcionTipoDocumento": "Cédula de Ciudadanía",
                                              "descripcion": "Documento de identificación del solicitante",
                                              "version": 1,
                                              "esVersionActual": true,
                                              "estado": "ACTIVO",
                                              "fechaSubida": "2024-01-15T10:30:00",
                                              "tramiteId": 123,
                                              "numeroRadicacionTramite": "11001-123-2024-00001",
                                              "nombreUsuarioSubida": "Juan Pérez García"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error en la validación del archivo",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error validación",
                                    value = """
                                            {
                                              "timestamp": "2024-01-15T10:30:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Extensión de archivo no permitida: exe. Extensiones permitidas: pdf, doc, docx, jpg, jpeg, png"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para subir documentos a este trámite"),
            @ApiResponse(responseCode = "404", description = "Trámite no encontrado")
    })
    @PostMapping(value = "/tramite/{tramiteId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SOLICITANTE') or hasRole('REVISOR') or hasRole('ADMIN_ENTIDAD') or hasRole('ADMIN_GLOBAL')")
    public ResponseEntity<DocumentoResponseDTO> subirDocumento(
            @Parameter(description = "ID del trámite", required = true, example = "123")
            @PathVariable Long tramiteId,

            @Parameter(description = "Archivo a subir", required = true)
            @RequestParam("archivo") MultipartFile archivo,

            @Parameter(description = "Tipo de documento", required = true)
            @RequestParam("tipoDocumento") Documento.TipoDocumento tipoDocumento,

            @Parameter(description = "Descripción opcional del documento")
            @RequestParam(value = "descripcion", required = false) String descripcion) throws IOException {

        Documento documento = documentoService.subirDocumento(tramiteId, archivo, tipoDocumento, descripcion);
        DocumentoResponseDTO response = new DocumentoResponseDTO(documento);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Descargar un documento",
            description = "Descarga un archivo específico por su ID. " +
                         "Solo usuarios con acceso al trámite pueden descargar sus documentos. " +
                         "Actualiza la fecha de último acceso para auditoría."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Archivo descargado exitosamente",
                    content = @Content(mediaType = "application/octet-stream")
            ),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para descargar este documento"),
            @ApiResponse(responseCode = "404", description = "Documento no encontrado")
    })
    @GetMapping("/{documentoId}/download")
    @PreAuthorize("hasRole('SOLICITANTE') or hasRole('REVISOR') or hasRole('ADMIN_ENTIDAD') or hasRole('ADMIN_GLOBAL')")
    public ResponseEntity<Resource> descargarDocumento(
            @Parameter(description = "ID del documento a descargar", required = true, example = "1")
            @PathVariable Long documentoId) {

        Documento documento = documentoService.obtenerDocumento(documentoId);
        Resource archivo = documentoService.descargarDocumento(documentoId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(documento.getTipoMime()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                       "attachment; filename=\"" + documento.getNombreOriginal() + "\"")
                .body(archivo);
    }

    @Operation(
            summary = "Obtener documentos de un trámite",
            description = "Lista todos los documentos activos asociados a un trámite específico. " +
                         "Incluye información completa de cada documento y respeta el control de acceso multi-tenant."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de documentos obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DocumentoResponseDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para acceder a este trámite"),
            @ApiResponse(responseCode = "404", description = "Trámite no encontrado")
    })
    @GetMapping("/tramite/{tramiteId}")
    @PreAuthorize("hasRole('SOLICITANTE') or hasRole('REVISOR') or hasRole('ADMIN_ENTIDAD') or hasRole('ADMIN_GLOBAL')")
    public ResponseEntity<List<DocumentoResponseDTO>> obtenerDocumentosPorTramite(
            @Parameter(description = "ID del trámite", required = true, example = "123")
            @PathVariable Long tramiteId) {

        List<Documento> documentos = documentoService.obtenerDocumentosPorTramite(tramiteId);
        List<DocumentoResponseDTO> response = documentos.stream()
                .map(DocumentoResponseDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Obtener información de un documento",
            description = "Obtiene la información detallada de un documento específico sin descargarlo. " +
                         "Incluye metadatos, versión, estado y información del trámite asociado."
    )
    @GetMapping("/{documentoId}")
    @PreAuthorize("hasRole('SOLICITANTE') or hasRole('REVISOR') or hasRole('ADMIN_ENTIDAD') or hasRole('ADMIN_GLOBAL')")
    public ResponseEntity<DocumentoResponseDTO> obtenerDocumento(
            @Parameter(description = "ID del documento", required = true, example = "1")
            @PathVariable Long documentoId) {

        Documento documento = documentoService.obtenerDocumento(documentoId);
        DocumentoResponseDTO response = new DocumentoResponseDTO(documento);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Eliminar un documento",
            description = "Marca un documento como eliminado (eliminación lógica). " +
                         "Solo el usuario que subió el documento, revisores o administradores pueden eliminarlo. " +
                         "El archivo físico se mantiene para auditoría."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Documento eliminado exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para eliminar este documento"),
            @ApiResponse(responseCode = "404", description = "Documento no encontrado")
    })
    @DeleteMapping("/{documentoId}")
    @PreAuthorize("hasRole('REVISOR') or hasRole('ADMIN_ENTIDAD') or hasRole('ADMIN_GLOBAL')")
    public ResponseEntity<Void> eliminarDocumento(
            @Parameter(description = "ID del documento a eliminar", required = true, example = "1")
            @PathVariable Long documentoId) {

        documentoService.eliminarDocumento(documentoId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Buscar documentos por nombre",
            description = "Busca documentos dentro de un trámite por nombre (búsqueda parcial, insensible a mayúsculas). " +
                         "Útil para encontrar documentos específicos en trámites con muchos archivos."
    )
    @GetMapping("/tramite/{tramiteId}/buscar")
    @PreAuthorize("hasRole('SOLICITANTE') or hasRole('REVISOR') or hasRole('ADMIN_ENTIDAD') or hasRole('ADMIN_GLOBAL')")
    public ResponseEntity<List<DocumentoResponseDTO>> buscarDocumentos(
            @Parameter(description = "ID del trámite", required = true, example = "123")
            @PathVariable Long tramiteId,

            @Parameter(description = "Texto a buscar en el nombre del documento", required = true, example = "cedula")
            @RequestParam("q") String consulta) {

        List<Documento> documentos = documentoService.buscarDocumentosPorNombre(tramiteId, consulta);
        List<DocumentoResponseDTO> response = documentos.stream()
                .map(DocumentoResponseDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Obtener versiones de un documento",
            description = "Lista todas las versiones de un documento específico, ordenadas por versión descendente. " +
                         "Incluye versiones anteriores archivadas y la versión actual activa."
    )
    @GetMapping("/{documentoId}/versiones")
    @PreAuthorize("hasRole('SOLICITANTE') or hasRole('REVISOR') or hasRole('ADMIN_ENTIDAD') or hasRole('ADMIN_GLOBAL')")
    public ResponseEntity<List<DocumentoResponseDTO>> obtenerVersionesDocumento(
            @Parameter(description = "ID del documento", required = true, example = "1")
            @PathVariable Long documentoId) {

        List<Documento> versiones = documentoService.obtenerVersionesDocumento(documentoId);
        List<DocumentoResponseDTO> response = versiones.stream()
                .map(DocumentoResponseDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Obtener documentos por tipo",
            description = "Lista todos los documentos de un tipo específico dentro de un trámite. " +
                         "Útil para verificar qué documentos de cierto tipo han sido subidos."
    )
    @GetMapping("/tramite/{tramiteId}/tipo/{tipoDocumento}")
    @PreAuthorize("hasRole('SOLICITANTE') or hasRole('REVISOR') or hasRole('ADMIN_ENTIDAD') or hasRole('ADMIN_GLOBAL')")
    public ResponseEntity<List<DocumentoResponseDTO>> obtenerDocumentosPorTipo(
            @Parameter(description = "ID del trámite", required = true, example = "123")
            @PathVariable Long tramiteId,

            @Parameter(description = "Tipo de documento a filtrar", required = true)
            @PathVariable Documento.TipoDocumento tipoDocumento) {

        List<Documento> documentos = documentoService.obtenerDocumentosPorTipoYTramite(tramiteId, tipoDocumento);
        List<DocumentoResponseDTO> response = documentos.stream()
                .map(DocumentoResponseDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Obtener estadísticas de documentos",
            description = "Proporciona estadísticas agregadas de los documentos de un trámite: " +
                         "cantidad total y tamaño total ocupado. Útil para dashboards y control de límites."
    )
    @GetMapping("/tramite/{tramiteId}/estadisticas")
    @PreAuthorize("hasRole('SOLICITANTE') or hasRole('REVISOR') or hasRole('ADMIN_ENTIDAD') or hasRole('ADMIN_GLOBAL')")
    public ResponseEntity<EstadisticasDocumentosDTO> obtenerEstadisticas(
            @Parameter(description = "ID del trámite", required = true, example = "123")
            @PathVariable Long tramiteId) {

        DocumentoService.EstadisticasDocumentos estadisticas = documentoService.obtenerEstadisticas(tramiteId);
        EstadisticasDocumentosDTO response = new EstadisticasDocumentosDTO(estadisticas, tramiteId);

        return ResponseEntity.ok(response);
    }
}