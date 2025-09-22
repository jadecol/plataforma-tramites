package com.gestion.tramites.service;

import com.gestion.tramites.exception.ResourceNotFoundException;
import com.gestion.tramites.model.ConsecutivoRadicacion;
import com.gestion.tramites.model.Entidad;
import com.gestion.tramites.model.Tramite;
import com.gestion.tramites.model.TipoTramite;
import com.gestion.tramites.model.Usuario;
import com.gestion.tramites.repository.ConsecutivoRadicacionRepository;
import com.gestion.tramites.repository.EntidadRepository;
import com.gestion.tramites.repository.TramiteRepository;
import com.gestion.tramites.repository.TipoTramiteRepository;
import com.gestion.tramites.repository.UsuarioRepository;
import com.gestion.tramites.service.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RadicacionService {

    private static final Logger logger = LoggerFactory.getLogger(RadicacionService.class);

    @Autowired
    private ConsecutivoRadicacionRepository consecutivoRepository;

    @Autowired
    private TramiteRepository tramiteRepository;

    @Autowired
    private EntidadRepository entidadRepository;

    @Autowired
    private ValidacionRadicacionService validacionService;

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private TipoTramiteRepository tipoTramiteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Genera automáticamente el siguiente número de radicación para una entidad
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public String generarSiguienteNumeroRadicacion(Long entidadId) {
        logger.info("Generando siguiente número de radicación para entidad: {}", entidadId);

        Entidad entidad = entidadRepository.findById(entidadId)
                .orElseThrow(() -> new ResourceNotFoundException("Entidad", "id", entidadId));

        if (!entidad.isActivo()) {
            throw new IllegalStateException("No se puede radicar en una entidad inactiva");
        }

        ConsecutivoRadicacion.TipoEntidadRadicacion tipoEntidad = validacionService.determinarTipoEntidad(entidad);
        int anoActual = LocalDate.now().getYear();

        ConsecutivoRadicacion consecutivo = obtenerOCrearConsecutivo(entidad, tipoEntidad, anoActual);
        String numeroRadicacion = consecutivo.incrementarYGenerar();
        consecutivoRepository.save(consecutivo);

        logger.info("Número de radicación generado: {} para entidad: {}", numeroRadicacion, entidad.getNombre());
        return numeroRadicacion;
    }

    /**
     * Radica un trámite asignando automáticamente el número de radicación
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public SolicitudRadicacion radicarTramite(SolicitudRadicacionTramite solicitud) {
        logger.info("Iniciando radicación de trámite para entidad: {}", solicitud.getEntidadId());

        validarSolicitudRadicacion(solicitud);
        String numeroRadicacion = generarSiguienteNumeroRadicacion(solicitud.getEntidadId());
        CustomUserDetails currentUser = getCurrentUser();

        Tramite tramite = crearTramiteDesdeRadicacion(solicitud, numeroRadicacion, currentUser);
        Tramite tramiteGuardado = tramiteRepository.save(tramite);

        SolicitudRadicacion radicacion = new SolicitudRadicacion();
        radicacion.setIdTramite(tramiteGuardado.getIdTramite());
        radicacion.setNumeroRadicacion(numeroRadicacion);
        radicacion.setFechaRadicacion(tramiteGuardado.getFechaRadicacion());
        radicacion.setEntidadId(solicitud.getEntidadId());
        radicacion.setTipoTramiteId(solicitud.getTipoTramiteId());
        radicacion.setObjetoTramite(solicitud.getObjetoTramite());
        radicacion.setEstado(tramiteGuardado.getEstadoActual());
        radicacion.setSolicitanteEmail(solicitud.getSolicitanteEmail());

        enviarNotificacionRadicacion(radicacion, tramiteGuardado);

        logger.info("Trámite radicado exitosamente: {} (ID: {})", numeroRadicacion, tramiteGuardado.getIdTramite());
        return radicacion;
    }

    /**
     * Obtiene estadísticas de radicación para una entidad
     */
    @Transactional(readOnly = true)
    public EstadisticasRadicacion obtenerEstadisticasRadicacion(Long entidadId, Integer ano) {
        Entidad entidad = entidadRepository.findById(entidadId)
                .orElseThrow(() -> new ResourceNotFoundException("Entidad", "id", entidadId));

        if (ano == null) {
            ano = LocalDate.now().getYear();
        }

        List<ConsecutivoRadicacion> consecutivos =
                consecutivoRepository.findByEntidadAndAnoAndActivoTrueOrderByTipoEntidad(entidad, ano);

        EstadisticasRadicacion estadisticas = new EstadisticasRadicacion();
        estadisticas.setEntidadId(entidadId);
        estadisticas.setNombreEntidad(entidad.getNombre());
        estadisticas.setAno(ano);

        for (ConsecutivoRadicacion consecutivo : consecutivos) {
            if (consecutivo.getTipoEntidad() == ConsecutivoRadicacion.TipoEntidadRadicacion.SECRETARIA) {
                estadisticas.setRadicacionesSecretaria(consecutivo.getUltimoConsecutivo());
            } else if (consecutivo.getTipoEntidad() == ConsecutivoRadicacion.TipoEntidadRadicacion.CURADURIA) {
                estadisticas.setRadicacionesCuraduria(consecutivo.getUltimoConsecutivo());
            }
        }

        estadisticas.calcularTotales();
        return estadisticas;
    }

    private ConsecutivoRadicacion obtenerOCrearConsecutivo(Entidad entidad,
                                                          ConsecutivoRadicacion.TipoEntidadRadicacion tipoEntidad,
                                                          int ano) {
        Optional<ConsecutivoRadicacion> consecutivoExistente =
                consecutivoRepository.findByEntidadAndTipoAndAnoWithLock(entidad, tipoEntidad, ano);

        if (consecutivoExistente.isPresent()) {
            return consecutivoExistente.get();
        }

        ConsecutivoRadicacion nuevoConsecutivo = new ConsecutivoRadicacion();
        nuevoConsecutivo.setEntidad(entidad);
        nuevoConsecutivo.setCodigoDane(entidad.getCodigoDane());
        nuevoConsecutivo.setTipoEntidad(tipoEntidad);
        nuevoConsecutivo.setAno(ano);
        nuevoConsecutivo.setUltimoConsecutivo(0);
        nuevoConsecutivo.setActivo(true);

        return consecutivoRepository.save(nuevoConsecutivo);
    }

    private void validarSolicitudRadicacion(SolicitudRadicacionTramite solicitud) {
        if (solicitud.getEntidadId() == null) {
            throw new IllegalArgumentException("ID de entidad es obligatorio");
        }
        if (solicitud.getTipoTramiteId() == null) {
            throw new IllegalArgumentException("ID de tipo de trámite es obligatorio");
        }
        if (solicitud.getObjetoTramite() == null || solicitud.getObjetoTramite().trim().isEmpty()) {
            throw new IllegalArgumentException("Objeto del trámite es obligatorio");
        }
        if (solicitud.getSolicitanteEmail() == null || solicitud.getSolicitanteEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email del solicitante es obligatorio");
        }
    }

    private Tramite crearTramiteDesdeRadicacion(SolicitudRadicacionTramite solicitud,
                                              String numeroRadicacion,
                                              CustomUserDetails currentUser) {
        // Obtener entidad
        Entidad entidad = entidadRepository.findById(solicitud.getEntidadId())
                .orElseThrow(() -> new ResourceNotFoundException("Entidad", "id", solicitud.getEntidadId()));

        // Obtener tipo de trámite
        TipoTramite tipoTramite = tipoTramiteRepository.findById(solicitud.getTipoTramiteId())
                .orElseThrow(() -> new ResourceNotFoundException("TipoTramite", "id", solicitud.getTipoTramiteId()));

        // Buscar o crear usuario solicitante por email
        Usuario solicitante = buscarOCrearSolicitante(solicitud.getSolicitanteEmail(), entidad);

        // Crear trámite completo
        Tramite tramite = new Tramite();
        tramite.setNumeroRadicacion(numeroRadicacion);
        tramite.setObjetoTramite(solicitud.getObjetoTramite());
        tramite.setFechaRadicacion(LocalDate.now());
        tramite.setFechaCreacion(LocalDateTime.now());
        tramite.setEstadoActual(Tramite.EstadoTramite.RADICADO);

        // Relaciones con entidades existentes
        tramite.setEntidad(entidad);
        tramite.setTipoTramite(tipoTramite);
        tramite.setSolicitante(solicitante);

        // Información adicional
        if (solicitud.getObservaciones() != null && !solicitud.getObservaciones().trim().isEmpty()) {
            tramite.setObservaciones(solicitud.getObservaciones());
        }

        logger.debug("Trámite creado: {} para tipo: {} en entidad: {}",
                numeroRadicacion, tipoTramite.getNombre(), entidad.getNombre());

        return tramite;
    }

    /**
     * Busca un usuario solicitante por email o crea uno nuevo si no existe
     */
    private Usuario buscarOCrearSolicitante(String email, Entidad entidad) {
        Optional<Usuario> usuarioExistente = usuarioRepository.findByCorreoElectronico(email);

        if (usuarioExistente.isPresent()) {
            return usuarioExistente.get();
        }

        // Crear nuevo usuario solicitante
        Usuario nuevoSolicitante = new Usuario();
        nuevoSolicitante.setCorreoElectronico(email);
        nuevoSolicitante.setNombreCompleto("Solicitante " + email); // Se actualizará después
        nuevoSolicitante.setRol(Usuario.Rol.SOLICITANTE);
        nuevoSolicitante.setEntidad(entidad);
        nuevoSolicitante.setActivo(true);

        // Contraseña temporal (debe cambiarla en el primer acceso)
        nuevoSolicitante.setContrasena("TEMPORAL_" + System.currentTimeMillis());

        Usuario solicitanteGuardado = usuarioRepository.save(nuevoSolicitante);

        logger.info("Nuevo usuario solicitante creado: {} para entidad: {}",
                email, entidad.getNombre());

        return solicitanteGuardado;
    }

    private void enviarNotificacionRadicacion(SolicitudRadicacion radicacion, Tramite tramite) {
        try {
            // Preparar datos para la notificación
            String asunto = String.format("Trámite Radicado - Número: %s", radicacion.getNumeroRadicacion());
            String mensaje = construirMensajeRadicacion(radicacion, tramite);

            // Enviar notificación usando el servicio existente
            notificacionService.enviarNotificacionEmail(
                radicacion.getSolicitanteEmail(),
                asunto,
                mensaje,
                NotificacionService.TipoNotificacion.RADICACION_EXITOSA
            );

            // Registrar en logs para auditoría
            logger.info("Notificación de radicación enviada: {} a {}",
                    radicacion.getNumeroRadicacion(), radicacion.getSolicitanteEmail());

        } catch (Exception e) {
            logger.error("Error enviando notificación de radicación {}: {}",
                    radicacion.getNumeroRadicacion(), e.getMessage());
            // No lanzar excepción para no afectar la radicación
        }
    }

    /**
     * Construye el mensaje de notificación de radicación
     */
    private String construirMensajeRadicacion(SolicitudRadicacion radicacion, Tramite tramite) {
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Estimado(a) solicitante,\n\n");
        mensaje.append("Su trámite ha sido radicado exitosamente con la siguiente información:\n\n");
        mensaje.append("📋 Número de Radicación: ").append(radicacion.getNumeroRadicacion()).append("\n");
        mensaje.append("📅 Fecha de Radicación: ").append(radicacion.getFechaRadicacion()).append("\n");
        mensaje.append("🏢 Entidad: ").append(tramite.getEntidad().getNombre()).append("\n");
        mensaje.append("📝 Tipo de Trámite: ").append(tramite.getTipoTramite().getNombre()).append("\n");
        mensaje.append("🎯 Objeto: ").append(radicacion.getObjetoTramite()).append("\n");
        mensaje.append("📊 Estado Actual: ").append(radicacion.getEstado().name()).append("\n\n");

        mensaje.append("IMPORTANTE:\n");
        mensaje.append("• Conserve este número de radicación para futuras consultas\n");
        mensaje.append("• Puede consultar el estado de su trámite en cualquier momento\n");
        mensaje.append("• Recibirá notificaciones automáticas cuando cambie el estado\n\n");

        mensaje.append("Para consultar el estado de su trámite, visite:\n");
        mensaje.append("Portal de Consulta: https://consulta.plataforma-tramites.gov.co\n\n");

        mensaje.append("Atentamente,\n");
        mensaje.append("Plataforma de Gestión de Trámites\n");
        mensaje.append(tramite.getEntidad().getNombre());

        return mensaje.toString();
    }

    /**
     * Reserva un número de radicación para uso posterior
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ReservaRadicacion reservarNumeroRadicacion(Long entidadId, String motivo) {
        logger.info("Reservando número de radicación para entidad: {}, motivo: {}", entidadId, motivo);

        String numeroReservado = generarSiguienteNumeroRadicacion(entidadId);
        CustomUserDetails currentUser = getCurrentUser();

        ReservaRadicacion reserva = new ReservaRadicacion();
        reserva.setNumeroRadicacion(numeroReservado);
        reserva.setEntidadId(entidadId);
        reserva.setMotivo(motivo);
        reserva.setUsuarioReserva(currentUser.getUsername());
        reserva.setFechaReserva(LocalDateTime.now());
        reserva.setEstado("RESERVADO");

        logger.info("Número reservado: {} por usuario: {}", numeroReservado, currentUser.getUsername());
        return reserva;
    }

    private CustomUserDetails getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            throw new IllegalStateException("Usuario no autenticado correctamente");
        }
        return (CustomUserDetails) principal;
    }

    /**
     * Método de compatibilidad con TramiteService - genera número por entidad y tipo
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public String generarNumeroRadicacion(Entidad entidad, TipoTramite tipoTramite) {
        return generarSiguienteNumeroRadicacion(entidad.getId());
    }

    /**
     * Método de compatibilidad con TramiteService - validación simple por formato
     */
    public boolean validarNumeroRadicacion(String numeroRadicacion) {
        try {
            ValidacionRadicacionService.ResultadoValidacionRadicacion resultado =
                validacionService.validarFormato(numeroRadicacion);
            return resultado.isValido();
        } catch (Exception e) {
            logger.warn("Error validando formato de radicación {}: {}", numeroRadicacion, e.getMessage());
            return false;
        }
    }

    // DTOs
    public static class SolicitudRadicacionTramite {
        private Long entidadId;
        private Long tipoTramiteId;
        private String objetoTramite;
        private String solicitanteEmail;
        private String observaciones;

        public Long getEntidadId() { return entidadId; }
        public void setEntidadId(Long entidadId) { this.entidadId = entidadId; }

        public Long getTipoTramiteId() { return tipoTramiteId; }
        public void setTipoTramiteId(Long tipoTramiteId) { this.tipoTramiteId = tipoTramiteId; }

        public String getObjetoTramite() { return objetoTramite; }
        public void setObjetoTramite(String objetoTramite) { this.objetoTramite = objetoTramite; }

        public String getSolicitanteEmail() { return solicitanteEmail; }
        public void setSolicitanteEmail(String solicitanteEmail) { this.solicitanteEmail = solicitanteEmail; }

        public String getObservaciones() { return observaciones; }
        public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    }

    public static class SolicitudRadicacion {
        private Long idTramite;
        private String numeroRadicacion;
        private LocalDate fechaRadicacion;
        private Long entidadId;
        private Long tipoTramiteId;
        private String objetoTramite;
        private Tramite.EstadoTramite estado;
        private String solicitanteEmail;

        public Long getIdTramite() { return idTramite; }
        public void setIdTramite(Long idTramite) { this.idTramite = idTramite; }

        public String getNumeroRadicacion() { return numeroRadicacion; }
        public void setNumeroRadicacion(String numeroRadicacion) { this.numeroRadicacion = numeroRadicacion; }

        public LocalDate getFechaRadicacion() { return fechaRadicacion; }
        public void setFechaRadicacion(LocalDate fechaRadicacion) { this.fechaRadicacion = fechaRadicacion; }

        public Long getEntidadId() { return entidadId; }
        public void setEntidadId(Long entidadId) { this.entidadId = entidadId; }

        public Long getTipoTramiteId() { return tipoTramiteId; }
        public void setTipoTramiteId(Long tipoTramiteId) { this.tipoTramiteId = tipoTramiteId; }

        public String getObjetoTramite() { return objetoTramite; }
        public void setObjetoTramite(String objetoTramite) { this.objetoTramite = objetoTramite; }

        public Tramite.EstadoTramite getEstado() { return estado; }
        public void setEstado(Tramite.EstadoTramite estado) { this.estado = estado; }

        public String getSolicitanteEmail() { return solicitanteEmail; }
        public void setSolicitanteEmail(String solicitanteEmail) { this.solicitanteEmail = solicitanteEmail; }
    }

    public static class EstadisticasRadicacion {
        private Long entidadId;
        private String nombreEntidad;
        private Integer ano;
        private Integer radicacionesSecretaria = 0;
        private Integer radicacionesCuraduria = 0;
        private Integer totalRadicaciones = 0;

        public void calcularTotales() {
            this.totalRadicaciones = (radicacionesSecretaria != null ? radicacionesSecretaria : 0) +
                                   (radicacionesCuraduria != null ? radicacionesCuraduria : 0);
        }

        public Long getEntidadId() { return entidadId; }
        public void setEntidadId(Long entidadId) { this.entidadId = entidadId; }

        public String getNombreEntidad() { return nombreEntidad; }
        public void setNombreEntidad(String nombreEntidad) { this.nombreEntidad = nombreEntidad; }

        public Integer getAno() { return ano; }
        public void setAno(Integer ano) { this.ano = ano; }

        public Integer getRadicacionesSecretaria() { return radicacionesSecretaria; }
        public void setRadicacionesSecretaria(Integer radicacionesSecretaria) { this.radicacionesSecretaria = radicacionesSecretaria; }

        public Integer getRadicacionesCuraduria() { return radicacionesCuraduria; }
        public void setRadicacionesCuraduria(Integer radicacionesCuraduria) { this.radicacionesCuraduria = radicacionesCuraduria; }

        public Integer getTotalRadicaciones() { return totalRadicaciones; }
        public void setTotalRadicaciones(Integer totalRadicaciones) { this.totalRadicaciones = totalRadicaciones; }
    }

    public static class ReservaRadicacion {
        private String numeroRadicacion;
        private Long entidadId;
        private String motivo;
        private String usuarioReserva;
        private LocalDateTime fechaReserva;
        private String estado;

        public String getNumeroRadicacion() { return numeroRadicacion; }
        public void setNumeroRadicacion(String numeroRadicacion) { this.numeroRadicacion = numeroRadicacion; }

        public Long getEntidadId() { return entidadId; }
        public void setEntidadId(Long entidadId) { this.entidadId = entidadId; }

        public String getMotivo() { return motivo; }
        public void setMotivo(String motivo) { this.motivo = motivo; }

        public String getUsuarioReserva() { return usuarioReserva; }
        public void setUsuarioReserva(String usuarioReserva) { this.usuarioReserva = usuarioReserva; }

        public LocalDateTime getFechaReserva() { return fechaReserva; }
        public void setFechaReserva(LocalDateTime fechaReserva) { this.fechaReserva = fechaReserva; }

        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }
    }
}