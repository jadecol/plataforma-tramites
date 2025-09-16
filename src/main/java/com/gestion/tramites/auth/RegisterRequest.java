package com.gestion.tramites.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "El correo electrónico no puede estar vacío")
    @Email(message = "Debe ser un formato de correo electrónico válido")
    private String correoElectronico;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String contrasena;
    
    @NotBlank(message = "El rol no puede estar vacío")
    private String rol;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombreCompleto;

    private String tipoDocumento;

    @NotBlank(message = "El número de documento no puede estar vacío")
    private String numeroDocumento;

    // --- GETTERS Y SETTERS ---

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }
}