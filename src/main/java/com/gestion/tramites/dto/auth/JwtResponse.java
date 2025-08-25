package com.gestion.tramites.dto.auth;

public class JwtResponse {
    private String token;
    private String tipo = "Bearer";
    private Long id;
    private String correoElectronico;
    private String rol;
    private String nombreCompleto;

    // Este es el constructor que tu AuthService necesita
    public JwtResponse(String token, Long id, String correoElectronico, String rol, String nombreCompleto) {
        this.token = token;
        this.id = id;
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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