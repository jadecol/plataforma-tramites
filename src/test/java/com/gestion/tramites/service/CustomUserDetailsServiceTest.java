package com.gestion.tramites.service;

import com.gestion.tramites.model.Usuario;
import com.gestion.tramites.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private Usuario testUsuario;

    @BeforeEach
    void setUp() {
        testUsuario = new Usuario();
        testUsuario.setIdUsuario(1L);
        testUsuario.setCorreoElectronico("test@example.com");
        testUsuario.setNombreCompleto("Test User");
        testUsuario.setContrasenaHash("$2a$10$hashedPassword");
        testUsuario.setRol(Usuario.Rol.ADMIN_GLOBAL);
        testUsuario.setEstaActivo(true);
        testUsuario.setFechaCreacion(LocalDateTime.now());
        testUsuario.setTipoDocumento("CC");
        testUsuario.setNumeroDocumento("12345678");
    }

    @Test
    void loadUserByUsername_ExistingUser_ReturnsCustomUserDetails() {
        // Arrange
        when(usuarioRepository.findByCorreoElectronico("test@example.com"))
                .thenReturn(Optional.of(testUsuario));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

        // Assert
        assertNotNull(userDetails);
        assertTrue(userDetails instanceof CustomUserDetails);

        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        assertEquals("test@example.com", customUserDetails.getUsername());
        assertEquals("$2a$10$hashedPassword", customUserDetails.getPassword());
        assertTrue(customUserDetails.isEnabled());
        assertTrue(customUserDetails.isAccountNonExpired());
        assertTrue(customUserDetails.isAccountNonLocked());
        assertTrue(customUserDetails.isCredentialsNonExpired());

        // Verificar que tiene autoridades
        assertFalse(customUserDetails.getAuthorities().isEmpty());

        verify(usuarioRepository).findByCorreoElectronico("test@example.com");
    }

    @Test
    void loadUserByUsername_NonExistingUser_ThrowsUsernameNotFoundException() {
        // Arrange
        String correoInexistente = "noexiste@example.com";
        when(usuarioRepository.findByCorreoElectronico(correoInexistente))
                .thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(correoInexistente)
        );

        assertEquals("Usuario no encontrado con correo: " + correoInexistente,
                exception.getMessage());
        verify(usuarioRepository).findByCorreoElectronico(correoInexistente);
    }

    @Test
    void loadUserByUsername_InactiveUser_ReturnsUserDetailsButDisabled() {
        // Arrange
        testUsuario.setEstaActivo(false);
        when(usuarioRepository.findByCorreoElectronico("test@example.com"))
                .thenReturn(Optional.of(testUsuario));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

        // Assert
        assertNotNull(userDetails);
        assertTrue(userDetails instanceof CustomUserDetails);

        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        assertEquals("test@example.com", customUserDetails.getUsername());
        assertFalse(customUserDetails.isEnabled()); // Usuario inactivo

        verify(usuarioRepository).findByCorreoElectronico("test@example.com");
    }

    @Test
    void loadUserByUsername_UserWithDifferentRole_ReturnsCorrectAuthorities() {
        // Arrange
        testUsuario.setRol(Usuario.Rol.SOLICITANTE);
        when(usuarioRepository.findByCorreoElectronico("test@example.com"))
                .thenReturn(Optional.of(testUsuario));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

        // Assert
        assertNotNull(userDetails);
        assertTrue(userDetails instanceof CustomUserDetails);

        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        assertFalse(customUserDetails.getAuthorities().isEmpty());

        // Verificar que contiene la autoridad correcta
        boolean hasUserRole = customUserDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_SOLICITANTE"));
        assertTrue(hasUserRole);

        verify(usuarioRepository).findByCorreoElectronico("test@example.com");
    }

    @Test
    void loadUserByUsername_EmptyEmail_ThrowsUsernameNotFoundException() {
        // Arrange
        String correoVacio = "";
        when(usuarioRepository.findByCorreoElectronico(correoVacio))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(correoVacio));

        verify(usuarioRepository).findByCorreoElectronico(correoVacio);
    }

    @Test
    void loadUserByUsername_NullEmail_ThrowsUsernameNotFoundException() {
        // Arrange
        when(usuarioRepository.findByCorreoElectronico(null))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(null));

        verify(usuarioRepository).findByCorreoElectronico(null);
    }
}