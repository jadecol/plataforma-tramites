package com.gestion.tramites.usuario;

import com.gestion.tramites.excepciones.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import jakarta.validation.Valid; // Para usar validaciones en DTOs (opcional, pero buena práctica)

@RestController
@RequestMapping("/api/v1/usuarios") // Prefijo de la URL para todos los endpoints de usuarios
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Endpoint para crear un nuevo usuario
    // Solo accesible por un ADMIN_GLOBAL o ADMIN_ENTIDAD (si crea usuarios dentro de su entidad)
    @PreAuthorize("hasAnyRole('ADMIN_GLOBAL', 'ADMIN_ENTIDAD')")
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> crearUsuario(@Valid @RequestBody UsuarioDTO usuarioDto) { // Recibe UsuarioDTO
        try {
            UsuarioResponseDTO nuevoUsuario = usuarioService.crearUsuario(usuarioDto); // Servicio devuelve ResponseDTO
            return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Por ejemplo, si el email ya existe
        }
    }

    // Endpoint para obtener todos los usuarios
    // Dependiendo del rol, se podrían filtrar (ej. ADMIN_ENTIDAD solo ve usuarios de su entidad)
    @PreAuthorize("hasAnyRole('ADMIN_GLOBAL', 'ADMIN_ENTIDAD')")
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> obtenerTodosLosUsuarios() { // Devuelve lista de ResponseDTOs
        List<UsuarioResponseDTO> usuarios = usuarioService.obtenerTodosLosUsuarios();
        return new ResponseEntity<>(usuarios, HttpStatus.OK);
    }

    // Endpoint para obtener un usuario por su ID
    // Un usuario podría ver su propio perfil
    @PreAuthorize("hasAnyRole('ADMIN_GLOBAL', 'ADMIN_ENTIDAD', 'SOLICITANTE', 'REVISOR')") // Ajustar roles según necesidad
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtenerUsuarioPorId(@PathVariable Long id) { // Devuelve ResponseDTO
        try {
            return usuarioService.obtenerUsuarioPorId(id)
                    .map(usuarioDto -> new ResponseEntity<>(usuarioDto, HttpStatus.OK))
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        } catch (ResourceNotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Endpoint para actualizar un usuario
    @PreAuthorize("hasAnyRole('ADMIN_GLOBAL', 'ADMIN_ENTIDAD')") // O el propio usuario para su perfil
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioDTO usuarioDto) { // Recibe y devuelve DTO
        try {
            UsuarioResponseDTO usuarioActualizado = usuarioService.actualizarUsuario(id, usuarioDto);
            return new ResponseEntity<>(usuarioActualizado, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Endpoint para cambiar el estado de un usuario (activo/inactivo)
    @PreAuthorize("hasAnyRole('ADMIN_GLOBAL', 'ADMIN_ENTIDAD')")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<UsuarioResponseDTO> cambiarEstadoUsuario(@PathVariable Long id, @RequestParam boolean estaActivo) { // Devuelve DTO
        try {
            UsuarioResponseDTO usuarioActualizado = usuarioService.cambiarEstadoUsuario(id, estaActivo);
            return new ResponseEntity<>(usuarioActualizado, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Endpoint para eliminar un usuario (considerar eliminación lógica en su lugar)
    @PreAuthorize("hasRole('ADMIN_GLOBAL')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        try {
            usuarioService.eliminarUsuario(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ResourceNotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
