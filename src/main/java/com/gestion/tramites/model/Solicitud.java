package com.gestion.tramites.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes")
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSolicitud;

    @Column(name = "numero_radicacion", nullable = false, unique = true)
    private String numeroRadicacion;

    @Column(name = "fecha_radicacion", nullable = false)
    private LocalDate fechaRadicacion;

    @Column(name = "objeto_tramite")
    private String objetoTramite;

    @Column(name = "descripcion_proyecto")
    private String descripcionProyecto;

    @Column(name = "direccion_inmueble")
    private String direccionInmueble;

    @Column(name = "condicion_radicacion")
    private String condicionRadicacion;

    @Column(name = "estado_actual")
    private String estadoActual;

    @Column(name = "fecha_ultimo_cambio_estado")
    private LocalDateTime fechaUltimoCambioEstado;

    @Column(name = "fecha_limite_proximo")
    private LocalDate fechaLimiteProximo;

    @Column(name = "fecha_limite_completar")
    private LocalDate fechaLimiteCompletar;

    // --- RELACIONES CON OTRAS ENTIDADES ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_modalidad_tramite")
    private ModalidadTramite modalidadTramite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_tramite", nullable = false)
    private TipoTramite tipoTramite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_subtipo_tramite")
    private SubtipoTramite subtipoTramite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_solicitante", nullable = false)
    private Usuario solicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_revisor_asignado")
    private Usuario revisorAsignado;

    // Constructor vacío (necesario para JPA)
    public Solicitud() {
    }

    // --- GETTERS Y SETTERS ---
    // Si usas tu IDE (STS), puedes generarlos automáticamente:
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

    public ModalidadTramite getModalidadTramite() {
        return modalidadTramite;
    }

    public void setModalidadTramite(ModalidadTramite modalidadTramite) {
        this.modalidadTramite = modalidadTramite;
    }

    public TipoTramite getTipoTramite() {
        return tipoTramite;
    }

    public void setTipoTramite(TipoTramite tipoTramite) {
        this.tipoTramite = tipoTramite;
    }

    public SubtipoTramite getSubtipoTramite() {
        return subtipoTramite;
    }

    public void setSubtipoTramite(SubtipoTramite subtipoTramite) {
        this.subtipoTramite = subtipoTramite;
    }

    public Usuario getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(Usuario solicitante) {
        this.solicitante = solicitante;
    }

    public Usuario getRevisorAsignado() {
        return revisorAsignado;
    }

    public void setRevisorAsignado(Usuario revisorAsignado) {
        this.revisorAsignado = revisorAsignado;
    }
}
