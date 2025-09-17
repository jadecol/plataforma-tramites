package com.gestion.tramites.dto.tramite;

import com.gestion.tramites.model.Tramite;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TramiteResponseDTO {

    private Long idTramite;
    private String numeroRadicacion;
    private LocalDate fechaRadicacion;
    private String objetoTramite;
    private String descripcionProyecto;
    private String direccionInmueble;
    private String condicionRadicacion;

    // Estado y fechas
    private Tramite.EstadoTramite estadoActual;
    private String descripcionEstado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimoCambioEstado;
    private LocalDate fechaLimiteProximo;
    private LocalDate fechaLimiteCompletar;
    private LocalDateTime fechaFinalizacion;
    private String comentariosRevisor;

    // Información de entidad (multi-tenant)
    private Long idEntidad;
    private String nombreEntidad;

    // Información de usuarios
    private Long idSolicitante;
    private String nombreSolicitante;
    private String correoSolicitante;

    private Long idRevisorAsignado;
    private String nombreRevisor;

    // Información de clasificación
    private Long idTipoTramite;
    private String nombreTipoTramite;

    private Long idModalidadTramite;
    private String nombreModalidadTramite;

    private Long idSubtipoTramite;
    private String nombreSubtipoTramite;

    // Métodos de utilidad
    public String getDescripcionEstado() {
        return estadoActual != null ? estadoActual.getDescripcion() : null;
    }

    public boolean esEditable() {
        return estadoActual == Tramite.EstadoTramite.RADICADO
                || estadoActual == Tramite.EstadoTramite.PENDIENTE_DOCUMENTOS;
    }

    public boolean estaFinalizado() {
        return estadoActual == Tramite.EstadoTramite.APROBADO
                || estadoActual == Tramite.EstadoTramite.RECHAZADO
                || estadoActual == Tramite.EstadoTramite.ARCHIVADO
                || estadoActual == Tramite.EstadoTramite.CANCELADO;
    }

    // Constructores
    public TramiteResponseDTO() {}

    // Getters y Setters
    public Long getIdTramite() {
        return idTramite;
    }

    public void setIdTramite(Long idTramite) {
        this.idTramite = idTramite;
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

    public Tramite.EstadoTramite getEstadoActual() {
        return estadoActual;
    }

    public void setEstadoActual(Tramite.EstadoTramite estadoActual) {
        this.estadoActual = estadoActual;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
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

    public LocalDateTime getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public void setFechaFinalizacion(LocalDateTime fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
    }

    public String getComentariosRevisor() {
        return comentariosRevisor;
    }

    public void setComentariosRevisor(String comentariosRevisor) {
        this.comentariosRevisor = comentariosRevisor;
    }

    public Long getIdEntidad() {
        return idEntidad;
    }

    public void setIdEntidad(Long idEntidad) {
        this.idEntidad = idEntidad;
    }

    public String getNombreEntidad() {
        return nombreEntidad;
    }

    public void setNombreEntidad(String nombreEntidad) {
        this.nombreEntidad = nombreEntidad;
    }

    public Long getIdSolicitante() {
        return idSolicitante;
    }

    public void setIdSolicitante(Long idSolicitante) {
        this.idSolicitante = idSolicitante;
    }

    public String getNombreSolicitante() {
        return nombreSolicitante;
    }

    public void setNombreSolicitante(String nombreSolicitante) {
        this.nombreSolicitante = nombreSolicitante;
    }

    public String getCorreoSolicitante() {
        return correoSolicitante;
    }

    public void setCorreoSolicitante(String correoSolicitante) {
        this.correoSolicitante = correoSolicitante;
    }

    public Long getIdRevisorAsignado() {
        return idRevisorAsignado;
    }

    public void setIdRevisorAsignado(Long idRevisorAsignado) {
        this.idRevisorAsignado = idRevisorAsignado;
    }

    public String getNombreRevisor() {
        return nombreRevisor;
    }

    public void setNombreRevisor(String nombreRevisor) {
        this.nombreRevisor = nombreRevisor;
    }

    public Long getIdTipoTramite() {
        return idTipoTramite;
    }

    public void setIdTipoTramite(Long idTipoTramite) {
        this.idTipoTramite = idTipoTramite;
    }

    public String getNombreTipoTramite() {
        return nombreTipoTramite;
    }

    public void setNombreTipoTramite(String nombreTipoTramite) {
        this.nombreTipoTramite = nombreTipoTramite;
    }

    public Long getIdModalidadTramite() {
        return idModalidadTramite;
    }

    public void setIdModalidadTramite(Long idModalidadTramite) {
        this.idModalidadTramite = idModalidadTramite;
    }

    public String getNombreModalidadTramite() {
        return nombreModalidadTramite;
    }

    public void setNombreModalidadTramite(String nombreModalidadTramite) {
        this.nombreModalidadTramite = nombreModalidadTramite;
    }

    public Long getIdSubtipoTramite() {
        return idSubtipoTramite;
    }

    public void setIdSubtipoTramite(Long idSubtipoTramite) {
        this.idSubtipoTramite = idSubtipoTramite;
    }

    public String getNombreSubtipoTramite() {
        return nombreSubtipoTramite;
    }

    public void setNombreSubtipoTramite(String nombreSubtipoTramite) {
        this.nombreSubtipoTramite = nombreSubtipoTramite;
    }
}
