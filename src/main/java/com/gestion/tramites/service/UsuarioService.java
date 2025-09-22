package com.gestion.tramites.service;

import com.gestion.tramites.model.Usuario;
import com.gestion.tramites.model.Entidad; // Importa Entidad
import com.gestion.tramites.repository.UsuarioRepository;
import com.gestion.tramites.repository.EntidadRepository; // Importa EntidadRepository
import com.gestion.tramites.exception.ResourceNotFoundException;
import com.gestion.tramites.dto.UsuarioDTO; // Para recibir datos
import com.gestion.tramites.dto.UsuarioResponseDTO; // Para devolver datos
import com.gestion.tramites.dto.EntidadDTO; // Para mapear la entidad en el response DTO
import com.gestion.tramites.util.PasswordGenerator; // Para encriptar contraseñas

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EntidadRepository entidadRepository; // Inyecta EntidadRepository
    private final PasswordGenerator passwordGenerator; // Inyecta PasswordGenerator

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository, EntidadRepository entidadRepository,
            PasswordGenerator passwordGenerator) {
        this.usuarioRepository = usuarioRepository;
        this.entidadRepository = entidadRepository;
        this.passwordGenerator = passwordGenerator;
    }

    // Helper para convertir Usuario a UsuarioResponseDTO
    private UsuarioResponseDTO convertToResponseDto(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setNombreCompleto(usuario.getNombreCompleto());
        dto.setTipoDocumento(usuario.getTipoDocumento());
        dto.setNumeroDocumento(usuario.getNumeroDocumento());
        dto.setCorreoElectronico(usuario.getCorreoElectronico());
        dto.setTelefono(usuario.getTelefono());
        dto.setRol(usuario.getRol().name());
        // contrasenaHash no se incluye por seguridad
        dto.setFechaCreacion(usuario.getFechaCreacion());
        dto.setFechaUltimaSesion(usuario.getFechaUltimaSesion());
        dto.setEstaActivo(usuario.getEstaActivo());
        dto.setMatriculaProfesional(usuario.getMatriculaProfesional());
        dto.setExperienciaAcreditada(usuario.getExperienciaAcreditada());

        // Mapea la Entidad a EntidadDTO si existe
        if (usuario.getEntidad() != null) {
            EntidadDTO entidadDto =
                    new EntidadDTO(usuario.getEntidad().getId(), usuario.getEntidad().getNombre(),
                            usuario.getEntidad().getNit(), null, null, null, null, // No necesitas
                                                                                   // todos los
                                                                                   // detalles en el
                                                                                   // DTO de
                                                                                   // usuario, solo
                                                                                   // los básicos
                            usuario.getEntidad().isActivo());
            dto.setEntidad(entidadDto);
        }
        return dto;
    }

    // Helper para convertir UsuarioDTO a Usuario (para creación/actualización)
    private Usuario convertToEntity(UsuarioDTO usuarioDto) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(usuarioDto.getIdUsuario()); // Puede ser nulo para creación
        usuario.setNombreCompleto(usuarioDto.getNombreCompleto());
        usuario.setTipoDocumento(usuarioDto.getTipoDocumento());
        usuario.setNumeroDocumento(usuarioDto.getNumeroDocumento());
        usuario.setCorreoElectronico(usuarioDto.getCorreoElectronico());
        usuario.setTelefono(usuarioDto.getTelefono());
        usuario.setRol(Usuario.Rol.valueOf(usuarioDto.getRol()));
        // Contraseña se maneja por separado en crear/actualizar
        usuario.setEstaActivo(usuarioDto.getEstaActivo());
        usuario.setMatriculaProfesional(usuarioDto.getMatriculaProfesional());
        usuario.setExperienciaAcreditada(usuarioDto.getExperienciaAcreditada());

        // Asociar Entidad si se proporciona un idEntidad
        if (usuarioDto.getIdEntidad() != null) {
            Entidad entidad = entidadRepository.findById(usuarioDto.getIdEntidad())
                    .orElseThrow(() -> new ResourceNotFoundException("Entidad", "id",
                            usuarioDto.getIdEntidad()));
            usuario.setEntidad(entidad);
        } else {
            usuario.setEntidad(null); // Asegura que si no se proporciona, sea null (ej. para
                                      // SOLICITANTE)
        }
        return usuario;
    }


    @Transactional
    public UsuarioResponseDTO crearUsuario(UsuarioDTO usuarioDto) {
        // Verificar si el correo electrónico ya existe
        if (usuarioRepository.findByCorreoElectronico(usuarioDto.getCorreoElectronico())
                .isPresent()) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado.");
        }

        Usuario usuario = convertToEntity(usuarioDto);
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setEstaActivo(true); // Nuevo usuario por defecto activo

        // Hashear la contraseña antes de guardar
        if (usuarioDto.getContrasenaHash() != null && !usuarioDto.getContrasenaHash().isEmpty()) {
            usuario.setContrasenaHash(passwordGenerator.encode(usuarioDto.getContrasenaHash()));
        } else {
            throw new IllegalArgumentException(
                    "La contraseña es obligatoria para nuevos usuarios.");
        }

        Usuario nuevoUsuario = usuarioRepository.save(usuario);
        return convertToResponseDto(nuevoUsuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll().stream().map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<UsuarioResponseDTO> obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id).map(this::convertToResponseDto);
    }

    @Transactional
    public UsuarioResponseDTO actualizarUsuario(Long id, UsuarioDTO usuarioDto) {
        return usuarioRepository.findById(id).map(usuarioExistente -> {
            // No permitir cambiar el correo electrónico si ya existe y es diferente
            if (!usuarioExistente.getCorreoElectronico().equals(usuarioDto.getCorreoElectronico())
                    && usuarioRepository.findByCorreoElectronico(usuarioDto.getCorreoElectronico())
                            .isPresent()) {
                throw new IllegalArgumentException("El nuevo correo electrónico ya está en uso.");
            }

            usuarioExistente.setNombreCompleto(usuarioDto.getNombreCompleto());
            usuarioExistente.setTipoDocumento(usuarioDto.getTipoDocumento());
            usuarioExistente.setNumeroDocumento(usuarioDto.getNumeroDocumento());
            usuarioExistente.setCorreoElectronico(usuarioDto.getCorreoElectronico());
            usuarioExistente.setTelefono(usuarioDto.getTelefono());
            usuarioExistente.setRol(Usuario.Rol.valueOf(usuarioDto.getRol())); // Actualiza el rol

            // Actualizar contraseña solo si se proporciona una nueva
            if (usuarioDto.getContrasenaHash() != null
                    && !usuarioDto.getContrasenaHash().isEmpty()) {
                usuarioExistente.setContrasenaHash(
                        passwordGenerator.encode(usuarioDto.getContrasenaHash()));
            }

            usuarioExistente.setEstaActivo(usuarioDto.getEstaActivo());
            usuarioExistente.setMatriculaProfesional(usuarioDto.getMatriculaProfesional());
            usuarioExistente.setExperienciaAcreditada(usuarioDto.getExperienciaAcreditada());

            // Actualizar la entidad asociada
            if (usuarioDto.getIdEntidad() != null) {
                Entidad entidad = entidadRepository.findById(usuarioDto.getIdEntidad())
                        .orElseThrow(() -> new ResourceNotFoundException("Entidad", "id",
                                usuarioDto.getIdEntidad()));
                usuarioExistente.setEntidad(entidad);
            } else {
                usuarioExistente.setEntidad(null); // Si el idEntidad es nulo, desasociar
            }

            Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);
            return convertToResponseDto(usuarioActualizado);
        }).orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario", "id", id);
        }
        usuarioRepository.deleteById(id);
    }

    @Transactional
    public UsuarioResponseDTO cambiarEstadoUsuario(Long id, boolean nuevoEstado) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setEstaActivo(nuevoEstado);
            Usuario usuarioActualizado = usuarioRepository.save(usuario);
            return convertToResponseDto(usuarioActualizado);
        }).orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
    }
}
