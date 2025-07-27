package com.gestion.tramites.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType; // <--- ¡AÑADE ESTA LÍNEA!

// ... el resto de tu clase Usuario ...

@Entity
@Table(name = "usuarios")
public class Usuario {

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
    private String rol; // Ej: Solicitante, Revisor, Administrador

    @Column(name = "contrasena_hash")
    private String contrasenaHash; // Para almacenar la contraseña hasheada

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_ultima_sesion")
    private LocalDateTime fechaUltimaSesion;

    @Column(name = "esta_activo")
    private Boolean estaActivo;

    @Column(name = "matricula_profesional")
    private String matriculaProfesional;

    @Column(name = "experiencia_acreditada")
    private String experienciaAcreditada; // Para revisores o profesionales

    // *** NUEVO CAMPO: Relación con Entidad ***
    @ManyToOne(fetch = FetchType.LAZY) // LAZY para cargar la entidad solo cuando se necesite
    @JoinColumn(name = "id_entidad", nullable = true) // Nombre de la columna FK en la tabla 'usuarios'
    private Entidad entidad; // Vincula el usuario a una Entidad (Curaduría/Secretaría)

    // IMPORTANTE: Asegúrate de añadir un constructor que incluya 'entidad' si lo necesitas,
    // o simplemente usa los setters.

    // Getters y Setters existentes...

    // *** NUEVOS Getters y Setters para 'entidad' ***
    public Entidad getEntidad() {
        return entidad;
    }

    public void setEntidad(Entidad entidad) {
        this.entidad = entidad;
    }


    // Mantén el resto de getters y setters, y si tienes constructores personalizados, actualízalos.
    // Getters y Setters (los que ya tenías)
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

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
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
}
