package com.gestion.tramites.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tipos_tramite")
public class TipoTramite extends BaseTenantEntity { // Added extends BaseTenantEntity

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTipoTramite;

    @Column(name = "nombre", nullable = false, unique = true)
    private String nombre;

    // Removed Entidad field and its getters/setters as they are now in BaseTenantEntity

    // Getters y Setters
    public Long getIdTipoTramite() {
        return idTipoTramite;
    }

    public void setIdTipoTramite(Long idTipoTramite) {
        this.idTipoTramite = idTipoTramite;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // Métodos requeridos por tests legacy
    public Long getId() {
        return this.idTipoTramite;
    }

    public void setId(Long id) {
        this.idTipoTramite = id;
    }

    public void setDescripcion(String descripcion) {
        // Mapear descripcion a nombre para compatibility
        this.nombre = descripcion;
    }

    public String getDescripcion() {
        return this.nombre;
    }

    public void setActivo(boolean activo) {
        // Los tipos de tramite no tienen campo activo, lo ignoramos
    }

    public boolean isActivo() {
        return true; // Siempre activo por defecto
    }
}
