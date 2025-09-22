package com.gestion.tramites.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * Post-Procesador para manejo de dependencias JPA/Flyway
 * TEMPORALMENTE DESHABILITADO para resolver dependencia circular
 */
//@Component
public class JpaDependsOnFlywayPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // Temporalmente deshabilitado para resolver dependencia circular
        // La configuración de Flyway en application.properties debería manejar el orden correcto
    }
}
