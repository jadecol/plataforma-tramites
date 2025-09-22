package com.gestion.tramites.model;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

/**
 * Clase base abstracta para todas las entidades que deben ser aisladas por entidad (tenant).
 *
 * Utiliza la anotación @MappedSuperclass de JPA para que sus campos sean heredados por las
 * subclases sin necesidad de crear una tabla para esta clase.
 *
 * Centraliza la definición del filtro de Hibernate (@FilterDef) y su aplicación (@Filter),
 * garantizando que cualquier entidad que herede de esta clase aplicará automáticamente
 * el filtro de seguridad multi-tenant.
 */
@MappedSuperclass
@FilterDef(name = "entityFilter", parameters = @ParamDef(name = "entityId", type = Long.class))
@Filter(name = "entityFilter", condition = "id_entidad = :entityId")
public abstract class BaseTenantEntity {

    /**
     * La entidad (tenant) a la que pertenece este registro.
     * Es la columna clave para el aislamiento de datos.
     * La carga es LAZY para optimizar el rendimiento.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_entidad", nullable = true)
    private Entidad entidad;

    // Getters y Setters

    public Entidad getEntidad() {
        return entidad;
    }

    public void setEntidad(Entidad entidad) {
        this.entidad = entidad;
    }
}
