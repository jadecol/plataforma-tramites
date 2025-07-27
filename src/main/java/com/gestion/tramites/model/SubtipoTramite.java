package com.gestion.tramites.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "subtipos_tramite")
public class SubtipoTramite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSubtipoTramite;

    @Column(name = "nombre", nullable = false, unique = true)
    private String nombre; // <-- ¡Asegúrate de que esta propiedad exista!

    // Getters y Setters
    public Long getIdSubtipoTramite() {
        return idSubtipoTramite;
    }

    public void setIdSubtipoTramite(Long idSubtipoTramite) {
        this.idSubtipoTramite = idSubtipoTramite;
    }

    public String getNombre() { // <-- ¡Y este getter!
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
