package com.gestion.tramites.usuario;

import java.time.LocalDateTime;

import com.gestion.tramites.entidad.dto.EntidadDTO;

public class UsuarioResponseDTO {

    private Long idUsuario;
    private String nombreCompleto;
    private String tipoDocumento;
    private String numeroDocumento;
    private String correoElectronico;
    private String telefono;
    private String rol;
    // No incluir contrasenaHash en la respuesta por seguridad
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimaSesion;
    private Boolean estaActivo;
    private String matriculaProfesional;
    private String experienciaAcreditada;

    // *** NUEVO CAMPO: EntidadDTO para la respuesta ***
    private EntidadDTO entidad; // Incluir los datos de la entidad asociada

    // Constructor, Getters y Setters...
    // Asegúrate de añadir los getters y setters para entidad

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public String getCorreoElectronico() { return correoElectronico; }
    public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaUltimaSesion() { return fechaUltimaSesion; }
    public void setFechaUltimaSesion(LocalDateTime fechaUltimaSesion) { this.fechaUltimaSesion = fechaUltimaSesion; }
    public Boolean getEstaActivo() { return estaActivo; }
    public void setEstaActivo(Boolean estaActivo) { this.estaActivo = estaActivo; }
    public String getMatriculaProfesional() { return matriculaProfesional; }
    public void setMatriculaProfesional(String matriculaProfesional) { this.matriculaProfesional = matriculaProfesional; }
    public String getExperienciaAcreditada() { return experienciaAcreditada; }
    public void setExperienciaAcreditada(String experienciaAcreditada) { this.experienciaAcreditada = experienciaAcreditada; }

    // NUEVO Getter y Setter para entidad (EntidadDTO)
    public EntidadDTO getEntidad() {
        return entidad;
    }

    public void setEntidad(EntidadDTO entidad) {
        this.entidad = entidad;
    }
}
