package com.gestion.tramites.service;

import com.gestion.tramites.model.Entidad;
import com.gestion.tramites.model.EntidadGubernamental;
import com.gestion.tramites.repository.EntidadRepository;
import com.gestion.tramites.repository.EntidadGubernamentalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gestion.tramites.exception.ResourceNotFoundException;
import com.gestion.tramites.dto.EntidadDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EntidadService {

    private static final Logger logger = LoggerFactory.getLogger(EntidadService.class);

    private final EntidadRepository entidadRepository;
    private final EntidadGubernamentalRepository entidadGubernamentalRepository;
    private final VerificacionEntidadGubernamentalService verificacionService;

    @Autowired
    public EntidadService(EntidadRepository entidadRepository,
                         EntidadGubernamentalRepository entidadGubernamentalRepository,
                         VerificacionEntidadGubernamentalService verificacionService) {
        this.entidadRepository = entidadRepository;
        this.entidadGubernamentalRepository = entidadGubernamentalRepository;
        this.verificacionService = verificacionService;
    }

    // Helper para convertir Entidad a EntidadDTO
    private EntidadDTO convertToDto(Entidad entidad) {
        if (entidad == null) {
            return null;
        }
        return new EntidadDTO(entidad.getId(), entidad.getNombre(), entidad.getNit(),
                entidad.getDireccion(), entidad.getTelefono(), entidad.getEmail(),
                entidad.getSitioWeb(), entidad.isActivo());
    }

    // Helper para convertir EntidadDTO a Entidad
    private Entidad convertToEntity(EntidadDTO entidadDto) {
        if (entidadDto == null) {
            return null;
        }
        Entidad entidad = new Entidad();
        entidad.setId(entidadDto.getId()); // ID puede ser nulo para creación
        entidad.setNombre(entidadDto.getNombre());
        entidad.setNit(entidadDto.getNit());
        entidad.setDireccion(entidadDto.getDireccion());
        entidad.setTelefono(entidadDto.getTelefono());
        entidad.setEmail(entidadDto.getEmail());
        entidad.setSitioWeb(entidadDto.getSitioWeb());
        entidad.setActivo(entidadDto.isActivo());
        return entidad;
    }

    @Transactional
    public EntidadDTO crearEntidad(EntidadDTO entidadDto) {
        Entidad entidad = convertToEntity(entidadDto);

        // Validar si es una entidad gubernamental que debe estar verificada
        validarSiRequiereVerificacionGubernamental(entidad);

        Entidad nuevaEntidad = entidadRepository.save(entidad);

        logger.info("Entidad creada: {} (ID: {})", nuevaEntidad.getNombre(), nuevaEntidad.getId());

        return convertToDto(nuevaEntidad);
    }

    @Transactional(readOnly = true)
    public List<EntidadDTO> obtenerTodasLasEntidades() { // Devuelve lista de DTOs
        return entidadRepository.findAll().stream().map(this::convertToDto) // Mapea cada Entidad a
                                                                            // EntidadDTO
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<EntidadDTO> obtenerEntidadPorId(Long id) { // Devuelve Optional de DTO
        return entidadRepository.findById(id).map(this::convertToDto); // Mapea a DTO si se
                                                                       // encuentra
    }

    @Transactional
    public EntidadDTO actualizarEntidad(Long id, EntidadDTO entidadActualizadaDto) { // Recibe y
                                                                                     // devuelve DTO
        return entidadRepository.findById(id).map(entidad -> {
            // Actualiza los campos de la entidad existente con los datos del DTO
            entidad.setNombre(entidadActualizadaDto.getNombre());
            entidad.setNit(entidadActualizadaDto.getNit());
            entidad.setDireccion(entidadActualizadaDto.getDireccion());
            entidad.setTelefono(entidadActualizadaDto.getTelefono());
            entidad.setEmail(entidadActualizadaDto.getEmail());
            entidad.setSitioWeb(entidadActualizadaDto.getSitioWeb());
            entidad.setActivo(entidadActualizadaDto.isActivo());
            Entidad entidadGuardada = entidadRepository.save(entidad);
            return convertToDto(entidadGuardada); // Devuelve DTO
        }).orElseThrow(() -> new ResourceNotFoundException("Entidad", "id", id));
    }

    @Transactional
    public void eliminarEntidad(Long id) {
        if (!entidadRepository.existsById(id)) {
            throw new ResourceNotFoundException("Entidad", "id", id);
        }
        entidadRepository.deleteById(id);
    }

    @Transactional
    public EntidadDTO cambiarEstadoEntidad(Long id, boolean nuevoEstado) {
        return entidadRepository.findById(id).map(entidad -> {
            // Validar si es una entidad gubernamental antes de cambiar estado
            if (nuevoEstado) {
                validarSiRequiereVerificacionGubernamental(entidad);
            }

            entidad.setActivo(nuevoEstado);
            Entidad entidadGuardada = entidadRepository.save(entidad);

            logger.info("Estado de entidad {} cambiado a: {}", entidad.getNombre(), nuevoEstado);

            return convertToDto(entidadGuardada);
        }).orElseThrow(() -> new ResourceNotFoundException("Entidad", "id", id));
    }

    /**
     * Valida si una entidad requiere verificación gubernamental antes de ser activada
     */
    private void validarSiRequiereVerificacionGubernamental(Entidad entidad) {
        if (entidad.getEmail() != null && esEmailGubernamental(entidad.getEmail())) {
            // Verificar si existe una entidad gubernamental verificada correspondiente
            Optional<EntidadGubernamental> entidadGubernamental =
                entidadGubernamentalRepository.findByEmailOficial(entidad.getEmail());

            if (entidadGubernamental.isEmpty() || !entidadGubernamental.get().estaVerificada()) {
                throw new IllegalStateException(
                    "Las entidades con dominios gubernamentales deben estar verificadas. " +
                    "Debe registrarse primero como entidad gubernamental en el sistema de verificación."
                );
            }

            logger.info("Entidad gubernamental verificada encontrada para: {}", entidad.getEmail());
        }

        if (entidad.getSitioWeb() != null && esDominioGubernamental(entidad.getSitioWeb())) {
            // Verificar si el sitio web corresponde a una entidad gubernamental verificada
            String dominio = extraerDominio(entidad.getSitioWeb());
            Optional<EntidadGubernamental> entidadGubernamental =
                entidadGubernamentalRepository.findByDominioOficial(dominio);

            if (entidadGubernamental.isEmpty() || !entidadGubernamental.get().estaVerificada()) {
                throw new IllegalStateException(
                    "Las entidades con sitios web gubernamentales deben estar verificadas. " +
                    "Debe registrarse primero como entidad gubernamental en el sistema de verificación."
                );
            }

            logger.info("Entidad gubernamental verificada encontrada para dominio: {}", dominio);
        }
    }

    /**
     * Verifica si un email pertenece a un dominio gubernamental
     */
    private boolean esEmailGubernamental(String email) {
        return email.endsWith(".gov.co") || email.endsWith(".edu.co");
    }

    /**
     * Verifica si un dominio es gubernamental
     */
    private boolean esDominioGubernamental(String sitioWeb) {
        String dominio = extraerDominio(sitioWeb);
        return dominio.endsWith(".gov.co") || dominio.contains("alcaldia") || dominio.contains("curaduria");
    }

    /**
     * Extrae el dominio de una URL
     */
    private String extraerDominio(String url) {
        if (url == null) return "";

        // Remover protocolo si está presente
        url = url.replaceFirst("^https?://", "");

        // Remover path si está presente
        url = url.split("/")[0];

        // Convertir a minúsculas
        return url.toLowerCase().trim();
    }

    /**
     * Obtiene entidades que están vinculadas a entidades gubernamentales verificadas
     */
    @Transactional(readOnly = true)
    public List<EntidadDTO> obtenerEntidadesGubernamentalesVerificadas() {
        List<EntidadGubernamental> entidadesGubernamentales =
            entidadGubernamentalRepository.findByEstadoVerificacionAndActivo(
                EntidadGubernamental.EstadoVerificacion.VERIFICADA);

        return entidadesGubernamentales.stream()
            .filter(eg -> eg.getEntidadSistema() != null)
            .map(eg -> convertToDto(eg.getEntidadSistema()))
            .collect(Collectors.toList());
    }

    /**
     * Valida que una entidad esté autorizada para operaciones críticas
     */
    @Transactional(readOnly = true)
    public void validarEntidadParaOperacionCritica(Long entidadId) {
        Entidad entidad = entidadRepository.findById(entidadId)
            .orElseThrow(() -> new ResourceNotFoundException("Entidad", "id", entidadId));

        if (!entidad.isActivo()) {
            throw new IllegalStateException("La entidad no está activa");
        }

        // Si la entidad tiene características gubernamentales, debe estar verificada
        if (esEmailGubernamental(entidad.getEmail()) || esDominioGubernamental(entidad.getSitioWeb())) {
            // Buscar entidad gubernamental correspondiente
            Optional<EntidadGubernamental> entidadGubernamental = Optional.empty();

            if (entidad.getEmail() != null) {
                entidadGubernamental = entidadGubernamentalRepository.findByEmailOficial(entidad.getEmail());
            }

            if (entidadGubernamental.isEmpty() && entidad.getSitioWeb() != null) {
                String dominio = extraerDominio(entidad.getSitioWeb());
                entidadGubernamental = entidadGubernamentalRepository.findByDominioOficial(dominio);
            }

            if (entidadGubernamental.isEmpty() || !entidadGubernamental.get().estaActiva()) {
                throw new IllegalStateException(
                    "Las entidades gubernamentales deben estar verificadas para realizar operaciones críticas");
            }
        }

        logger.debug("Entidad {} validada para operación crítica", entidad.getNombre());
    }

    /**
     * Sincroniza una entidad gubernamental verificada con el sistema principal
     */
    @Transactional
    public EntidadDTO sincronizarEntidadGubernamental(Long entidadGubernamentalId) {
        EntidadGubernamental entidadGubernamental = entidadGubernamentalRepository.findById(entidadGubernamentalId)
            .orElseThrow(() -> new ResourceNotFoundException("EntidadGubernamental", "id", entidadGubernamentalId));

        if (!entidadGubernamental.estaVerificada()) {
            throw new IllegalStateException("Solo se pueden sincronizar entidades gubernamentales verificadas");
        }

        Entidad entidadSistema = entidadGubernamental.getEntidadSistema();
        if (entidadSistema == null) {
            // Crear nueva entidad en el sistema principal
            entidadSistema = new Entidad();
            entidadSistema.setNombre(entidadGubernamental.getNombre());
            entidadSistema.setNit(entidadGubernamental.getNit());
            entidadSistema.setDireccion(entidadGubernamental.getDireccionFisica());
            entidadSistema.setTelefono(entidadGubernamental.getTelefonoOficial());
            entidadSistema.setEmail(entidadGubernamental.getEmailOficial());
            entidadSistema.setSitioWeb(entidadGubernamental.getSitioWebOficial());
            entidadSistema.setActivo(true);

            entidadSistema = entidadRepository.save(entidadSistema);

            // Vincular con la entidad gubernamental
            entidadGubernamental.setEntidadSistema(entidadSistema);
            entidadGubernamentalRepository.save(entidadGubernamental);

            logger.info("Entidad gubernamental {} sincronizada con el sistema principal (ID: {})",
                    entidadGubernamental.getNombre(), entidadSistema.getId());
        }

        return convertToDto(entidadSistema);
    }
}
