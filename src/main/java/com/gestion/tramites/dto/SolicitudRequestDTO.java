package com.gestion.tramites.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor; // Si no usas Lombok, puedes quitar esta línea y añadir constructores manuales
import lombok.Data;              // Si no usas Lombok, puedes quitar esta línea y añadir getters/setters manuales
import lombok.NoArgsConstructor;   // Si no usas Lombok, puedes quitar esta línea y añadir constructores manuales

@Data // Mantendremos @Data por ahora, si sigue dando error de getters/setters, lo quitamos
@AllArgsConstructor
@NoArgsConstructor
public class SolicitudRequestDTO {

    @NotNull(message = "El ID del solicitante es obligatorio")
    private Long idSolicitante;

    @NotNull(message = "El ID del tipo de trámite es obligatorio")
    private Long idTipoTramite;

    private Long idSubtipoTramite; // Puede ser nulo

    private Long idModalidadTramite; // Puede ser nulo

    private Long idRevisorAsignado; // Puede ser nulo

    @NotBlank(message = "El número de radicación es obligatorio")
    @Size(max = 255, message = "El número de radicación no debe exceder 255 caracteres")
    private String numeroRadicacion;

    @Size(max = 255, message = "La dirección del inmueble no debe exceder 255 caracteres")
    private String direccionInmueble;

    @Size(max = 1000, message = "La descripción del proyecto no debe exceder 1000 caracteres")
    private String descripcionProyecto; // Nuevo nombre

    @Size(max = 255, message = "El objeto del trámite no debe exceder 255 caracteres")
    private String objetoTramite;

    @Size(max = 50, message = "La condición de radicación no debe exceder 50 caracteres")
    private String condicionRadicacion;

    private String fechaLimiteProximo;
    private String fechaLimiteCompletar;
}
