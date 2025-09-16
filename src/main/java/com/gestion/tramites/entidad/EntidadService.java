package com.gestion.tramites.entidad;

import com.gestion.tramites.model.Entidad;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gestion.tramites.excepciones.ResourceNotFoundException;
import com.gestion.tramites.entidad.dto.EntidadDTO;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Para mapear listas

@Service
public class EntidadService {

    private final EntidadRepository entidadRepository;

    @Autowired
    public EntidadService(EntidadRepository entidadRepository) {
        this.entidadRepository = entidadRepository;
    }

    // Helper para convertir Entidad a EntidadDTO
    private EntidadDTO convertToDto(Entidad entidad) {
        if (entidad == null) {
            return null;
        }
        return new EntidadDTO(
            entidad.getId(),
            entidad.getNombre(),
            entidad.getNit(),
            entidad.getDireccion(),
            entidad.getTelefono(),
            entidad.getEmail(),
            entidad.getSitioWeb(),
            entidad.isActivo()
        );
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
    public EntidadDTO crearEntidad(EntidadDTO entidadDto) { // Recibe DTO
        Entidad entidad = convertToEntity(entidadDto); // Convierte a Entidad
        Entidad nuevaEntidad = entidadRepository.save(entidad);
        return convertToDto(nuevaEntidad); // Devuelve DTO
    }

    @Transactional(readOnly = true)
    public List<EntidadDTO> obtenerTodasLasEntidades() { // Devuelve lista de DTOs
        return entidadRepository.findAll().stream()
                .map(this::convertToDto) // Mapea cada Entidad a EntidadDTO
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<EntidadDTO> obtenerEntidadPorId(Long id) { // Devuelve Optional de DTO
        return entidadRepository.findById(id)
                .map(this::convertToDto); // Mapea a DTO si se encuentra
    }

    @Transactional
    public EntidadDTO actualizarEntidad(Long id, EntidadDTO entidadActualizadaDto) { // Recibe y devuelve DTO
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
    public EntidadDTO cambiarEstadoEntidad(Long id, boolean nuevoEstado) { // Devuelve DTO
        return entidadRepository.findById(id).map(entidad -> {
            entidad.setActivo(nuevoEstado);
            Entidad entidadGuardada = entidadRepository.save(entidad);
            return convertToDto(entidadGuardada); // Devuelve DTO
        }).orElseThrow(() -> new ResourceNotFoundException("Entidad", "id", id));
    }
}
