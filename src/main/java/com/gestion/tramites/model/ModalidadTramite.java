package com.gestion.tramites.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "modalidades_tramite")
public class ModalidadTramite extends BaseTenantEntity { // Added extends BaseTenantEntity

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idModalidadTramite;

    @Column(name = "nombre", nullable = false, unique = true)
    private String nombre;

    // Removed Entidad field and its getters/setters as they are now in BaseTenantEntity

    // Getters y Setters
    public Long getIdModalidadTramite() {
        return idModalidadTramite;
    }

    public void setIdModalidadTramite(Long idModalidadTramite) {
        this.idModalidadTramite = idModalidadTramite;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
