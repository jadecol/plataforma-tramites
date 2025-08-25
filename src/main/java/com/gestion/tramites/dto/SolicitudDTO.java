package com.gestion.tramites.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SolicitudDTO { // Asegúrate de que el nombre de la clase sea SolicitudDTO

    private Long idSolicitud; // Para cuando se lee o actualiza

    private String numeroRadicacion;
    private LocalDate fechaRadicacion;
    private String objetoTramite;
    private String descripcionProyecto;
    private String direccionInmueble;
    private String condicionRadicacion;
    private String estadoActual;
    private LocalDateTime fechaUltimoCambioEstado;
    private LocalDate fechaLimiteProximo;
    private LocalDate fechaLimiteCompletar;

    // IDs de las entidades relacionadas
    private Long idModalidadTramite;
    private Long idTipoTramite;
    private Long idSubtipoTramite;
    private Long idSolicitante;
    private Long idRevisorAsignado;

    // Constructor vacío
    public SolicitudDTO() {
    }

    // --- GETTERS Y SETTERS ---
    // Genera todos los getters y setters automáticamente en STS:
    // Clic derecho en el código de la clase > Source > Generate Getters and Setters...
    // Selecciona todos los campos y haz clic en Generate.

    public Long getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Long idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

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

    public String getDescripcionProyecto() {
        return descripcionProyecto;
    }

    public void setDescripcionProyecto(String descripcionProyecto) {
        this.descripcionProyecto = descripcionProyecto;
    }

    public String getDireccionInmueble() {
        return direccionInmueble;
    }

    public void setDireccionInmueble(String direccionInmueble) {
        this.direccionInmueble = direccionInmueble;
    }

    public String getCondicionRadicacion() {
        return condicionRadicacion;
    }

    public void setCondicionRadicacion(String condicionRadicacion) {
        this.condicionRadicacion = condicionRadicacion;
    }

    public String getEstadoActual() {
        return estadoActual;
    }

    public void setEstadoActual(String estadoActual) {
        this.estadoActual = estadoActual;
    }

    public LocalDateTime getFechaUltimoCambioEstado() {
        return fechaUltimoCambioEstado;
    }

    public void setFechaUltimoCambioEstado(LocalDateTime fechaUltimoCambioEstado) {
        this.fechaUltimoCambioEstado = fechaUltimoCambioEstado;
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

    public Long getIdModalidadTramite() {
        return idModalidadTramite;
    }

    public void setIdModalidadTramite(Long idModalidadTramite) {
        this.idModalidadTramite = idModalidadTramite;
    }

    public Long getIdTipoTramite() {
        return idTipoTramite;
    }

    public void setIdTipoTramite(Long idTipoTramite) {
        this.idTipoTramite = idTipoTramite;
    }

    public Long getIdSubtipoTramite() {
        return idSubtipoTramite;
    }

    public void setIdSubtipoTramite(Long idSubtipoTramite) {
        this.idSubtipoTramite = idSubtipoTramite;
    }

    public Long getIdSolicitante() {
        return idSolicitante;
    }

    public void setIdSolicitante(Long idSolicitante) {
        this.idSolicitante = idSolicitante;
    }

    public Long getIdRevisorAsignado() {
        return idRevisorAsignado;
    }

    public void setIdRevisorAsignado(Long idRevisorAsignado) {
        this.idRevisorAsignado = idRevisorAsignado;
    }
}