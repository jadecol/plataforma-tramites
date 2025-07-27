package com.gestion.tramites.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor; // Para constructor con todos los args
import lombok.Data; // Para getters y setters automáticos
import lombok.NoArgsConstructor; // Para constructor sin args

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModalidadTramiteDTO {
    private Long idModalidadTramite; // Necesario para respuestas y para PUT

    @NotBlank(message = "El nombre de la modalidad no puede estar vacío")
    @Size(max = 255, message = "El nombre de la modalidad no puede exceder los 255 caracteres")
    private String nombre;
}
