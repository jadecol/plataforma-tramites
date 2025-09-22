package com.gestion.tramites.model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Representa un usuario en la plataforma. Puede ser un administrador, un gestor o un cliente.
 * Hereda la propiedad 'entidad' y el filtro multi-tenant de BaseTenantEntity.
 */
@Entity
@Table(name = "usuarios")
public class Usuario extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @Column(name = "tipo_documento", nullable = false)
    private String tipoDocumento;

    @Column(name = "numero_documento", unique = true, nullable = false)
    private String numeroDocumento;

    @Column(name = "correo_electronico", unique = true, nullable = false)
    private String correoElectronico;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "rol", nullable = false)
    @Enumerated(EnumType.STRING)
    private Rol rol;

    @Column(name = "contrasena_hash")
    private String contrasenaHash;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_ultima_sesion")
    private LocalDateTime fechaUltimaSesion;

    @Column(name = "esta_activo")
    private Boolean estaActivo;

    @Column(name = "matricula_profesional")
    private String matriculaProfesional;

    @Column(name = "experiencia_acreditada")
    private String experienciaAcreditada;

    // Enum para roles de usuario
    public enum Rol {
        ADMIN_GLOBAL, ADMIN_ENTIDAD, VENTANILLA_UNICA, REVISOR, SOLICITANTE
    }

    // El campo 'entidad' y sus getters/setters son heredados de BaseTenantEntity

    // Getters y Setters
    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
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

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public void setContrasenaHash(String contrasenaHash) {
        this.contrasenaHash = contrasenaHash;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaUltimaSesion() {
        return fechaUltimaSesion;
    }

    public void setFechaUltimaSesion(LocalDateTime fechaUltimaSesion) {
        this.fechaUltimaSesion = fechaUltimaSesion;
    }

    public Boolean getEstaActivo() {
        return estaActivo;
    }

    public void setEstaActivo(Boolean estaActivo) {
        this.estaActivo = estaActivo;
    }

    public String getMatriculaProfesional() {
        return matriculaProfesional;
    }

    public void setMatriculaProfesional(String matriculaProfesional) {
        this.matriculaProfesional = matriculaProfesional;
    }

    public String getExperienciaAcreditada() {
        return experienciaAcreditada;
    }

    public void setExperienciaAcreditada(String experienciaAcreditada) {
        this.experienciaAcreditada = experienciaAcreditada;
    }

    // Métodos adicionales requeridos por servicios
    public Long getId() {
        return this.idUsuario;
    }

    public boolean isActivo() {
        return this.estaActivo != null ? this.estaActivo : false;
    }

    public void setActivo(boolean activo) {
        this.estaActivo = activo;
    }

    public String getContrasena() {
        return this.contrasenaHash;
    }

    public void setContrasena(String contrasena) {
        this.contrasenaHash = contrasena;
    }

    // Métodos requeridos por tests legacy
    public void setUsername(String username) {
        this.correoElectronico = username;
    }

    public String getUsername() {
        return this.correoElectronico;
    }

    public void setPassword(String password) {
        this.contrasenaHash = password;
    }

    public String getPassword() {
        return this.contrasenaHash;
    }

    public void setId(Long id) {
        this.idUsuario = id;
    }

    // Método para compatibility con tests que pasan String en lugar de Rol
    public void setRol(String rolString) {
        try {
            this.rol = Usuario.Rol.valueOf(rolString);
        } catch (IllegalArgumentException e) {
            // Default a SOLICITANTE si el string no es válido
            this.rol = Usuario.Rol.SOLICITANTE;
        }
    }
}