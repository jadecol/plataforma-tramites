package com.gestion.tramites.service;

import com.gestion.tramites.dto.UsuarioDTO;
import com.gestion.tramites.dto.UsuarioResponseDTO;
import com.gestion.tramites.model.Entidad;
import com.gestion.tramites.model.Usuario;
import com.gestion.tramites.repository.EntidadRepository;
import com.gestion.tramites.repository.UsuarioRepository;
import com.gestion.tramites.exception.ResourceNotFoundException;
import com.gestion.tramites.util.PasswordGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private EntidadRepository entidadRepository;
    @Mock
    private PasswordGenerator passwordGenerator;

    @InjectMocks
    private UsuarioService usuarioService;

    private UsuarioDTO usuarioDTO;
    private Usuario usuario;
    private Entidad entidad;

    @BeforeEach
    void setUp() {
        entidad = new Entidad();
        entidad.setId(1L);
        entidad.setNombre("Entidad Test");

        usuarioDTO = new UsuarioDTO();
        usuarioDTO.setNombreCompleto("Test User");
        usuarioDTO.setCorreoElectronico("test@example.com");
        usuarioDTO.setContrasenaHash("password123");
        usuarioDTO.setRol("SOLICITANTE");
        usuarioDTO.setIdEntidad(1L);

        usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setNombreCompleto("Test User");
        usuario.setCorreoElectronico("test@example.com");
        usuario.setContrasenaHash("hashedPassword");
        usuario.setRol(Usuario.Rol.SOLICITANTE);
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setEstaActivo(true);
        usuario.setEntidad(entidad);
    }

    @Test
    void crearUsuario_cuandoEsExitoso_deberiaRetornarUsuarioResponseDTO() {
        when(usuarioRepository.findByCorreoElectronico(anyString())).thenReturn(Optional.empty());
        when(entidadRepository.findById(anyLong())).thenReturn(Optional.of(entidad));
        when(passwordGenerator.encode(anyString())).thenReturn("hashedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        UsuarioResponseDTO result = usuarioService.crearUsuario(usuarioDTO);

        assertNotNull(result);
        assertEquals("test@example.com", result.getCorreoElectronico());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void crearUsuario_cuandoCorreoYaExiste_deberiaLanzarIllegalArgumentException() {
        when(usuarioRepository.findByCorreoElectronico(anyString())).thenReturn(Optional.of(usuario));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.crearUsuario(usuarioDTO);
        });

        assertEquals("El correo electrónico ya está registrado.", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void crearUsuario_cuandoContrasenaEsNula_deberiaLanzarIllegalArgumentException() {
        when(entidadRepository.findById(anyLong())).thenReturn(Optional.of(entidad)); // Mock entidad for this test
        usuarioDTO.setContrasenaHash(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.crearUsuario(usuarioDTO);
        });

        assertEquals("La contraseña es obligatoria para nuevos usuarios.", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void crearUsuario_cuandoContrasenaEstaVacia_deberiaLanzarIllegalArgumentException() {
        when(entidadRepository.findById(anyLong())).thenReturn(Optional.of(entidad)); // Mock entidad for this test
        usuarioDTO.setContrasenaHash("");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.crearUsuario(usuarioDTO);
        });

        assertEquals("La contraseña es obligatoria para nuevos usuarios.", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void crearUsuario_cuandoEntidadNoExiste_deberiaLanzarResourceNotFoundException() {
        when(usuarioRepository.findByCorreoElectronico(anyString())).thenReturn(Optional.empty());
        when(entidadRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            usuarioService.crearUsuario(usuarioDTO);
        });

                assertEquals("Entidad no encontrado con id : '1'", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void obtenerTodosLosUsuarios_cuandoExistenUsuarios_deberiaRetornarListaDeDTOs() {
        Usuario anotherUser = new Usuario();
        anotherUser.setIdUsuario(2L);
        anotherUser.setCorreoElectronico("another@example.com");
        anotherUser.setRol(Usuario.Rol.ADMIN_GLOBAL);
        anotherUser.setEstaActivo(true);

        when(usuarioRepository.findAll()).thenReturn(Arrays.asList(usuario, anotherUser));

        List<UsuarioResponseDTO> results = usuarioService.obtenerTodosLosUsuarios();

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("test@example.com", results.get(0).getCorreoElectronico());
        assertEquals("another@example.com", results.get(1).getCorreoElectronico());
    }

    @Test
    void obtenerTodosLosUsuarios_cuandoNoExistenUsuarios_deberiaRetornarListaVacia() {
        when(usuarioRepository.findAll()).thenReturn(Arrays.asList());

        List<UsuarioResponseDTO> results = usuarioService.obtenerTodosLosUsuarios();

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void obtenerUsuarioPorId_cuandoUsuarioExiste_deberiaRetornarOptionalConDTO() {
        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.of(usuario));

        Optional<UsuarioResponseDTO> result = usuarioService.obtenerUsuarioPorId(1L);

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getCorreoElectronico());
    }

    @Test
    void obtenerUsuarioPorId_cuandoUsuarioNoExiste_deberiaRetornarOptionalVacio() {
        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<UsuarioResponseDTO> result = usuarioService.obtenerUsuarioPorId(99L);

        assertFalse(result.isPresent());
    }

    @Test
    void actualizarUsuario_cuandoEsExitoso_deberiaRetornarUsuarioResponseDTO() {
        UsuarioDTO updateDTO = new UsuarioDTO();
        updateDTO.setNombreCompleto("Updated Name");
        updateDTO.setCorreoElectronico("updated@example.com");
        updateDTO.setRol("ADMIN_GLOBAL");
        updateDTO.setContrasenaHash("newPassword");
        updateDTO.setIdEntidad(1L);

        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByCorreoElectronico(anyString())).thenReturn(Optional.empty()); // New email not in use
        when(entidadRepository.findById(anyLong())).thenReturn(Optional.of(entidad));
        when(passwordGenerator.encode(anyString())).thenReturn("newHashedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        UsuarioResponseDTO result = usuarioService.actualizarUsuario(1L, updateDTO);

        assertNotNull(result);
        assertEquals("Updated Name", result.getNombreCompleto());
        assertEquals("updated@example.com", result.getCorreoElectronico());
        assertEquals("ADMIN_GLOBAL", result.getRol());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void actualizarUsuario_cuandoUsuarioNoExiste_deberiaLanzarResourceNotFoundException() {
        UsuarioDTO updateDTO = new UsuarioDTO();
        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            usuarioService.actualizarUsuario(99L, updateDTO);
        });

        assertEquals("Usuario no encontrado con id : '99'", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void actualizarUsuario_cuandoNuevoCorreoYaExiste_deberiaLanzarIllegalArgumentException() {
        Usuario existingUserWithNewEmail = new Usuario();
        existingUserWithNewEmail.setIdUsuario(2L);
        existingUserWithNewEmail.setCorreoElectronico("newemail@example.com");

        UsuarioDTO updateDTO = new UsuarioDTO();
        updateDTO.setCorreoElectronico("newemail@example.com"); // Email different from original
        updateDTO.setNombreCompleto("Test User"); // Keep other fields to avoid nulls

        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByCorreoElectronico("newemail@example.com")).thenReturn(Optional.of(existingUserWithNewEmail));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.actualizarUsuario(1L, updateDTO);
        });

        assertEquals("El nuevo correo electrónico ya está en uso.", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void actualizarUsuario_cuandoEntidadNoExiste_deberiaLanzarResourceNotFoundException() {
        UsuarioDTO updateDTO = new UsuarioDTO();
        updateDTO.setNombreCompleto("Updated Name");
        updateDTO.setCorreoElectronico("test@example.com"); // Same email
        updateDTO.setRol("SOLICITANTE"); // Add valid role
        updateDTO.setIdEntidad(99L); // Non-existent entity

        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.of(usuario));
        when(entidadRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            usuarioService.actualizarUsuario(1L, updateDTO);
        });

        assertEquals("Entidad no encontrado con id : '99'", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void eliminarUsuario_cuandoEsExitoso_deberiaLlamarDeleteById() {
        when(usuarioRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(usuarioRepository).deleteById(anyLong());

        usuarioService.eliminarUsuario(1L);

        verify(usuarioRepository, times(1)).existsById(1L);
        verify(usuarioRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminarUsuario_cuandoUsuarioNoExiste_deberiaLanzarResourceNotFoundException() {
        when(usuarioRepository.existsById(anyLong())).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            usuarioService.eliminarUsuario(99L);
        });

        assertEquals("Usuario no encontrado con id : '99'", exception.getMessage());
        verify(usuarioRepository, never()).deleteById(anyLong());
    }

    @Test
    void cambiarEstadoUsuario_cuandoEsExitoso_deberiaActualizarEstadoYRetornarDTO() {
        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        UsuarioResponseDTO result = usuarioService.cambiarEstadoUsuario(1L, false);

        assertNotNull(result);
        assertFalse(result.getEstaActivo());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void cambiarEstadoUsuario_cuandoUsuarioNoExiste_deberiaLanzarResourceNotFoundException() {
        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            usuarioService.cambiarEstadoUsuario(99L, true);
        });

        assertEquals("Usuario no encontrado con id : '99'", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}
