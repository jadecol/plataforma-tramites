package com.gestion.tramites.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data; // Si usas Lombok, si no, añade getters/setters y constructores manualmente

@Data
public class LoginRequest {
    @NotBlank(message = "El correo electrónico no puede estar vacío")
    @Email(message = "Debe ser un formato de correo electrónico válido")
    private String correoElectronico;

    @NotBlank(message = "La contraseña no puede estar vacía")
    private String contrasena;
}
