package com.gestion.tramites.dto.documentos;

import com.gestion.tramites.model.Documento;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Información completa de un documento")
public class DocumentoResponseDTO {

    @Schema(description = "ID único del documento", example = "1")
    private Long id;

    @Schema(description = "Nombre original del archivo", example = "cedula_ciudadania.pdf")
    private String nombreOriginal;

    @Schema(description = "Tipo MIME del archivo", example = "application/pdf")
    private String tipoMime;

    @Schema(description = "Extensión del archivo", example = "pdf")
    private String extension;

    @Schema(description = "Tamaño del archivo en bytes", example = "2048576")
    private Long tamanoBytes;

    @Schema(description = "Tamaño del archivo en formato legible", example = "2.0 MB")
    private String tamanoLegible;

    @Schema(description = "Tipo de documento")
    private String tipoDocumento;

    @Schema(description = "Descripción del tipo de documento")
    private String descripcionTipoDocumento;

    @Schema(description = "Descripción adicional del documento")
    private String descripcion;

    @Schema(description = "Número de versión del documento", example = "1")
    private Integer version;

    @Schema(description = "Indica si es la versión actual del documento", example = "true")
    private Boolean esVersionActual;

    @Schema(description = "Estado del documento")
    private String estado;

    @Schema(description = "Fecha y hora de subida del documento")
    private LocalDateTime fechaSubida;

    @Schema(description = "Fecha y hora del último acceso al documento")
    private LocalDateTime fechaUltimoAcceso;

    @Schema(description = "ID del trámite al que pertenece", example = "123")
    private Long tramiteId;

    @Schema(description = "Número de radicación del trámite", example = "11001-123-2024-00001")
    private String numeroRadicacionTramite;

    @Schema(description = "Nombre completo del usuario que subió el documento")
    private String nombreUsuarioSubida;

    @Schema(description = "ID del documento padre (si es una versión)", example = "45")
    private Long documentoPadreId;

    @Schema(description = "Hash SHA-256 del archivo para verificación de integridad")
    private String hashArchivo;

    public DocumentoResponseDTO() {}

    public DocumentoResponseDTO(Documento documento) {
        this.id = documento.getIdDocumento();
        this.nombreOriginal = documento.getNombreOriginal();
        this.tipoMime = documento.getTipoMime();
        this.extension = documento.getExtension();
        this.tamanoBytes = documento.getTamanoBytes();
        this.tamanoLegible = documento.getTamanoLegible();
        this.tipoDocumento = documento.getTipoDocumento().name();
        this.descripcionTipoDocumento = documento.getTipoDocumento().getDescripcion();
        this.descripcion = documento.getDescripcion();
        this.version = documento.getVersion();
        this.esVersionActual = documento.getEsVersionActual();
        this.estado = documento.getEstado().name();
        this.fechaSubida = documento.getFechaSubida();
        this.fechaUltimoAcceso = documento.getFechaUltimoAcceso();
        this.tramiteId = documento.getTramite().getIdTramite();
        this.numeroRadicacionTramite = documento.getTramite().getNumeroRadicacion();
        this.nombreUsuarioSubida = documento.getUsuarioSubida().getNombreCompleto();
        this.documentoPadreId = documento.getDocumentoPadre() != null ?
                               documento.getDocumentoPadre().getIdDocumento() : null;
        this.hashArchivo = documento.getHashArchivo();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreOriginal() { return nombreOriginal; }
    public void setNombreOriginal(String nombreOriginal) { this.nombreOriginal = nombreOriginal; }

    public String getTipoMime() { return tipoMime; }
    public void setTipoMime(String tipoMime) { this.tipoMime = tipoMime; }

    public String getExtension() { return extension; }
    public void setExtension(String extension) { this.extension = extension; }

    public Long getTamanoBytes() { return tamanoBytes; }
    public void setTamanoBytes(Long tamanoBytes) { this.tamanoBytes = tamanoBytes; }

    public String getTamanoLegible() { return tamanoLegible; }
    public void setTamanoLegible(String tamanoLegible) { this.tamanoLegible = tamanoLegible; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getDescripcionTipoDocumento() { return descripcionTipoDocumento; }
    public void setDescripcionTipoDocumento(String descripcionTipoDocumento) { this.descripcionTipoDocumento = descripcionTipoDocumento; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public Boolean getEsVersionActual() { return esVersionActual; }
    public void setEsVersionActual(Boolean esVersionActual) { this.esVersionActual = esVersionActual; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaSubida() { return fechaSubida; }
    public void setFechaSubida(LocalDateTime fechaSubida) { this.fechaSubida = fechaSubida; }

    public LocalDateTime getFechaUltimoAcceso() { return fechaUltimoAcceso; }
    public void setFechaUltimoAcceso(LocalDateTime fechaUltimoAcceso) { this.fechaUltimoAcceso = fechaUltimoAcceso; }

    public Long getTramiteId() { return tramiteId; }
    public void setTramiteId(Long tramiteId) { this.tramiteId = tramiteId; }

    public String getNumeroRadicacionTramite() { return numeroRadicacionTramite; }
    public void setNumeroRadicacionTramite(String numeroRadicacionTramite) { this.numeroRadicacionTramite = numeroRadicacionTramite; }

    public String getNombreUsuarioSubida() { return nombreUsuarioSubida; }
    public void setNombreUsuarioSubida(String nombreUsuarioSubida) { this.nombreUsuarioSubida = nombreUsuarioSubida; }

    public Long getDocumentoPadreId() { return documentoPadreId; }
    public void setDocumentoPadreId(Long documentoPadreId) { this.documentoPadreId = documentoPadreId; }

    public String getHashArchivo() { return hashArchivo; }
    public void setHashArchivo(String hashArchivo) { this.hashArchivo = hashArchivo; }
}