package com.gestion.tramites.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tipos_tramite")
public class TipoTramite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTipoTramite;

    @Column(name = "nombre", nullable = false, unique = true)
    private String nombre; // <-- ¡Asegúrate de que esta propiedad exista!

    // Getters y Setters
    public Long getIdTipoTramite() {
        return idTipoTramite;
    }

    public void setIdTipoTramite(Long idTipoTramite) {
        this.idTipoTramite = idTipoTramite;
    }

    public String getNombre() { // <-- ¡Y este getter!
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
