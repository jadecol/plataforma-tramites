package com.gestion.tramites.usuario;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequestDTO {

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 255, message = "El nombre completo no puede exceder los 255 caracteres")
    private String nombreCompleto;

    @NotBlank(message = "El tipo de documento es obligatorio")
    @Size(max = 50, message = "El tipo de documento no puede exceder los 50 caracteres")
    private String tipoDocumento;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(max = 50, message = "El número de documento no puede exceder los 50 caracteres")
    private String numeroDocumento;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico debe ser válido")
    @Size(max = 255, message = "El correo electrónico no puede exceder los 255 caracteres")
    private String correoElectronico;

    @Size(max = 50, message = "El teléfono no puede exceder los 50 caracteres")
    private String telefono;

    @NotBlank(message = "El rol es obligatorio")
    @Size(max = 50, message = "El rol no puede exceder los 50 caracteres")
    private String rol;

//    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasena;

    @Size(max = 100, message = "La matrícula profesional no puede exceder los 100 caracteres")
    private String matriculaProfesional;

    @Size(max = 255, message = "La experiencia acreditada no puede exceder los 255 caracteres")
    private String experienciaAcreditada;

    // Añade este campo para permitir actualizar el estado de activo del usuario
    private Boolean estaActivo; // <-- Añade esta línea
}
