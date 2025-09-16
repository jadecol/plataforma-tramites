package com.gestion.tramites.solicitud.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.gestion.tramites.model.Solicitud; // Necesario para el método fromEntity

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudResponseDTO {
    private Long idSolicitud;
    private String numeroRadicacion;
    private LocalDate fechaRadicacion;

    private Long idSolicitante;
    private String nombreSolicitante;

    private Long idRevisorAsignado;
    private String nombreRevisorAsignado;

    private String estadoActual;
    private String condicionRadicacion;
    private String objetoTramite;
    private String descripcionProyecto;
    private String direccionInmueble;

    private Long idTipoTramite;
    private String nombreTipoTramite; // Para mostrar el nombre del tipo de trámite

    private Long idSubtipoTramite;
    private String nombreSubtipoTramite; // Para mostrar el nombre del subtipo de trámite

    private Long idModalidadTramite;
    private String nombreModalidadTramite; // Para mostrar el nombre de la modalidad de trámite

    private LocalDate fechaLimiteCompletar;
    private LocalDate fechaLimiteProximo;
    private LocalDateTime fechaUltimoCambioEstado;

    // --- MÉTODO ESTÁTICO fromEntity (SOLUCIÓN AL ERROR) ---
    public static SolicitudResponseDTO fromEntity(Solicitud solicitud) {
        SolicitudResponseDTO dto = new SolicitudResponseDTO();
        dto.setIdSolicitud(solicitud.getIdSolicitud());
        dto.setNumeroRadicacion(solicitud.getNumeroRadicacion());
        dto.setFechaRadicacion(solicitud.getFechaRadicacion());

        if (solicitud.getSolicitante() != null) {
            dto.setIdSolicitante(solicitud.getSolicitante().getIdUsuario());
            dto.setNombreSolicitante(solicitud.getSolicitante().getNombreCompleto());
        }

        if (solicitud.getRevisorAsignado() != null) {
            dto.setIdRevisorAsignado(solicitud.getRevisorAsignado().getIdUsuario());
            dto.setNombreRevisorAsignado(solicitud.getRevisorAsignado().getNombreCompleto());
        }

        dto.setEstadoActual(solicitud.getEstadoActual());
        dto.setCondicionRadicacion(solicitud.getCondicionRadicacion());
        dto.setObjetoTramite(solicitud.getObjetoTramite());
        dto.setDescripcionProyecto(solicitud.getDescripcionProyecto());
        dto.setDireccionInmueble(solicitud.getDireccionInmueble());

        if (solicitud.getTipoTramite() != null) {
            dto.setIdTipoTramite(solicitud.getTipoTramite().getIdTipoTramite());
            dto.setNombreTipoTramite(solicitud.getTipoTramite().getNombre());
        }

        if (solicitud.getSubtipoTramite() != null) {
            dto.setIdSubtipoTramite(solicitud.getSubtipoTramite().getIdSubtipoTramite());
            dto.setNombreSubtipoTramite(solicitud.getSubtipoTramite().getNombre());
        }

        if (solicitud.getModalidadTramite() != null) {
            dto.setIdModalidadTramite(solicitud.getModalidadTramite().getIdModalidadTramite());
            dto.setNombreModalidadTramite(solicitud.getModalidadTramite().getNombre());
        }

        dto.setFechaLimiteCompletar(solicitud.getFechaLimiteCompletar());
        dto.setFechaLimiteProximo(solicitud.getFechaLimiteProximo());
        dto.setFechaUltimoCambioEstado(solicitud.getFechaUltimoCambioEstado());

        return dto;
    }
}