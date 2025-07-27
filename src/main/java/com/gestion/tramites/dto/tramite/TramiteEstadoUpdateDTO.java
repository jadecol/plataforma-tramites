package com.gestion.tramites.dto.tramite;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TramiteEstadoUpdateDTO {
    @NotBlank(message = "El estado no puede estar vacío")
    private String nuevoEstado; // Será un String, se mapeará al enum en el servicio
    private String comentariosRevisor; // Opcional
}
