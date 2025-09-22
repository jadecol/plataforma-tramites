package com.gestion.tramites.service;

import com.gestion.tramites.exception.ResourceNotFoundException;
import com.gestion.tramites.model.Tramite;
import com.gestion.tramites.repository.TramiteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para seguimiento público de trámites sin autenticación
 * Permite a los ciudadanos consultar el estado de sus trámites
 */
@Service
@Transactional(readOnly = true)
public class SeguimientoService {

    private static final Logger logger = LoggerFactory.getLogger(SeguimientoService.class);

    @Autowired
    private TramiteRepository tramiteRepository;

    /**
     * Consulta un trámite por número de radicación (acceso público)
     */
    @Cacheable(value = "consultaPublica", key = "#numeroRadicacion")
    public ConsultaTramitePublico consultarPorNumeroRadicacion(String numeroRadicacion) {
        logger.info("Consulta pública por número de radicación: {}", numeroRadicacion);

        if (numeroRadicacion == null || numeroRadicacion.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de radicación es obligatorio");
        }

        String numeroLimpio = limpiarNumeroRadicacion(numeroRadicacion);

        Optional<Tramite> tramite = tramiteRepository.findByNumeroRadicacion(numeroLimpio);

        if (tramite.isEmpty()) {
            throw new ResourceNotFoundException("Trámite", "número de radicación", numeroLimpio);
        }

        ConsultaTramitePublico consulta = construirConsultaPublica(tramite.get());

        logger.debug("Consulta pública exitosa para radicación: {}", numeroRadicacion);
        return consulta;
    }

    /**
     * Consulta trámites por email del solicitante (acceso público)
     */
    @Cacheable(value = "consultaPublicaEmail", key = "#emailSolicitante")
    public List<ConsultaTramitePublico> consultarPorEmailSolicitante(String emailSolicitante) {
        logger.info("Consulta pública por email: {}", emailSolicitante);

        if (emailSolicitante == null || emailSolicitante.trim().isEmpty()) {
            throw new IllegalArgumentException("El email del solicitante es obligatorio");
        }

        String emailLimpio = emailSolicitante.trim().toLowerCase();

        // Buscar trámites por email del solicitante
        List<Tramite> tramites = tramiteRepository.findBySolicitanteCorreoElectronicoOrderByFechaRadicacionDesc(emailLimpio);

        if (tramites.isEmpty()) {
            logger.info("No se encontraron trámites para el email: {}", emailLimpio);
            return List.of();
        }

        List<ConsultaTramitePublico> consultas = tramites.stream()
                .map(this::construirConsultaPublica)
                .collect(Collectors.toList());

        logger.info("Encontrados {} trámites para email: {}", consultas.size(), emailLimpio);
        return consultas;
    }

    /**
     * Consulta trámites recientes por entidad (para mostrar actividad pública)
     */
    @Cacheable(value = "tramitesRecientes", key = "#entidadId + '_' + #limite")
    public List<ResumenTramitePublico> consultarTramitesRecientesPorEntidad(Long entidadId, int limite) {
        logger.debug("Consultando {} trámites recientes para entidad: {}", limite, entidadId);

        if (limite <= 0 || limite > 50) {
            limite = 10; // Límite por defecto y máximo de seguridad
        }

        LocalDate fechaLimite = LocalDate.now().minusDays(30); // Solo últimos 30 días

        List<Tramite> tramitesRecientes = tramiteRepository
                .findByEntidadIdAndFechaRadicacionAfterOrderByFechaRadicacionDesc(entidadId, fechaLimite)
                .stream()
                .limit(limite)
                .collect(Collectors.toList());

        return tramitesRecientes.stream()
                .map(this::construirResumenPublico)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene estadísticas públicas de una entidad
     */
    @Cacheable(value = "estadisticasPublicas", key = "#entidadId")
    public EstadisticasPublicasEntidad obtenerEstadisticasPublicas(Long entidadId) {
        logger.debug("Obteniendo estadísticas públicas para entidad: {}", entidadId);

        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate inicioAno = LocalDate.now().withDayOfYear(1);

        // Estadísticas del mes actual
        long tramitesMesActual = tramiteRepository.countByEntidadIdAndFechaRadicacionAfter(entidadId, inicioMes);

        // Estadísticas del año actual
        long tramitesAnoActual = tramiteRepository.countByEntidadIdAndFechaRadicacionAfter(entidadId, inicioAno);

        // Promedio de días de procesamiento (aproximado)
        double promedioDiasProceso = calcularPromedioDiasProcesamiento(entidadId);

        return new EstadisticasPublicasEntidad(
                entidadId,
                tramitesMesActual,
                tramitesAnoActual,
                promedioDiasProceso
        );
    }

    /**
     * Validar acceso público a un trámite (verificación adicional de seguridad)
     */
    public boolean validarAccesoPublico(String numeroRadicacion, String emailSolicitante) {
        if (numeroRadicacion == null || emailSolicitante == null) {
            return false;
        }

        Optional<Tramite> tramite = tramiteRepository.findByNumeroRadicacion(numeroRadicacion.trim());

        return tramite.isPresent() &&
               tramite.get().getSolicitante() != null &&
               emailSolicitante.trim().toLowerCase()
                       .equals(tramite.get().getSolicitante().getCorreoElectronico().toLowerCase());
    }

    // Métodos auxiliares privados

    private String limpiarNumeroRadicacion(String numeroRadicacion) {
        return numeroRadicacion.trim().toUpperCase().replaceAll("\\s+", "");
    }

    private ConsultaTramitePublico construirConsultaPublica(Tramite tramite) {
        ConsultaTramitePublico consulta = new ConsultaTramitePublico();

        // Información básica (segura para mostrar públicamente)
        consulta.setNumeroRadicacion(tramite.getNumeroRadicacion());
        consulta.setObjetoTramite(tramite.getObjetoTramite());
        consulta.setFechaRadicacion(tramite.getFechaRadicacion());
        consulta.setEstadoActual(tramite.getEstadoActual());

        // Información de la entidad
        if (tramite.getEntidad() != null) {
            consulta.setNombreEntidad(tramite.getEntidad().getNombre());
            consulta.setTelefonoEntidad(tramite.getEntidad().getTelefono());
            consulta.setEmailEntidad(tramite.getEntidad().getEmail());
        }

        // Información del tipo de trámite
        if (tramite.getTipoTramite() != null) {
            consulta.setTipoTramite(tramite.getTipoTramite().getNombre());
            consulta.setDescripcionTipoTramite("Tipo de trámite: " + tramite.getTipoTramite().getNombre());
        }

        // Calcular días transcurridos
        consulta.setDiasTranscurridos(calcularDiasTranscurridos(tramite.getFechaRadicacion()));

        // Estado descriptivo para ciudadanos
        consulta.setDescripcionEstado(obtenerDescripcionEstadoCiudadano(tramite.getEstadoActual()));

        // Observaciones públicas (filtradas)
        consulta.setObservacionesPublicas(filtrarObservacionesPublicas(tramite.getComentariosRevisor()));

        return consulta;
    }

    private ResumenTramitePublico construirResumenPublico(Tramite tramite) {
        ResumenTramitePublico resumen = new ResumenTramitePublico();

        resumen.setNumeroRadicacion(tramite.getNumeroRadicacion());
        resumen.setTipoTramite(tramite.getTipoTramite() != null ? tramite.getTipoTramite().getNombre() : "");
        resumen.setFechaRadicacion(tramite.getFechaRadicacion());
        resumen.setEstadoActual(tramite.getEstadoActual());
        resumen.setDiasTranscurridos(calcularDiasTranscurridos(tramite.getFechaRadicacion()));

        return resumen;
    }

    private int calcularDiasTranscurridos(LocalDate fechaRadicacion) {
        return (int) java.time.temporal.ChronoUnit.DAYS.between(fechaRadicacion, LocalDate.now());
    }

    private String obtenerDescripcionEstadoCiudadano(Tramite.EstadoTramite estado) {
        return switch (estado) {
            case RADICADO -> "Su trámite ha sido recibido y está en cola para revisión";
            case EN_REVISION -> "Su trámite está siendo revisado por nuestro equipo técnico";
            case PENDIENTE_DOCUMENTOS -> "Su trámite requiere información adicional o correcciones";
            case APROBADO -> "¡Felicitaciones! Su trámite ha sido aprobado";
            case RECHAZADO -> "Su trámite no pudo ser aprobado. Consulte las observaciones";
            case ARCHIVADO -> "Su trámite ha sido archivado";
            default -> "Estado del trámite: " + estado.name();
        };
    }

    private String filtrarObservacionesPublicas(String observaciones) {
        if (observaciones == null || observaciones.trim().isEmpty()) {
            return "Sin observaciones públicas";
        }

        // Filtrar información sensible (ejemplo básico)
        String filtradas = observaciones
                .replaceAll("(?i)(contraseña|password|clave)", "[INFORMACIÓN CONFIDENCIAL]")
                .replaceAll("(?i)(interno|privado)", "[USO INTERNO]");

        return filtradas.length() > 500 ? filtradas.substring(0, 500) + "..." : filtradas;
    }

    private double calcularPromedioDiasProcesamiento(Long entidadId) {
        // Implementación simplificada - en producción se haría con query específica
        LocalDate fechaLimite = LocalDate.now().minusMonths(3);
        List<Tramite> tramitesCompletados = tramiteRepository
                .findByEntidadIdAndFechaRadicacionAfterAndEstadoActualIn(
                        entidadId,
                        fechaLimite,
                        List.of(Tramite.EstadoTramite.APROBADO, Tramite.EstadoTramite.RECHAZADO)
                );

        if (tramitesCompletados.isEmpty()) {
            return 15.0; // Promedio por defecto
        }

        double totalDias = tramitesCompletados.stream()
                .mapToInt(t -> calcularDiasTranscurridos(t.getFechaRadicacion()))
                .average()
                .orElse(15.0);

        return Math.round(totalDias * 10.0) / 10.0; // Redondear a 1 decimal
    }

    // DTOs para respuestas públicas

    public static class ConsultaTramitePublico {
        private String numeroRadicacion;
        private String objetoTramite;
        private LocalDate fechaRadicacion;
        private Tramite.EstadoTramite estadoActual;
        private String descripcionEstado;
        private String nombreEntidad;
        private String telefonoEntidad;
        private String emailEntidad;
        private String tipoTramite;
        private String descripcionTipoTramite;
        private int diasTranscurridos;
        private String observacionesPublicas;

        // Getters y Setters
        public String getNumeroRadicacion() { return numeroRadicacion; }
        public void setNumeroRadicacion(String numeroRadicacion) { this.numeroRadicacion = numeroRadicacion; }

        public String getObjetoTramite() { return objetoTramite; }
        public void setObjetoTramite(String objetoTramite) { this.objetoTramite = objetoTramite; }

        public LocalDate getFechaRadicacion() { return fechaRadicacion; }
        public void setFechaRadicacion(LocalDate fechaRadicacion) { this.fechaRadicacion = fechaRadicacion; }

        public Tramite.EstadoTramite getEstadoActual() { return estadoActual; }
        public void setEstadoActual(Tramite.EstadoTramite estadoActual) { this.estadoActual = estadoActual; }

        public String getDescripcionEstado() { return descripcionEstado; }
        public void setDescripcionEstado(String descripcionEstado) { this.descripcionEstado = descripcionEstado; }

        public String getNombreEntidad() { return nombreEntidad; }
        public void setNombreEntidad(String nombreEntidad) { this.nombreEntidad = nombreEntidad; }

        public String getTelefonoEntidad() { return telefonoEntidad; }
        public void setTelefonoEntidad(String telefonoEntidad) { this.telefonoEntidad = telefonoEntidad; }

        public String getEmailEntidad() { return emailEntidad; }
        public void setEmailEntidad(String emailEntidad) { this.emailEntidad = emailEntidad; }

        public String getTipoTramite() { return tipoTramite; }
        public void setTipoTramite(String tipoTramite) { this.tipoTramite = tipoTramite; }

        public String getDescripcionTipoTramite() { return descripcionTipoTramite; }
        public void setDescripcionTipoTramite(String descripcionTipoTramite) { this.descripcionTipoTramite = descripcionTipoTramite; }

        public int getDiasTranscurridos() { return diasTranscurridos; }
        public void setDiasTranscurridos(int diasTranscurridos) { this.diasTranscurridos = diasTranscurridos; }

        public String getObservacionesPublicas() { return observacionesPublicas; }
        public void setObservacionesPublicas(String observacionesPublicas) { this.observacionesPublicas = observacionesPublicas; }
    }

    public static class ResumenTramitePublico {
        private String numeroRadicacion;
        private String tipoTramite;
        private LocalDate fechaRadicacion;
        private Tramite.EstadoTramite estadoActual;
        private int diasTranscurridos;

        // Getters y Setters
        public String getNumeroRadicacion() { return numeroRadicacion; }
        public void setNumeroRadicacion(String numeroRadicacion) { this.numeroRadicacion = numeroRadicacion; }

        public String getTipoTramite() { return tipoTramite; }
        public void setTipoTramite(String tipoTramite) { this.tipoTramite = tipoTramite; }

        public LocalDate getFechaRadicacion() { return fechaRadicacion; }
        public void setFechaRadicacion(LocalDate fechaRadicacion) { this.fechaRadicacion = fechaRadicacion; }

        public Tramite.EstadoTramite getEstadoActual() { return estadoActual; }
        public void setEstadoActual(Tramite.EstadoTramite estadoActual) { this.estadoActual = estadoActual; }

        public int getDiasTranscurridos() { return diasTranscurridos; }
        public void setDiasTranscurridos(int diasTranscurridos) { this.diasTranscurridos = diasTranscurridos; }
    }

    public static class EstadisticasPublicasEntidad {
        private final Long entidadId;
        private final long tramitesMesActual;
        private final long tramitesAnoActual;
        private final double promedioDiasProcesamiento;

        public EstadisticasPublicasEntidad(Long entidadId, long tramitesMesActual,
                                         long tramitesAnoActual, double promedioDiasProcesamiento) {
            this.entidadId = entidadId;
            this.tramitesMesActual = tramitesMesActual;
            this.tramitesAnoActual = tramitesAnoActual;
            this.promedioDiasProcesamiento = promedioDiasProcesamiento;
        }

        // Getters
        public Long getEntidadId() { return entidadId; }
        public long getTramitesMesActual() { return tramitesMesActual; }
        public long getTramitesAnoActual() { return tramitesAnoActual; }
        public double getPromedioDiasProcesamiento() { return promedioDiasProcesamiento; }
    }
}