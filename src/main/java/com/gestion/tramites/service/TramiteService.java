// ============ ARCHIVO 1: TramiteService.java ============
package com.gestion.tramites.service;

import com.gestion.tramites.context.EntityContext;
import com.gestion.tramites.dto.tramite.TramiteRequestDTO;
import com.gestion.tramites.dto.tramite.TramiteResponseDTO;
import com.gestion.tramites.dto.tramite.TramitePublicoDTO;
import com.gestion.tramites.model.*;
import com.gestion.tramites.repository.*;
import com.gestion.tramites.exception.ResourceNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TramiteService {

    @Autowired
    private TramiteRepository tramiteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EntidadRepository entidadRepository;

    @Autowired
    private TipoTramiteRepository tipoTramiteRepository;

    @Autowired
    private RadicacionService radicacionService;

    @Autowired
    private ModelMapper modelMapper;

    // ============ MÉTODOS MULTI-TENANT (con filtrado automático) ============

    /**
     * Crear un nuevo trámite (automáticamente asociado a la entidad del usuario)
     */
    public TramiteResponseDTO crearTramite(TramiteRequestDTO requestDTO) {
        // Obtener usuario autenticado
        CustomUserDetails currentUser = getCurrentUser();

        // Validar que el usuario tenga entidad (excepto ADMIN_GLOBAL)
        if (!currentUser.isAdminGlobal() && !currentUser.tieneEntidad()) {
            throw new IllegalStateException(
                    "El usuario debe estar asociado a una entidad para crear trámites");
        }

        // Crear entidad Tramite
        Tramite tramite = new Tramite();

        // Establecer entidad automáticamente
        Long entidadId = determinarEntidadParaTramite(requestDTO, currentUser);
        Entidad entidad = entidadRepository.findById(entidadId)
                .orElseThrow(() -> new ResourceNotFoundException("Entidad", "id", entidadId));
        tramite.setEntidad(entidad);

        // Establecer solicitante
        Usuario solicitante = usuarioRepository.findById(requestDTO.getIdSolicitante())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id",
                        requestDTO.getIdSolicitante()));
        tramite.setSolicitante(solicitante);

        // Establecer tipo de trámite
        TipoTramite tipoTramite = tipoTramiteRepository.findById(requestDTO.getIdTipoTramite())
                .orElseThrow(() -> new ResourceNotFoundException("TipoTramite", "id",
                        requestDTO.getIdTipoTramite()));
        tramite.setTipoTramite(tipoTramite);

        // Generar número de radicación
        String numeroRadicacion = radicacionService.generarNumeroRadicacion(entidad, tipoTramite);
        tramite.setNumeroRadicacion(numeroRadicacion);

        // Mapear campos adicionales
        tramite.setObjetoTramite(requestDTO.getObjetoTramite());
        tramite.setDescripcionProyecto(requestDTO.getDescripcionProyecto());
        tramite.setDireccionInmueble(requestDTO.getDireccionInmueble());
        tramite.setEstadoActual(Tramite.EstadoTramite.RADICADO);
        tramite.setFechaRadicacion(LocalDate.now());

        // Establecer relaciones opcionales
        if (requestDTO.getIdModalidadTramite() != null) {
            // Buscar modalidad y establecer
        }
        if (requestDTO.getIdSubtipoTramite() != null) {
            // Buscar subtipo y establecer
        }

        Tramite tramiteGuardado = tramiteRepository.save(tramite);
        return convertToResponseDTO(tramiteGuardado);
    }

    /**
     * Obtener todos los trámites de la entidad actual
     */
    @Transactional(readOnly = true)
    public List<TramiteResponseDTO> obtenerTramites() {
        CustomUserDetails currentUser = getCurrentUser();

        List<Tramite> tramites;
        if (currentUser.isAdminGlobal()) {
            // Admin global ve todos los trámites
            tramites = tramiteRepository.findAll();
        } else {
            // Usuarios de entidad ven solo trámites de su entidad
            tramites = tramiteRepository.findAll();
        }

        return tramites.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    /**
     * Obtener trámite por ID (con filtro de entidad automático)
     */
    @Transactional(readOnly = true)
    public TramiteResponseDTO obtenerTramitePorId(Long id) {
        CustomUserDetails currentUser = getCurrentUser();

        Optional<Tramite> tramite;
        if (currentUser.isAdminGlobal()) {
            tramite = tramiteRepository.findById(id);
        } else {
            tramite = tramiteRepository.findById(id);
        }

        return tramite.map(this::convertToResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Tramite", "id", id));
    }

    /**
     * Actualizar estado de trámite
     */
    public TramiteResponseDTO actualizarEstado(Long id, Tramite.EstadoTramite nuevoEstado,
            String comentarios) {
        CustomUserDetails currentUser = getCurrentUser();

        Tramite tramite;
        if (currentUser.isAdminGlobal()) {
            tramite = tramiteRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Tramite", "id", id));
        } else {
            tramite = tramiteRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Tramite", "id", id));
        }

        // Validar transición de estado
        validarTransicionEstado(tramite.getEstadoActual(), nuevoEstado);

        // Actualizar estado
        tramite.cambiarEstado(nuevoEstado, comentarios);

        // Si se asigna a revisor, establecer revisor actual
        if (nuevoEstado == Tramite.EstadoTramite.ASIGNADO && currentUser.isRevisor()) {
            Usuario revisor = usuarioRepository.findById(currentUser.getIdUsuario())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id",
                            currentUser.getIdUsuario()));
            tramite.setRevisorAsignado(revisor);
        }

        Tramite tramiteActualizado = tramiteRepository.save(tramite);
        return convertToResponseDTO(tramiteActualizado);
    }

    /**
     * Asignar revisor a trámite
     */
    public TramiteResponseDTO asignarRevisor(Long tramiteId, Long revisorId) {
        CustomUserDetails currentUser = getCurrentUser();

        // Solo admins pueden asignar revisores
        if (!currentUser.isAdminGlobal() && !currentUser.isAdminEntidad()) {
            throw new IllegalStateException("Solo los administradores pueden asignar revisores");
        }

        Tramite tramite = obtenerTramiteConFiltro(tramiteId, currentUser);
        Usuario revisor = usuarioRepository.findById(revisorId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", revisorId));

        // Validar que el revisor pertenezca a la misma entidad
        if (!currentUser.isAdminGlobal()
                && !revisor.getEntidad().getId().equals(currentUser.getIdEntidad())) {
            throw new IllegalStateException("El revisor debe pertenecer a la misma entidad");
        }

        tramite.setRevisorAsignado(revisor);
        tramite.cambiarEstado(Tramite.EstadoTramite.ASIGNADO,
                "Asignado a revisor: " + revisor.getNombreCompleto());

        Tramite tramiteActualizado = tramiteRepository.save(tramite);
        return convertToResponseDTO(tramiteActualizado);
    }

    // ============ API PÚBLICA (sin autenticación) ============

    /**
     * Consulta pública del estado de un trámite por número de radicación
     */
    @Transactional(readOnly = true)
    public TramitePublicoDTO consultarEstadoPublico(String numeroRadicacion) {
        if (!radicacionService.validarNumeroRadicacion(numeroRadicacion)) {
            throw new IllegalArgumentException("Número de radicación inválido");
        }

        Tramite tramite = tramiteRepository.findByNumeroRadicacion(numeroRadicacion)
                .orElseThrow(() -> new ResourceNotFoundException("Tramite", "numeroRadicacion",
                        numeroRadicacion));

        return convertToPublicDTO(tramite);
    }

    // ============ MÉTODOS AUXILIARES ============

    private CustomUserDetails getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            throw new IllegalStateException("Usuario no autenticado correctamente");
        }
        return (CustomUserDetails) principal;
    }

    private Long determinarEntidadParaTramite(TramiteRequestDTO requestDTO,
            CustomUserDetails currentUser) {
        // Admin global puede especificar entidad
        if (currentUser.isAdminGlobal() && requestDTO.getIdEntidad() != null) {
            return requestDTO.getIdEntidad();
        }

        // Otros usuarios usan su entidad
        if (currentUser.tieneEntidad()) {
            return currentUser.getIdEntidad();
        }

        throw new IllegalStateException("No se puede determinar la entidad para el trámite");
    }

    private Tramite obtenerTramiteConFiltro(Long tramiteId, CustomUserDetails currentUser) {
        if (currentUser.isAdminGlobal()) {
            return tramiteRepository.findById(tramiteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tramite", "id", tramiteId));
        } else {
            return tramiteRepository.findByIdAndCurrentEntity(tramiteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tramite", "id", tramiteId));
        }
    }

    private void validarTransicionEstado(Tramite.EstadoTramite estadoActual,
            Tramite.EstadoTramite nuevoEstado) {
        // Definir transiciones válidas
        boolean transicionValida = switch (estadoActual) {
            case RADICADO -> nuevoEstado == Tramite.EstadoTramite.ASIGNADO
                    || nuevoEstado == Tramite.EstadoTramite.CANCELADO;
            case ASIGNADO -> nuevoEstado == Tramite.EstadoTramite.EN_REVISION
                    || nuevoEstado == Tramite.EstadoTramite.CANCELADO;
            case EN_REVISION -> nuevoEstado == Tramite.EstadoTramite.PENDIENTE_DOCUMENTOS
                    || nuevoEstado == Tramite.EstadoTramite.EN_ESPERA_CONCEPTO
                    || nuevoEstado == Tramite.EstadoTramite.APROBADO
                    || nuevoEstado == Tramite.EstadoTramite.RECHAZADO;
            case PENDIENTE_DOCUMENTOS -> nuevoEstado == Tramite.EstadoTramite.EN_REVISION
                    || nuevoEstado == Tramite.EstadoTramite.ARCHIVADO;
            case EN_ESPERA_CONCEPTO -> nuevoEstado == Tramite.EstadoTramite.CONCEPTO_FAVORABLE
                    || nuevoEstado == Tramite.EstadoTramite.CONCEPTO_DESFAVORABLE;
            case CONCEPTO_FAVORABLE -> nuevoEstado == Tramite.EstadoTramite.APROBADO;
            case CONCEPTO_DESFAVORABLE -> nuevoEstado == Tramite.EstadoTramite.RECHAZADO;
            default -> false; // Estados finales no pueden cambiar
        };

        if (!transicionValida) {
            throw new IllegalStateException(
                    String.format("Transición no válida: de %s a %s", estadoActual, nuevoEstado));
        }
    }

    private TramiteResponseDTO convertToResponseDTO(Tramite tramite) {
        TramiteResponseDTO dto = modelMapper.map(tramite, TramiteResponseDTO.class);

        // Mapear campos específicos que ModelMapper no puede inferir
        if (tramite.getEntidad() != null) {
            dto.setNombreEntidad(tramite.getEntidad().getNombre());
        }
        if (tramite.getSolicitante() != null) {
            dto.setNombreSolicitante(tramite.getSolicitante().getNombreCompleto());
        }
        if (tramite.getRevisorAsignado() != null) {
            dto.setNombreRevisor(tramite.getRevisorAsignado().getNombreCompleto());
        }
        if (tramite.getTipoTramite() != null) {
            dto.setNombreTipoTramite(tramite.getTipoTramite().getNombre());
        }

        return dto;
    }

    private TramitePublicoDTO convertToPublicDTO(Tramite tramite) {
        TramitePublicoDTO dto = new TramitePublicoDTO();
        dto.setNumeroRadicacion(tramite.getNumeroRadicacion());
        dto.setFechaRadicacion(tramite.getFechaRadicacion());
        dto.setObjetoTramite(tramite.getObjetoTramite());
        dto.setEstadoActual(tramite.getEstadoActual().name());
        dto.setDescripcionEstado(tramite.getEstadoActual().getDescripcion());
        dto.setFechaUltimoCambio(tramite.getFechaUltimoCambioEstado());
        dto.setNombreEntidad(tramite.getEntidad().getNombre());

        // Solo información pública, sin datos sensibles
        return dto;
    }
}
