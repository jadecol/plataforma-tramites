package com.gestion.tramites.model; // Paquete correcto

import jakarta.persistence.*; // O javax.persistence.* si usas Spring Boot 2.x
import java.util.Objects; // Para el método Objects.equals y hashCode

@Entity
@Table(name = "entidades") // Nombre de la tabla en la base de datos
public class Entidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // O GenerationType.UUID para UUIDs
    private Long id; // Si prefieres UUIDs, cambia a private UUID id; y GenerationType.UUID

    @Column(nullable = false, unique = true)
    private String nombre; // Nombre de la Curaduría/Secretaría

    @Column(nullable = false, unique = true)
    private String nit; // Número de Identificación Tributaria, debe ser único

    @Column(nullable = true) // Puede que al inicio no tengan dirección o sea opcional
    private String direccion;

    @Column(nullable = true) // Teléfono de contacto
    private String telefono;

    @Column(nullable = true) // Correo electrónico de la entidad
    private String email;

    @Column(nullable = true) // Sitio web de la entidad
    private String sitioWeb;

    @Column(nullable = false) // Estado de la entidad (Activa/Inactiva)
    private boolean activo = true; // Por defecto, se crea como activa

    // Constructores
    public Entidad() {}

    public Entidad(String nombre, String nit, String direccion, String telefono, String email,
            String sitioWeb) {
        this.nombre = nombre;
        this.nit = nit;
        this.direccion = direccion;
        this.telefono = telefono;
        this.email = email;
        this.sitioWeb = sitioWeb;
        this.activo = true; // Al crear, siempre activa
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

    // Métodos equals y hashCode (importante para colecciones y JPA)
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Entidad entidad = (Entidad) o;
        return Objects.equals(id, entidad.id); // Solo compara por ID si es persistente
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // Solo usa ID si es persistente
    }

    @Override
    public String toString() {
        return "Entidad{" + "id=" + id + ", nombre='" + nombre + '\'' + ", nit='" + nit + '\''
                + ", direccion='" + direccion + '\'' + ", telefono='" + telefono + '\''
                + ", email='" + email + '\'' + ", sitioWeb='" + sitioWeb + '\'' + ", activo="
                + activo + '}';
    }
}
