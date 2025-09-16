package com.gestion.tramites.tramite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TramiteRequestDTO {
    // No incluir idTramite aquí, ya que se genera automáticamente

    @NotNull(message = "El ID del solicitante no puede ser nulo")
    private Long idSolicitante; // ID del Usuario solicitante

    @NotNull(message = "El ID de la entidad no puede ser nulo")
    private Long idEntidad; // ID de la Entidad responsable

    // Opcional: El revisor se asigna después, pero podría venir en una actualización
    private Long idRevisor; // ID del Usuario revisor (puede ser nulo)

    @NotBlank(message = "El nombre del trámite no puede estar vacío")
    private String nombreTramite;

    @NotBlank(message = "La descripción del trámite no puede estar vacía")
    private String descripcion;

    // El estado inicial se manejará en la entidad o servicio, pero puede ser actualizable
    // @NotNull(message = "El estado del trámite no puede ser nulo")
    // private String estado; // Podría ser un String o el Enum directamente

    // Campos para actualizaciones de estado/comentarios
    private String estado; // Para actualizaciones, se validará que sea un EstadoTramite válido
    private String comentariosRevisor;
    private String numeroExpediente;
}
