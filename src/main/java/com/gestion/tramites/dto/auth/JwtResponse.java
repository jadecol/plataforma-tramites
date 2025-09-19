package com.gestion.tramites.dto.auth;

public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long idUsuario;
    private String correoElectronico;
    private String rol;
    private String nombreCompleto;

    public JwtResponse(String token, Long idUsuario, String correoElectronico, String rol,
            String nombreCompleto) {
        this.token = token;
        this.idUsuario = idUsuario;
        this.correoElectronico = correoElectronico;
        this.rol = rol;
        this.nombreCompleto = nombreCompleto;
    }

    // Getters y Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
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
}
