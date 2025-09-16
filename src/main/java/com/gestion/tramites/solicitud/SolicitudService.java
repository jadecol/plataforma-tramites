package com.gestion.tramites.solicitud;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections; // <--- Añadir este import
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Para manejar transacciones

import com.gestion.tramites.auth.CustomUserDetails;
import com.gestion.tramites.excepciones.ResourceNotFoundException;
// Importa todas las entidades necesarias
import com.gestion.tramites.model.ModalidadTramite;
import com.gestion.tramites.model.Solicitud;
import com.gestion.tramites.model.SubtipoTramite;
import com.gestion.tramites.model.TipoTramite;
import com.gestion.tramites.tipo.SubtipoTramiteRepository;
import com.gestion.tramites.tipo.TipoTramiteRepository;
import com.gestion.tramites.tramite.ModalidadTramiteRepository;
import com.gestion.tramites.usuario.Usuario;
import com.gestion.tramites.usuario.UsuarioRepository;

@Service
public class SolicitudService {

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private UsuarioRepository usuarioRepository; // Necesario para buscar solicitante/revisor

    @Autowired
    private TipoTramiteRepository tipoTramiteRepository; // Para asociar el tipo de trámite

    @Autowired
    private SubtipoTramiteRepository subtipoTramiteRepository; // Para asociar el subtipo de trámite

    @Autowired
    private ModalidadTramiteRepository modalidadTramiteRepository; // Para asociar la modalidad de trámite

    // Método para obtener el usuario autenticado del contexto de seguridad
    private CustomUserDetails getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No hay usuario autenticado.");
        }
        // Asegúrate de que el principal sea una instancia de CustomUserDetails
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return (CustomUserDetails) principal;
        } else {
            // Esto podría ocurrir si hay un usuario anónimo o si Spring Security usa un UserDetails por defecto
            throw new IllegalStateException("El principal de autenticación no es CustomUserDetails.");
        }
    }

    // Genera un número de radicación simple (puedes mejorarlo)
    private String generateNumeroRadicacion() {
        // Formato: SOL-AAAA-MM-DD-HHMMSS-RANDOM (o un contador de DB)
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        // Para un sistema real, querrías una secuencia en DB para asegurar unicidad y evitar colisiones
        long randomSuffix = System.currentTimeMillis() % 10000; // Un sufijo simple
        return "SOL-" + timestamp + "-" + randomSuffix;
    }

    // CREAR SOLICITUD
    @Transactional
    public Solicitud crearSolicitud(Solicitud solicitud) {
        CustomUserDetails currentUser = getAuthenticatedUser();
        Usuario solicitante = usuarioRepository.findById(currentUser.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario solicitante no encontrado con ID: " + currentUser.getIdUsuario()));

        // Asigna el solicitante y otros valores por defecto
        solicitud.setSolicitante(solicitante);
        solicitud.setFechaRadicacion(LocalDate.now());
        solicitud.setFechaUltimoCambioEstado(LocalDateTime.now());
        solicitud.setEstadoActual("RADICADA"); // Estado inicial

        // Generar número de radicación único
        String numRadicacion;
        do {
            numRadicacion = generateNumeroRadicacion();
        } while (solicitudRepository.findByNumeroRadicacion(numRadicacion).isPresent());
        solicitud.setNumeroRadicacion(numRadicacion);

        // Cargar entidades relacionadas para asegurar que estén "managed" por JPA
        if (solicitud.getTipoTramite() != null && solicitud.getTipoTramite().getIdTipoTramite() != null) {
            TipoTramite tipo = tipoTramiteRepository.findById(solicitud.getTipoTramite().getIdTipoTramite())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de Trámite no encontrado con ID: " + solicitud.getTipoTramite().getIdTipoTramite()));
            solicitud.setTipoTramite(tipo);
        } else {
             throw new IllegalArgumentException("El ID del Tipo de Trámite es obligatorio.");
        }

        if (solicitud.getSubtipoTramite() != null && solicitud.getSubtipoTramite().getIdSubtipoTramite() != null) {
            SubtipoTramite subtipo = subtipoTramiteRepository.findById(solicitud.getSubtipoTramite().getIdSubtipoTramite())
                    .orElseThrow(() -> new ResourceNotFoundException("Subtipo de Trámite no encontrado con ID: " + solicitud.getSubtipoTramite().getIdSubtipoTramite()));
            solicitud.setSubtipoTramite(subtipo);
        }

        if (solicitud.getModalidadTramite() != null && solicitud.getModalidadTramite().getIdModalidadTramite() != null) {
            ModalidadTramite modalidad = modalidadTramiteRepository.findById(solicitud.getModalidadTramite().getIdModalidadTramite())
                    .orElseThrow(() -> new ResourceNotFoundException("Modalidad de Trámite no encontrada con ID: " + solicitud.getModalidadTramite().getIdModalidadTramite()));
            solicitud.setModalidadTramite(modalidad);
        }

        return solicitudRepository.save(solicitud);
    }

    // OBTENER SOLICITUD POR ID (con validación de rol)
    public Solicitud getSolicitudById(Long id) {
        CustomUserDetails currentUser = getAuthenticatedUser();
        String userRole = currentUser.getAuthorities().iterator().next().getAuthority(); // Obtiene el primer rol
        Long currentUserId = currentUser.getIdUsuario();

        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la solicitud con el id: " + id));

        // Lógica de autorización basada en roles
        if (userRole.equals("ROLE_ADMIN") ||
            (userRole.equals("ROLE_SOLICITANTE") && solicitud.getSolicitante().getIdUsuario().equals(currentUserId)) ||
            (userRole.equals("ROLE_REVISOR") && solicitud.getRevisorAsignado() != null && solicitud.getRevisorAsignado().getIdUsuario().equals(currentUserId))) {
            return solicitud;
        } else {
            throw new IllegalStateException("Acceso denegado a la solicitud con id: " + id); // O una excepción más específica como AccessDeniedException
        }
    }

    // LISTAR SOLICITUDES (filtrado por rol)
    public List<Solicitud> getAllSolicitudes() {
        CustomUserDetails currentUser = getAuthenticatedUser();
        String userRole = currentUser.getAuthorities().iterator().next().getAuthority();
        Long currentUserId = currentUser.getIdUsuario();

        if (userRole.equals("ROLE_ADMIN")) {
            return solicitudRepository.findAll();
        } else if (userRole.equals("ROLE_SOLICITANTE")) {
            Usuario solicitante = usuarioRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario solicitante no encontrado con ID: " + currentUserId));
            return solicitudRepository.findBySolicitante(solicitante);
        } else if (userRole.equals("ROLE_REVISOR")) {
            Usuario revisor = usuarioRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario revisor no encontrado con ID: " + currentUserId));
            return solicitudRepository.findByRevisorAsignado(revisor);
        } else {
            // Para roles no definidos o que no deberían ver solicitudes
            return Collections.emptyList();
        }
    }

    // ACTUALIZAR SOLICITUD (Solo admin o revisor asignado)
    @Transactional
    public Solicitud updateSolicitud(Long id, Solicitud detallesSolicitud) {
        CustomUserDetails currentUser = getAuthenticatedUser();
        String userRole = currentUser.getAuthorities().iterator().next().getAuthority();
        Long currentUserId = currentUser.getIdUsuario();

        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la solicitud con el id: " + id));

        // Lógica de autorización para actualizar
        if (!userRole.equals("ROLE_ADMIN") &&
            !(userRole.equals("ROLE_REVISOR") && solicitud.getRevisorAsignado() != null && solicitud.getRevisorAsignado().getIdUsuario().equals(currentUserId))) {
            throw new IllegalStateException("No tienes permisos para actualizar esta solicitud.");
        }

        // Actualiza solo los campos permitidos o relevantes para el flujo
        solicitud.setObjetoTramite(detallesSolicitud.getObjetoTramite());
        solicitud.setDescripcionProyecto(detallesSolicitud.getDescripcionProyecto());
        solicitud.setDireccionInmueble(detallesSolicitud.getDireccionInmueble());
        solicitud.setCondicionRadicacion(detallesSolicitud.getCondicionRadicacion());

        // Actualizar relaciones (solo si se proporciona un ID válido)
        if (detallesSolicitud.getTipoTramite() != null && detallesSolicitud.getTipoTramite().getIdTipoTramite() != null) {
            TipoTramite tipo = tipoTramiteRepository.findById(detallesSolicitud.getTipoTramite().getIdTipoTramite())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de Trámite no encontrado con ID: " + detallesSolicitud.getTipoTramite().getIdTipoTramite()));
            solicitud.setTipoTramite(tipo);
        }
        if (detallesSolicitud.getSubtipoTramite() != null && detallesSolicitud.getSubtipoTramite().getIdSubtipoTramite() != null) {
            SubtipoTramite subtipo = subtipoTramiteRepository.findById(detallesSolicitud.getSubtipoTramite().getIdSubtipoTramite())
                    .orElseThrow(() -> new ResourceNotFoundException("Subtipo de Trámite no encontrado con ID: " + detallesSolicitud.getSubtipoTramite().getIdSubtipoTramite()));
            solicitud.setSubtipoTramite(subtipo);
        }
        if (detallesSolicitud.getModalidadTramite() != null && detallesSolicitud.getModalidadTramite().getIdModalidadTramite() != null) {
            ModalidadTramite modalidad = modalidadTramiteRepository.findById(detallesSolicitud.getModalidadTramite().getIdModalidadTramite())
                    .orElseThrow(() -> new ResourceNotFoundException("Modalidad de Trámite no encontrada con ID: " + detallesSolicitud.getModalidadTramite().getIdModalidadTramite()));
            solicitud.setModalidadTramite(modalidad);
        }

        // Solo ciertos roles pueden cambiar el estado o asignar revisor
        if (userRole.equals("ROLE_ADMIN") || userRole.equals("ROLE_REVISOR")) {
            if (detallesSolicitud.getEstadoActual() != null && !detallesSolicitud.getEstadoActual().isEmpty()) {
                solicitud.setEstadoActual(detallesSolicitud.getEstadoActual());
                solicitud.setFechaUltimoCambioEstado(LocalDateTime.now());
            }
            if (detallesSolicitud.getFechaLimiteProximo() != null) {
                solicitud.setFechaLimiteProximo(detallesSolicitud.getFechaLimiteProximo());
            }
            if (detallesSolicitud.getFechaLimiteCompletar() != null) {
                solicitud.setFechaLimiteCompletar(detallesSolicitud.getFechaLimiteCompletar());
            }
        }


        return solicitudRepository.save(solicitud);
    }

    // ASIGNAR REVISOR (Solo ADMIN)
    @Transactional
    public Solicitud asignarRevisor(Long solicitudId, Long revisorId) {
        CustomUserDetails currentUser = getAuthenticatedUser();
        String userRole = currentUser.getAuthorities().iterator().next().getAuthority();

        if (!userRole.equals("ROLE_ADMIN")) {
            throw new IllegalStateException("Solo los administradores pueden asignar revisores.");
        }

        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la solicitud con el id: " + solicitudId));

        Usuario revisor = usuarioRepository.findById(revisorId)
                .orElseThrow(() -> new ResourceNotFoundException("Revisor no encontrado con ID: " + revisorId));

        // Opcional: Validar que el usuario 'revisor' realmente tenga el rol 'REVISOR'
        if (!"REVISOR".equalsIgnoreCase(revisor.getRol())) {
            throw new IllegalArgumentException("El usuario con ID " + revisorId + " no tiene el rol de REVISOR.");
        }

        solicitud.setRevisorAsignado(revisor);
        solicitud.setEstadoActual("EN_REVISION"); // Actualiza el estado al asignar un revisor
        solicitud.setFechaUltimoCambioEstado(LocalDateTime.now());
        return solicitudRepository.save(solicitud);
    }

    // ELIMINAR SOLICITUD (Solo ADMIN)
    public Map<String, Boolean> deleteSolicitud(Long id) {
        CustomUserDetails currentUser = getAuthenticatedUser();
        String userRole = currentUser.getAuthorities().iterator().next().getAuthority();

        if (!userRole.equals("ROLE_ADMIN")) {
            throw new IllegalStateException("Solo los administradores pueden eliminar solicitudes.");
        }

        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la solicitud con el id: " + id));

        solicitudRepository.delete(solicitud);
        Map<String, Boolean> response = new HashMap<>();
        response.put("eliminado", Boolean.TRUE);
        return response;
    }
}