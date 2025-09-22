package com.gestion.tramites.dto.documentos;

import com.gestion.tramites.service.DocumentoService;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estadísticas de documentos de un trámite")
public class EstadisticasDocumentosDTO {

    @Schema(description = "Número total de documentos activos", example = "15")
    private Long cantidadDocumentos;

    @Schema(description = "Tamaño total en bytes de todos los documentos", example = "52428800")
    private Long tamanoTotalBytes;

    @Schema(description = "Tamaño total en formato legible", example = "50.0 MB")
    private String tamanoTotalLegible;

    @Schema(description = "ID del trámite", example = "123")
    private Long tramiteId;

    public EstadisticasDocumentosDTO() {}

    public EstadisticasDocumentosDTO(DocumentoService.EstadisticasDocumentos estadisticas, Long tramiteId) {
        this.cantidadDocumentos = estadisticas.getCantidadDocumentos();
        this.tamanoTotalBytes = estadisticas.getTamanoTotal();
        this.tamanoTotalLegible = estadisticas.getTamanoTotalLegible();
        this.tramiteId = tramiteId;
    }

    public Long getCantidadDocumentos() { return cantidadDocumentos; }
    public void setCantidadDocumentos(Long cantidadDocumentos) { this.cantidadDocumentos = cantidadDocumentos; }

    public Long getTamanoTotalBytes() { return tamanoTotalBytes; }
    public void setTamanoTotalBytes(Long tamanoTotalBytes) { this.tamanoTotalBytes = tamanoTotalBytes; }

    public String getTamanoTotalLegible() { return tamanoTotalLegible; }
    public void setTamanoTotalLegible(String tamanoTotalLegible) { this.tamanoTotalLegible = tamanoTotalLegible; }

    public Long getTramiteId() { return tramiteId; }
    public void setTramiteId(Long tramiteId) { this.tramiteId = tramiteId; }
}