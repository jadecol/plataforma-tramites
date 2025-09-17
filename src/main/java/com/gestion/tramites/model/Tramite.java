package com.gestion.tramites.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tramites")
public class Tramite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tramite")
    private Long idTramite;

    // ============ CAMPOS MULTI-TENANT ============
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_entidad", nullable = false)
    @NotNull(message = "La entidad es obligatoria")
    private Entidad entidad; // CRÍTICO: Para aislamiento multi-tenant

    // ============ NUMERACIÓN Y RADICACIÓN ============
    @Column(name = "numero_radicacion", nullable = false, unique = true)
    @NotBlank(message = "El número de radicación es obligatorio")
    private String numeroRadicacion; // Formato: DANE-CURADURIA-AAAA-CONSECUTIVO

    @Column(name = "fecha_radicacion", nullable = false)
    @NotNull(message = "La fecha de radicación es obligatoria")
    private LocalDate fechaRadicacion;

    // ============ INFORMACIÓN DEL TRÁMITE ============
    @Column(name = "objeto_tramite", length = 500)
    private String objetoTramite; // Descripción del objetivo del trámite

    @Column(name = "descripcion_proyecto", length = 1000)
    private String descripcionProyecto; // Descripción detallada del proyecto

    @Column(name = "direccion_inmueble", length = 255)
    private String direccionInmueble; // Dirección del predio/inmueble

    // ============ GESTIÓN DE ESTADOS ============
    @Column(name = "estado_actual", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "El estado es obligatorio")
    private EstadoTramite estadoActual;

    @Column(name = "condicion_radicacion")
    private String condicionRadicacion; // Condiciones especiales de la radicación

    @Column(name = "comentarios_revisor", length = 1000)
    private String comentariosRevisor; // Observaciones del revisor

    // ============ FECHAS DE GESTIÓN ============
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_ultimo_cambio_estado")
    private LocalDateTime fechaUltimoCambioEstado;

    @Column(name = "fecha_limite_proximo")
    private LocalDate fechaLimiteProximo; // Próxima fecha límite para el ciudadano

    @Column(name = "fecha_limite_completar")
    private LocalDate fechaLimiteCompletar; // Fecha límite final del trámite

    @Column(name = "fecha_finalizacion")
    private LocalDateTime fechaFinalizacion; // Fecha de aprobación/rechazo final

    // ============ RELACIONES CON USUARIOS ============
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_solicitante", nullable = false)
    @NotNull(message = "El solicitante es obligatorio")
    private Usuario solicitante; // Ciudadano o profesional que radica

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_revisor_asignado")
    private Usuario revisorAsignado; // Revisor responsable (arquitecto, jurídico, etc.)

    // ============ CLASIFICACIÓN DE TRÁMITES ============
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_tramite", nullable = false)
    @NotNull(message = "El tipo de trámite es obligatorio")
    private TipoTramite tipoTramite; // Licencia, Concepto, Solicitud, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_modalidad_tramite")
    private ModalidadTramite modalidadTramite; // Urbanización, Construcción, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_subtipo_tramite")
    private SubtipoTramite subtipoTramite; // Clasificación específica

    // ============ ENUM DE ESTADOS ============
    public enum EstadoTramite {
        RADICADO("Radicado - En espera de asignación"), ASIGNADO("Asignado a revisor"), EN_REVISION(
                "En revisión técnica"), PENDIENTE_DOCUMENTOS(
                        "Pendiente de documentos"), EN_ESPERA_CONCEPTO(
                                "En espera de concepto técnico"), CONCEPTO_FAVORABLE(
                                        "Concepto técnico favorable"), CONCEPTO_DESFAVORABLE(
                                                "Concepto técnico desfavorable"), APROBADO(
                                                        "Trámite aprobado"), RECHAZADO(
                                                                "Trámite rechazado"), ARCHIVADO(
                                                                        "Archivado por abandono"), CANCELADO(
                                                                                "Cancelado por el solicitante");

        private final String descripcion;

        EstadoTramite(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    // ============ MÉTODOS DE CICLO DE VIDA ============
    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaUltimoCambioEstado = LocalDateTime.now();
        if (this.estadoActual == null) {
            this.estadoActual = EstadoTramite.RADICADO;
        }
        if (this.fechaRadicacion == null) {
            this.fechaRadicacion = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaUltimoCambioEstado = LocalDateTime.now();
    }

    // ============ MÉTODOS DE NEGOCIO ============
    public boolean esEditable() {
        return estadoActual == EstadoTramite.RADICADO
                || estadoActual == EstadoTramite.PENDIENTE_DOCUMENTOS;
    }

    public boolean estaFinalizado() {
        return estadoActual == EstadoTramite.APROBADO || estadoActual == EstadoTramite.RECHAZADO
                || estadoActual == EstadoTramite.ARCHIVADO
                || estadoActual == EstadoTramite.CANCELADO;
    }

    public void cambiarEstado(EstadoTramite nuevoEstado, String comentarios) {
        this.estadoActual = nuevoEstado;
        this.comentariosRevisor = comentarios;
        this.fechaUltimoCambioEstado = LocalDateTime.now();

        if (estaFinalizado()) {
            this.fechaFinalizacion = LocalDateTime.now();
        }
    }

    // ============ CONSTRUCTORES ============
    public Tramite() {}

    public Tramite(String numeroRadicacion, Entidad entidad, Usuario solicitante,
            TipoTramite tipoTramite, String objetoTramite) {
        this.numeroRadicacion = numeroRadicacion;
        this.entidad = entidad;
        this.solicitante = solicitante;
        this.tipoTramite = tipoTramite;
        this.objetoTramite = objetoTramite;
        this.estadoActual = EstadoTramite.RADICADO;
        this.fechaRadicacion = LocalDate.now();
    }

    // ============ GETTERS Y SETTERS ============
    public Long getIdTramite() {
        return idTramite;
    }

    public void setIdTramite(Long idTramite) {
        this.idTramite = idTramite;
    }

    public Entidad getEntidad() {
        return entidad;
    }

    public void setEntidad(Entidad entidad) {
        this.entidad = entidad;
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

    public EstadoTramite getEstadoActual() {
        return estadoActual;
    }

    public void setEstadoActual(EstadoTramite estadoActual) {
        this.estadoActual = estadoActual;
    }

    public String getCondicionRadicacion() {
        return condicionRadicacion;
    }

    public void setCondicionRadicacion(String condicionRadicacion) {
        this.condicionRadicacion = condicionRadicacion;
    }

    public String getComentariosRevisor() {
        return comentariosRevisor;
    }

    public void setComentariosRevisor(String comentariosRevisor) {
        this.comentariosRevisor = comentariosRevisor;
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

    public TipoTramite getTipoTramite() {
        return tipoTramite;
    }

    public void setTipoTramite(TipoTramite tipoTramite) {
        this.tipoTramite = tipoTramite;
    }

    public ModalidadTramite getModalidadTramite() {
        return modalidadTramite;
    }

    public void setModalidadTramite(ModalidadTramite modalidadTramite) {
        this.modalidadTramite = modalidadTramite;
    }

    public SubtipoTramite getSubtipoTramite() {
        return subtipoTramite;
    }

    public void setSubtipoTramite(SubtipoTramite subtipoTramite) {
        this.subtipoTramite = subtipoTramite;
    }
}

