package com.gestion.tramites.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "modalidades_tramite")
public class ModalidadTramite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idModalidadTramite;

    @Column(name = "nombre", nullable = false, unique = true)
    private String nombre; // <-- ¡Aquí está! La propiedad es 'nombre'

    // Getters y Setters
    public Long getIdModalidadTramite() {
        return idModalidadTramite;
    }

    public void setIdModalidadTramite(Long idModalidadTramite) {
        this.idModalidadTramite = idModalidadTramite;
    }

    public String getNombre() { // <-- Su getter es getNombre()
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
