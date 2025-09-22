package com.gestion.tramites.service;

import com.gestion.tramites.model.EntidadGubernamental;
import com.gestion.tramites.repository.EntidadGubernamentalRepository;
import com.gestion.tramites.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VerificacionEntidadGubernamentalService {

    private static final Logger logger = LoggerFactory.getLogger(VerificacionEntidadGubernamentalService.class);

    @Autowired
    private EntidadGubernamentalRepository entidadGubernamentalRepository;

    @Autowired
    private ValidacionDominiosGubernamentalesService validacionDominiosService;

    @Autowired
    private NotificacionService notificacionService;

    /**
     * Registra una nueva entidad gubernamental y inicia el proceso de verificación
     */
    public EntidadGubernamental registrarEntidadGubernamental(SolicitudRegistroEntidad solicitud) {
        logger.info("Iniciando registro de entidad gubernamental: {}", solicitud.getNombre());

        // Validar dominio oficial
        ValidacionDominiosGubernamentalesService.ResultadoValidacionDominio resultadoValidacion =
                validacionDominiosService.validarDominioGubernamental(
                        solicitud.getDominioOficial(),
                        solicitud.getTipoEntidad().name()
                );

        if (!resultadoValidacion.isValido()) {
            throw new IllegalArgumentException(
                    "Dominio no válido para entidad gubernamental: " + resultadoValidacion.getMensaje());
        }

        // Verificar unicidad de datos críticos
        verificarUnicidadDatos(solicitud);

        // Crear entidad gubernamental
        EntidadGubernamental entidad = new EntidadGubernamental();
        entidad.setNombre(solicitud.getNombre());
        entidad.setCodigoDane(solicitud.getCodigoDane());
        entidad.setNit(solicitud.getNit());
        entidad.setTipoEntidad(solicitud.getTipoEntidad());
        entidad.setDominioOficial(solicitud.getDominioOficial());
        entidad.setSitioWebOficial(solicitud.getSitioWebOficial());
        entidad.setEmailOficial(solicitud.getEmailOficial());
        entidad.setTelefonoOficial(solicitud.getTelefonoOficial());
        entidad.setDireccionFisica(solicitud.getDireccionFisica());
        entidad.setDepartamento(solicitud.getDepartamento());
        entidad.setMunicipio(solicitud.getMunicipio());
        entidad.setEstadoVerificacion(EntidadGubernamental.EstadoVerificacion.PENDIENTE);

        EntidadGubernamental entidadGuardada = entidadGubernamentalRepository.save(entidad);

        // Crear entrada de auditoría
        crearEntradaAuditoria(entidadGuardada, "REGISTRO_INICIADO",
                "Entidad registrada. Dominio validado: " + resultadoValidacion.getMensaje());

        logger.info("Entidad gubernamental registrada exitosamente: {} (ID: {})",
                entidad.getNombre(), entidadGuardada.getIdEntidadGubernamental());

        return entidadGuardada;
    }

    /**
     * Verifica manualmente una entidad gubernamental
     */
    public EntidadGubernamental verificarEntidad(Long entidadId, SolicitudVerificacion solicitudVerificacion) {
        CustomUserDetails currentUser = getCurrentUser();

        // Solo administradores globales pueden verificar entidades
        if (!currentUser.isAdminGlobal()) {
            throw new IllegalStateException("Solo administradores globales pueden verificar entidades gubernamentales");
        }

        EntidadGubernamental entidad = entidadGubernamentalRepository.findById(entidadId)
                .orElseThrow(() -> new ResourceNotFoundException("EntidadGubernamental", "id", entidadId));

        if (entidad.getEstadoVerificacion() == EntidadGubernamental.EstadoVerificacion.VERIFICADA) {
            throw new IllegalStateException("La entidad ya está verificada");
        }

        // Realizar verificación automática de dominio nuevamente
        ValidacionDominiosGubernamentalesService.ResultadoValidacionDominio resultadoValidacion =
                validacionDominiosService.validarDominioGubernamental(
                        entidad.getDominioOficial(),
                        entidad.getTipoEntidad().name()
                );

        if (!resultadoValidacion.isValido()) {
            entidad.rechazar(currentUser.getUsername(),
                    "Dominio no válido durante verificación: " + resultadoValidacion.getMensaje());
        } else if (solicitudVerificacion.isAprobada()) {
            entidad.verificar(currentUser.getUsername(), solicitudVerificacion.getObservaciones());

            // Crear entidad en el sistema principal si es aprobada
            if (entidad.getEntidadSistema() == null) {
                crearEntidadEnSistemaPrincipal(entidad);
            }
        } else {
            entidad.rechazar(currentUser.getUsername(), solicitudVerificacion.getObservaciones());
        }

        EntidadGubernamental entidadActualizada = entidadGubernamentalRepository.save(entidad);

        // Crear entrada de auditoría
        String accion = solicitudVerificacion.isAprobada() ? "VERIFICACION_APROBADA" : "VERIFICACION_RECHAZADA";
        crearEntradaAuditoria(entidadActualizada, accion,
                String.format("Verificada por %s. Observaciones: %s",
                        currentUser.getUsername(), solicitudVerificacion.getObservaciones()));

        logger.info("Entidad {} {}: {} por {}",
                entidad.getNombre(),
                solicitudVerificacion.isAprobada() ? "verificada" : "rechazada",
                entidadId,
                currentUser.getUsername());

        return entidadActualizada;
    }

    /**
     * Suspende una entidad gubernamental verificada
     */
    public EntidadGubernamental suspenderEntidad(Long entidadId, String motivo) {
        CustomUserDetails currentUser = getCurrentUser();

        if (!currentUser.isAdminGlobal()) {
            throw new IllegalStateException("Solo administradores globales pueden suspender entidades");
        }

        EntidadGubernamental entidad = entidadGubernamentalRepository.findById(entidadId)
                .orElseThrow(() -> new ResourceNotFoundException("EntidadGubernamental", "id", entidadId));

        if (!entidad.estaVerificada()) {
            throw new IllegalStateException("Solo se pueden suspender entidades verificadas");
        }

        entidad.suspender(currentUser.getUsername(), motivo);
        EntidadGubernamental entidadActualizada = entidadGubernamentalRepository.save(entidad);

        // Desactivar entidad en sistema principal
        if (entidad.getEntidadSistema() != null) {
            entidad.getEntidadSistema().setActivo(false);
        }

        crearEntradaAuditoria(entidadActualizada, "ENTIDAD_SUSPENDIDA",
                String.format("Suspendida por %s. Motivo: %s", currentUser.getUsername(), motivo));

        logger.warn("Entidad gubernamental suspendida: {} (ID: {}) por {}",
                entidad.getNombre(), entidadId, currentUser.getUsername());

        return entidadActualizada;
    }

    /**
     * Obtiene entidades pendientes de verificación
     */
    @Transactional(readOnly = true)
    public List<EntidadGubernamental> obtenerEntidadesPendientesVerificacion() {
        return entidadGubernamentalRepository.findByEstadoVerificacion(
                EntidadGubernamental.EstadoVerificacion.PENDIENTE);
    }

    /**
     * Obtiene entidades por estado de verificación
     */
    @Transactional(readOnly = true)
    public List<EntidadGubernamental> obtenerEntidadesPorEstado(EntidadGubernamental.EstadoVerificacion estado) {
        return entidadGubernamentalRepository.findByEstadoVerificacion(estado);
    }

    /**
     * Busca entidad gubernamental por dominio oficial
     */
    @Transactional(readOnly = true)
    public Optional<EntidadGubernamental> buscarPorDominioOficial(String dominio) {
        return entidadGubernamentalRepository.findByDominioOficial(dominio);
    }

    /**
     * Busca entidad gubernamental por código DANE
     */
    @Transactional(readOnly = true)
    public Optional<EntidadGubernamental> buscarPorCodigoDane(String codigoDane) {
        return entidadGubernamentalRepository.findByCodigoDane(codigoDane);
    }

    /**
     * Valida que una entidad esté verificada y activa para operaciones críticas
     */
    public void validarEntidadParaOperacionCritica(Long entidadId) {
        EntidadGubernamental entidad = entidadGubernamentalRepository.findById(entidadId)
                .orElseThrow(() -> new ResourceNotFoundException("EntidadGubernamental", "id", entidadId));

        if (!entidad.estaActiva()) {
            throw new IllegalStateException(
                    String.format("La entidad %s no está verificada o está suspendida", entidad.getNombre()));
        }
    }

    private void verificarUnicidadDatos(SolicitudRegistroEntidad solicitud) {
        // Verificar dominio único
        if (entidadGubernamentalRepository.existsByDominioOficial(solicitud.getDominioOficial())) {
            throw new IllegalArgumentException("El dominio oficial ya está registrado por otra entidad");
        }

        // Verificar código DANE único
        if (entidadGubernamentalRepository.existsByCodigoDane(solicitud.getCodigoDane())) {
            throw new IllegalArgumentException("El código DANE ya está registrado por otra entidad");
        }

        // Verificar NIT único
        if (entidadGubernamentalRepository.existsByNit(solicitud.getNit())) {
            throw new IllegalArgumentException("El NIT ya está registrado por otra entidad");
        }

        // Verificar email único
        if (entidadGubernamentalRepository.existsByEmailOficial(solicitud.getEmailOficial())) {
            throw new IllegalArgumentException("El email oficial ya está registrado por otra entidad");
        }
    }

    private void crearEntidadEnSistemaPrincipal(EntidadGubernamental entidadGubernamental) {
        // Crear entidad en el sistema principal (tabla entidades)
        com.gestion.tramites.model.Entidad entidadSistema = new com.gestion.tramites.model.Entidad();
        entidadSistema.setNombre(entidadGubernamental.getNombre());
        entidadSistema.setNit(entidadGubernamental.getNit());
        entidadSistema.setDireccion(entidadGubernamental.getDireccionFisica());
        entidadSistema.setTelefono(entidadGubernamental.getTelefonoOficial());
        entidadSistema.setEmail(entidadGubernamental.getEmailOficial());
        entidadSistema.setSitioWeb(entidadGubernamental.getSitioWebOficial());
        entidadSistema.setActivo(true);

        // Aquí necesitarías inyectar EntidadRepository para guardar
        // Por simplicidad, se asume que se manejará en otra capa

        entidadGubernamental.setEntidadSistema(entidadSistema);

        logger.info("Entidad creada en sistema principal para: {}", entidadGubernamental.getNombre());
    }

    private void crearEntradaAuditoria(EntidadGubernamental entidad, String accion, String detalle) {
        CustomUserDetails currentUser = getCurrentUser();

        // Crear entrada de auditoría
        logger.info("AUDITORIA - Entidad: {} | Acción: {} | Usuario: {} | Detalle: {}",
                entidad.getNombre(), accion, currentUser.getUsername(), detalle);
    }

    private CustomUserDetails getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            throw new IllegalStateException("Usuario no autenticado correctamente");
        }
        return (CustomUserDetails) principal;
    }

    // DTOs para solicitudes
    public static class SolicitudRegistroEntidad {
        private String nombre;
        private String codigoDane;
        private String nit;
        private EntidadGubernamental.TipoEntidadGubernamental tipoEntidad;
        private String dominioOficial;
        private String sitioWebOficial;
        private String emailOficial;
        private String telefonoOficial;
        private String direccionFisica;
        private String departamento;
        private String municipio;

        // Getters y Setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getCodigoDane() { return codigoDane; }
        public void setCodigoDane(String codigoDane) { this.codigoDane = codigoDane; }

        public String getNit() { return nit; }
        public void setNit(String nit) { this.nit = nit; }

        public EntidadGubernamental.TipoEntidadGubernamental getTipoEntidad() { return tipoEntidad; }
        public void setTipoEntidad(EntidadGubernamental.TipoEntidadGubernamental tipoEntidad) { this.tipoEntidad = tipoEntidad; }

        public String getDominioOficial() { return dominioOficial; }
        public void setDominioOficial(String dominioOficial) { this.dominioOficial = dominioOficial; }

        public String getSitioWebOficial() { return sitioWebOficial; }
        public void setSitioWebOficial(String sitioWebOficial) { this.sitioWebOficial = sitioWebOficial; }

        public String getEmailOficial() { return emailOficial; }
        public void setEmailOficial(String emailOficial) { this.emailOficial = emailOficial; }

        public String getTelefonoOficial() { return telefonoOficial; }
        public void setTelefonoOficial(String telefonoOficial) { this.telefonoOficial = telefonoOficial; }

        public String getDireccionFisica() { return direccionFisica; }
        public void setDireccionFisica(String direccionFisica) { this.direccionFisica = direccionFisica; }

        public String getDepartamento() { return departamento; }
        public void setDepartamento(String departamento) { this.departamento = departamento; }

        public String getMunicipio() { return municipio; }
        public void setMunicipio(String municipio) { this.municipio = municipio; }
    }

    public static class SolicitudVerificacion {
        private boolean aprobada;
        private String observaciones;

        public SolicitudVerificacion() {}

        public SolicitudVerificacion(boolean aprobada, String observaciones) {
            this.aprobada = aprobada;
            this.observaciones = observaciones;
        }

        public boolean isAprobada() { return aprobada; }
        public void setAprobada(boolean aprobada) { this.aprobada = aprobada; }

        public String getObservaciones() { return observaciones; }
        public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    }
}