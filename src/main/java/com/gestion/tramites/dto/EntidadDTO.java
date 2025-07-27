package com.gestion.tramites.dto; // Paquete correcto

// Importa las anotaciones de validación si las vas a usar (opcional para ahora)
// import jakarta.validation.constraints.NotBlank;
// import jakarta.validation.constraints.Size;
// import jakarta.validation.constraints.Email;

public class EntidadDTO {

    private Long id;
    // @NotBlank(message = "El nombre es obligatorio")
    // @Size(min = 3, max = 255, message = "El nombre debe tener entre 3 y 255 caracteres")
    private String nombre;
    // @NotBlank(message = "El NIT es obligatorio")
    // @Size(min = 5, max = 20, message = "El NIT debe tener entre 5 y 20 caracteres")
    private String nit;
    private String direccion;
    private String telefono;
    // @Email(message = "Debe ser un correo electrónico válido")
    private String email;
    private String sitioWeb;
    private boolean activo; // Para reflejar el estado activo/inactivo

    // Constructor vacío
    public EntidadDTO() {
    }

    // Constructor completo (puedes añadir más si necesitas)
    public EntidadDTO(Long id, String nombre, String nit, String direccion, String telefono, String email, String sitioWeb, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.nit = nit;
        this.direccion = direccion;
        this.telefono = telefono;
        this.email = email;
        this.sitioWeb = sitioWeb;
        this.activo = activo;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSitioWeb() {
        return sitioWeb;
    }

    public void setSitioWeb(String sitioWeb) {
        this.sitioWeb = sitioWeb;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
