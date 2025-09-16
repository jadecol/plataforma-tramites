package com.gestion.tramites.usuario;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;

public class UsuarioDTO {

    private Long idUsuario;
    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombreCompleto;
    @NotBlank(message = "El tipo de documento es obligatorio")
    private String tipoDocumento;
    @NotBlank(message = "El numero de documento es obligatorio")
    private String numeroDocumento;
    @Email(message = "Debe ser un correo electronico valido")
    @NotBlank(message = "El correo electronico es obligatorio")
    private String correoElectronico;
    private String telefono;
    @NotBlank(message = "El rol es obligatorio")
    private String rol;
    @NotBlank(message = "La contrasena es obligatoria para nuevos usuarios")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
             message = "La contrasena debe tener al menos 8 caracteres, incluyendo mayusculas, minusculas, numeros y un caracter especial.")
    private String contrasenaHash; // Este es el campo que se usará para la contraseña
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimaSesion;
    @NotNull(message = "El estado 'activo' es obligatorio")
    private Boolean estaActivo;
    private String matriculaProfesional;
    private String experienciaAcreditada;

    @NotNull(message = "El ID de la entidad es obligatorio")
    private Long idEntidad;

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

	public Long getIdEntidad() {
		return idEntidad;
	}

	public void setIdEntidad(Long idEntidad) {
		this.idEntidad = idEntidad;
	}

    // **AQUI NO DEBE HABER NINGUN GETTER O SETTER MANUALMENTE ESCRITO POR AHORA**  
}