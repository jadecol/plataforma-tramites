package com.gestion.tramites.dto.reportes;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Métricas del dashboard para gestión de trámites")
public class DashboardMetricasDTO {

    @Schema(description = "Total de trámites en el sistema", example = "142")
    private Long totalTramites;

    @Schema(description = "Trámites creados en el último mes", example = "23")
    private Long tramitesUltimoMes;

    @Schema(description = "Trámites creados hoy", example = "3")
    private Long tramitesHoy;

    @Schema(description = "Distribución de trámites por estado")
    private Map<String, Long> distribucionPorEstado;

    @Schema(description = "Tiempo promedio de procesamiento en días", example = "12.5")
    private Double tiempoPromedioProcesamientoDias;

    @Schema(description = "Trámites vencidos (que superaron límites)", example = "5")
    private Long tramitesVencidos;

    @Schema(description = "Carga de trabajo por revisor")
    private Map<String, Long> cargaPorRevisor;

    @Schema(description = "Trámites por tipo")
    private Map<String, Long> tramitesPorTipo;

    @Schema(description = "Tasa de aprobación (%) del último mes", example = "78.5")
    private Double tasaAprobacionUltimoMes;

    @Schema(description = "Fecha y hora de generación del reporte")
    private LocalDateTime fechaGeneracion;

    @Schema(description = "Nombre de la entidad (null para admin global)")
    private String nombreEntidad;

    public DashboardMetricasDTO() {
        this.fechaGeneracion = LocalDateTime.now();
    }

    public Long getTotalTramites() {
        return totalTramites;
    }

    public void setTotalTramites(Long totalTramites) {
        this.totalTramites = totalTramites;
    }

    public Long getTramitesUltimoMes() {
        return tramitesUltimoMes;
    }

    public void setTramitesUltimoMes(Long tramitesUltimoMes) {
        this.tramitesUltimoMes = tramitesUltimoMes;
    }

    public Long getTramitesHoy() {
        return tramitesHoy;
    }

    public void setTramitesHoy(Long tramitesHoy) {
        this.tramitesHoy = tramitesHoy;
    }

    public Map<String, Long> getDistribucionPorEstado() {
        return distribucionPorEstado;
    }

    public void setDistribucionPorEstado(Map<String, Long> distribucionPorEstado) {
        this.distribucionPorEstado = distribucionPorEstado;
    }

    public Double getTiempoPromedioProcesamientoDias() {
        return tiempoPromedioProcesamientoDias;
    }

    public void setTiempoPromedioProcesamientoDias(Double tiempoPromedioProcesamientoDias) {
        this.tiempoPromedioProcesamientoDias = tiempoPromedioProcesamientoDias;
    }

    public Long getTramitesVencidos() {
        return tramitesVencidos;
    }

    public void setTramitesVencidos(Long tramitesVencidos) {
        this.tramitesVencidos = tramitesVencidos;
    }

    public Map<String, Long> getCargaPorRevisor() {
        return cargaPorRevisor;
    }

    public void setCargaPorRevisor(Map<String, Long> cargaPorRevisor) {
        this.cargaPorRevisor = cargaPorRevisor;
    }

    public Map<String, Long> getTramitesPorTipo() {
        return tramitesPorTipo;
    }

    public void setTramitesPorTipo(Map<String, Long> tramitesPorTipo) {
        this.tramitesPorTipo = tramitesPorTipo;
    }

    public Double getTasaAprobacionUltimoMes() {
        return tasaAprobacionUltimoMes;
    }

    public void setTasaAprobacionUltimoMes(Double tasaAprobacionUltimoMes) {
        this.tasaAprobacionUltimoMes = tasaAprobacionUltimoMes;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public String getNombreEntidad() {
        return nombreEntidad;
    }

    public void setNombreEntidad(String nombreEntidad) {
        this.nombreEntidad = nombreEntidad;
    }
}