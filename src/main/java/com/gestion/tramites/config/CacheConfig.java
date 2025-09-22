package com.gestion.tramites.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de caché para optimizar consultas públicas del Portal Ciudadano
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configuración del gestor de caché para consultas públicas
     * Utiliza caché en memoria para mejorar el rendimiento de las consultas frecuentes
     */
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();

        // Configurar cachés específicos para Portal Ciudadano
        cacheManager.setCacheNames(java.util.Arrays.asList(
            "consultaPublica",          // Consultas por número de radicación
            "consultaPublicaEmail",     // Consultas por email
            "tramitesRecientes",        // Trámites recientes por entidad
            "estadisticasPublicas"      // Estadísticas públicas de entidades
        ));

        return cacheManager;
    }
}