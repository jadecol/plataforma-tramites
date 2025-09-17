package com.gestion.tramites.dto.tramite;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TramitePublicoDTO {
    private String numeroRadicacion;
    private LocalDate fechaRadicacion;
    private String objetoTramite;
    private String estadoActual;
    private String descripcionEstado;
    private LocalDateTime fechaUltimoCambio;
    private String nombreEntidad;
    private LocalDate fechaLimiteProximo;
    private LocalDate fechaLimiteCompletar;
    private String tipoTramite;

    public TramitePublicoDTO() {}

    // Getters y Setters
    public String getNumeroRadicacion() {
        return numeroRadicacion;
    }

    public void setNumeroRadicacion(String numeroRadicacion) {
        this.numeroRadicacion = numeroRadicacion;
    }

    public LocalDate getFechaRadicacion() {
        return fechaRadicacion;
    }

    public void setFechaRadicacion(LocalDate fechaRadicacion) {
        this.fechaRadicacion = fechaRadicacion;
    }

    public String getObjetoTramite() {
        return objetoTramite;
    }

    public void setObjetoTramite(String objetoTramite) {
        this.objetoTramite = objetoTramite;
    }

    public String getEstadoActual() {
        return estadoActual;
    }

    public void setEstadoActual(String estadoActual) {
        this.estadoActual = estadoActual;
    }

    public String getDescripcionEstado() {
        return descripcionEstado;
    }

    public void setDescripcionEstado(String descripcionEstado) {
        this.descripcionEstado = descripcionEstado;
    }

    public LocalDateTime getFechaUltimoCambio() {
        return fechaUltimoCambio;
    }

    public void setFechaUltimoCambio(LocalDateTime fechaUltimoCambio) {
        this.fechaUltimoCambio = fechaUltimoCambio;
    }

    public String getNombreEntidad() {
        return nombreEntidad;
    }

    public void setNombreEntidad(String nombreEntidad) {
        this.nombreEntidad = nombreEntidad;
    }

    public LocalDate getFechaLimiteProximo() {
        return fechaLimiteProximo;
    }

    public void setFechaLimiteProximo(LocalDate fechaLimiteProximo) {
        this.fechaLimiteProximo = fechaLimiteProximo;
    }

    public LocalDate getFechaLimiteCompletar() {
        return fechaLimiteCompletar;
    }

    public void setFechaLimiteCompletar(LocalDate fechaLimiteCompletar) {
        this.fechaLimiteCompletar = fechaLimiteCompletar;
    }

    public String getTipoTramite() {
        return tipoTramite;
    }

    public void setTipoTramite(String tipoTramite) {
        this.tipoTramite = tipoTramite;
    }
}
