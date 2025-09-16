package com.gestion.tramites.tramite;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper; // Necesitarás esta dependencia
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestion.tramites.entidad.EntidadRepository;
import com.gestion.tramites.excepciones.ResourceNotFoundException; // <-- ¡CORREGIDO A PLURAL!
//import com.gestion.tramites.entidad.Entidad;
import com.gestion.tramites.model.Entidad; // <-- ¡CAMBIO AQUÍ!
import com.gestion.tramites.model.Tramite;
import com.gestion.tramites.tramite.dto.TramiteEstadoUpdateDTO;
import com.gestion.tramites.tramite.dto.TramiteRequestDTO;
import com.gestion.tramites.tramite.dto.TramiteResponseDTO;
import com.gestion.tramites.usuario.Usuario;
import com.gestion.tramites.usuario.UsuarioRepository;

@Service
public class TramiteService {

    private final TramiteRepository tramiteRepository;
    private final UsuarioRepository usuarioRepository;
    private final EntidadRepository entidadRepository;
    private final ModelMapper modelMapper; // Para mapear entre Entidad/DTO

    public TramiteService(TramiteRepository tramiteRepository,
                          UsuarioRepository usuarioRepository,
                          EntidadRepository entidadRepository,
                          ModelMapper modelMapper) {
        this.tramiteRepository = tramiteRepository;
        this.usuarioRepository = usuarioRepository;
        this.entidadRepository = entidadRepository;
        this.modelMapper = modelMapper;
    }

    // --- Métodos de CRUD para Trámites ---

    @Transactional
    public TramiteResponseDTO crearTramite(TramiteRequestDTO tramiteRequestDTO) {
        // Verificar que el solicitante existe
        Usuario solicitante = usuarioRepository.findById(tramiteRequestDTO.getIdSolicitante())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario solicitante no encontrado con ID: " + tramiteRequestDTO.getIdSolicitante()));

        // Verificar que la entidad existe
        Entidad entidad = entidadRepository.findById(tramiteRequestDTO.getIdEntidad())
                .orElseThrow(() -> new ResourceNotFoundException("Entidad no encontrada con ID: " + tramiteRequestDTO.getIdEntidad()));

        // Mapear DTO a Entidad
        Tramite tramite = modelMapper.map(tramiteRequestDTO, Tramite.class);
        tramite.setSolicitante(solicitante);
        tramite.setEntidad(entidad);
        // El revisor y el estado inicial se manejan por defecto en la entidad o se asignarán después

        Tramite tramiteGuardado = tramiteRepository.save(tramite);
        return modelMapper.map(tramiteGuardado, TramiteResponseDTO.class);
    }

    @Transactional(readOnly = true)
    public List<TramiteResponseDTO> getAllTramites() {
        return tramiteRepository.findAll().stream()
                .map(tramite -> modelMapper.map(tramite, TramiteResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TramiteResponseDTO getTramiteById(Long id) {
        Tramite tramite = tramiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trámite no encontrado con ID: " + id));
        return modelMapper.map(tramite, TramiteResponseDTO.class);
    }

    @Transactional
    public TramiteResponseDTO updateTramite(Long id, TramiteRequestDTO tramiteRequestDTO) {
        Tramite tramiteExistente = tramiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trámite no encontrado con ID: " + id));

        // Actualizar campos básicos
        tramiteExistente.setNombreTramite(tramiteRequestDTO.getNombreTramite());
        tramiteExistente.setDescripcion(tramiteRequestDTO.getDescripcion());
        tramiteExistente.setNumeroExpediente(tramiteRequestDTO.getNumeroExpediente());

        // Actualizar solicitante (si el ID cambia y es diferente del actual)
        if (tramiteRequestDTO.getIdSolicitante() != null &&
            !tramiteRequestDTO.getIdSolicitante().equals(tramiteExistente.getSolicitante().getIdUsuario())) {
            Usuario nuevoSolicitante = usuarioRepository.findById(tramiteRequestDTO.getIdSolicitante())
                    .orElseThrow(() -> new ResourceNotFoundException("Nuevo solicitante no encontrado con ID: " + tramiteRequestDTO.getIdSolicitante()));
            tramiteExistente.setSolicitante(nuevoSolicitante);
        }

        // Actualizar entidad (si el ID cambia y es diferente del actual)
        if (tramiteRequestDTO.getIdEntidad() != null &&
            !tramiteRequestDTO.getIdEntidad().equals(tramiteExistente.getEntidad().getId())) {
            Entidad nuevaEntidad = entidadRepository.findById(tramiteRequestDTO.getIdEntidad())
                    .orElseThrow(() -> new ResourceNotFoundException("Nueva entidad no encontrada con ID: " + tramiteRequestDTO.getIdEntidad()));
            tramiteExistente.setEntidad(nuevaEntidad);
        }

        // Actualizar revisor (puede ser asignado o reasignado)
        if (tramiteRequestDTO.getIdRevisor() != null) {
            Usuario nuevoRevisor = usuarioRepository.findById(tramiteRequestDTO.getIdRevisor())
                    .orElseThrow(() -> new ResourceNotFoundException("Revisor no encontrado con ID: " + tramiteRequestDTO.getIdRevisor()));
            tramiteExistente.setRevisor(nuevoRevisor);
        } else {
            tramiteExistente.setRevisor(null); // Si el ID del revisor es null, se desasigna
        }

        // Actualizar estado y comentarios del revisor si se proporcionan
        if (tramiteRequestDTO.getEstado() != null && !tramiteRequestDTO.getEstado().isEmpty()) {
            try {
                Tramite.EstadoTramite nuevoEstado = Tramite.EstadoTramite.valueOf(tramiteRequestDTO.getEstado().toUpperCase());
                tramiteExistente.setEstado(nuevoEstado);
                // Si el estado es final (APROBADO/RECHAZADO/CANCELADO), establecer fechaFinalizacion
                if (nuevoEstado == Tramite.EstadoTramite.APROBADO || nuevoEstado == Tramite.EstadoTramite.RECHAZADO || nuevoEstado == Tramite.EstadoTramite.CANCELADO) {
                    tramiteExistente.setFechaFinalizacion(java.time.LocalDateTime.now());
                } else {
                    tramiteExistente.setFechaFinalizacion(null); // Resetear si no es un estado final
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Estado de trámite inválido: " + tramiteRequestDTO.getEstado());
            }
        }
        tramiteExistente.setComentariosRevisor(tramiteRequestDTO.getComentariosRevisor());


        Tramite tramiteActualizado = tramiteRepository.save(tramiteExistente);
        return modelMapper.map(tramiteActualizado, TramiteResponseDTO.class);
    }

    @Transactional
    public void deleteTramite(Long id) {
        if (!tramiteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Trámite no encontrado con ID: " + id);
        }
        tramiteRepository.deleteById(id);
    }


    // --- Métodos específicos para el ciclo de vida del trámite ---

    @Transactional
    public TramiteResponseDTO asignarRevisor(Long idTramite, Long idRevisor) {
        Tramite tramite = tramiteRepository.findById(idTramite)
                .orElseThrow(() -> new ResourceNotFoundException("Trámite no encontrado con ID: " + idTramite));

        Usuario revisor = usuarioRepository.findById(idRevisor)
                .orElseThrow(() -> new ResourceNotFoundException("Revisor no encontrado con ID: " + idRevisor));

        // Validación adicional: Asegurarse de que el revisor tenga el rol adecuado (ej. REVISOR)
        if (!revisor.getRol().equals("REVISOR") && !revisor.getRol().equals("ADMIN_GLOBAL") && !revisor.getRol().equals("ADMIN_ENTIDAD")) {
             throw new IllegalArgumentException("El usuario con ID " + idRevisor + " no tiene un rol válido para ser revisor.");
        }
        // Validación: El revisor debe pertenecer a la misma entidad que el trámite (si aplica)
        if (!revisor.getEntidad().getId().equals(tramite.getEntidad().getId())) {
             throw new IllegalArgumentException("El revisor debe pertenecer a la misma entidad que el trámite.");
        }


        tramite.setRevisor(revisor);
        if (tramite.getEstado() == Tramite.EstadoTramite.PENDIENTE) {
            tramite.setEstado(Tramite.EstadoTramite.EN_REVISION); // Cambiar estado a En Revisión si estaba pendiente
        }

        Tramite tramiteActualizado = tramiteRepository.save(tramite);
        return modelMapper.map(tramiteActualizado, TramiteResponseDTO.class);
    }

    @Transactional
    public TramiteResponseDTO actualizarEstadoTramite(Long idTramite, TramiteEstadoUpdateDTO updateDTO) {
        Tramite tramite = tramiteRepository.findById(idTramite)
                .orElseThrow(() -> new ResourceNotFoundException("Trámite no encontrado con ID: " + idTramite));

        try {
            Tramite.EstadoTramite nuevoEstado = Tramite.EstadoTramite.valueOf(updateDTO.getNuevoEstado().toUpperCase());

            // Aquí puedes añadir lógica de negocio para transiciones de estado
            // Ej: No se puede pasar de APROBADO a PENDIENTE
            // Ej: Solo el revisor o admin pueden cambiar el estado a APROBADO/RECHAZADO
            // Por ahora, solo actualizamos el estado:
            tramite.setEstado(nuevoEstado);
            tramite.setComentariosRevisor(updateDTO.getComentariosRevisor());

            // Si el estado es final (APROBADO/RECHAZADO/CANCELADO), establecer fechaFinalizacion
            if (nuevoEstado == Tramite.EstadoTramite.APROBADO || nuevoEstado == Tramite.EstadoTramite.RECHAZADO || nuevoEstado == Tramite.EstadoTramite.CANCELADO) {
                tramite.setFechaFinalizacion(java.time.LocalDateTime.now());
            } else {
                tramite.setFechaFinalizacion(null); // Resetear si no es un estado final
            }

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado de trámite inválido: " + updateDTO.getNuevoEstado());
        }

        Tramite tramiteActualizado = tramiteRepository.save(tramite);
        return modelMapper.map(tramiteActualizado, TramiteResponseDTO.class);
    }
}

