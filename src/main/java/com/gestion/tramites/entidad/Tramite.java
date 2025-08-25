package com.gestion.tramites.entidad;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// AHORA SÍ, ESTOS SON LOS IMPORTS CORRECTOS PARA TUS ENTIDADES
import com.gestion.tramites.model.Entidad; // <-- ¡CAMBIADO! Estaba en 'entidad', ahora es 'model'
import com.gestion.tramites.model.Usuario; // <-- ¡CAMBIADO! Estaba en 'entidad', ahora es 'model'

@Entity
@Table(name = "tramites")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tramite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tramite")
    private Long idTramite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_solicitante", nullable = false)
    private Usuario solicitante; // Usuario que inicia el trámite

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_entidad", nullable = false)
    private Entidad entidad; // Entidad responsable de gestionar este trámite

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_revisor") // Puede ser nulo al inicio
    private Usuario revisor; // Usuario asignado para revisar el trámite (ej. un REVISOR de la entidad)

    @Column(nullable = false, length = 100)
    private String nombreTramite; // Ej. "Licencia de Construcción", "Permiso de Uso de Suelo"

    @Column(nullable = false, length = 255)
    private String descripcion;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoTramite estado; // EN_REVISION, APROBADO, RECHAZADO, PENDIENTE, EN_ESPERA, etc.

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;

    private LocalDateTime fechaFinalizacion; // Fecha en que el trámite fue aprobado/rechazado

    @Column(length = 500)
    private String comentariosRevisor; // Comentarios del revisor, en caso de aprobación/rechazo/solicitud de información

    @Column(length = 255)
    private String numeroExpediente; // Opcional: número de expediente interno del trámite

    // Enum para el estado del trámite
    public enum EstadoTramite {
        PENDIENTE,        // Recién creado, esperando asignación/revisión
        EN_REVISION,      // Asignado a un revisor, en proceso
        EN_ESPERA_INFO,   // Revisor solicitó más información al solicitante
        APROBADO,         // Trámite finalizado con éxito
        RECHAZADO,        // Trámite finalizado sin éxito
        CANCELADO         // Trámite cancelado por el solicitante
    }

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoTramite.PENDIENTE; // Estado por defecto al crear
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}

