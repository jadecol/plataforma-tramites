package com.gestion.tramites.config.hibernate;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración central para las definiciones de filtros de Hibernate.
 * Aquí se declara el filtro 'entityFilter' que se usará para el aislamiento de datos multi-tenant.
 */
@Configuration
@FilterDef(name = "entityFilter", parameters = @ParamDef(name = "entityId", type = Long.class))
public class HibernateFilterConfig {
    // Esta clase solo necesita existir para que Spring la detecte
    // y registre la definición del filtro de Hibernate al iniciar.
}
