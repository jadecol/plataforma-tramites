package com.gestion.tramites.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * Solución definitiva para la dependencia circular entre JPA y Flyway en contextos de prueba.
 * Este Post-Procesador intercepta la configuración de beans de Spring ANTES de que se creen.
 * Busca la definición del 'entityManagerFactory' de JPA y le añade programáticamente
 * una dependencia explícita del inicializador de Flyway ('flywayInitializer').
 *
 * Esto garantiza que Flyway SIEMPRE se ejecutará antes que JPA, rompiendo el ciclo.
 */
@Component
public class JpaDependsOnFlywayPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        try {
            String[] jpaBeanNames = beanFactory.getBeanNamesForAnnotation(jakarta.persistence.PersistenceContext.class);
            for (String beanName : jpaBeanNames) {
                beanFactory.getBeanDefinition(beanName).setDependsOn("flywayInitializer");
            }

            // Ataque directo al bean problemático
            beanFactory.getBeanDefinition("entityManagerFactory").setDependsOn("flywayInitializer");
        } catch (Exception e) {
            // Ignorar si los beans no se encuentran, aunque deberían estar presentes.
            // Esto hace que el procesador sea seguro si la configuración de JPA cambia.
        }
    }
}
