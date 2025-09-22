package com.gestion.tramites.service;

import com.gestion.tramites.dto.auth.JwtResponse;
import com.gestion.tramites.dto.auth.LoginRequest;
import com.gestion.tramites.dto.auth.RegisterRequest;
import com.gestion.tramites.model.Usuario;
import com.gestion.tramites.repository.UsuarioRepository;
import com.gestion.tramites.security.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private Usuario testUsuario;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

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

        loginRequest = new LoginRequest();
        loginRequest.setCorreoElectronico("test@example.com");
        loginRequest.setContrasena("password123");

        registerRequest = new RegisterRequest();
        registerRequest.setCorreoElectronico("new@example.com");
        registerRequest.setNombreCompleto("New User");
        registerRequest.setTipoDocumento("CC");
        registerRequest.setNumeroDocumento("12345678");
        registerRequest.setRol("SOLICITANTE");
        registerRequest.setContrasena("newPassword123");
    }

    @Test
    void authenticateUser_ValidCredentials_ReturnsJwtResponse() {
        // Arrange
        when(usuarioRepository.findByCorreoElectronico("test@example.com"))
                .thenReturn(Optional.of(testUsuario));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword"))
                .thenReturn(true);
        when(jwtUtil.generateToken(any())).thenReturn("mock-jwt-token");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(testUsuario);

        // Act
        JwtResponse response = authService.authenticateUser(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals(1L, response.getIdUsuario());
        assertEquals("test@example.com", response.getCorreoElectronico());
        assertEquals("ADMIN_GLOBAL", response.getRol());
        assertEquals("Test User", response.getNombreCompleto());

        verify(usuarioRepository).findByCorreoElectronico("test@example.com");
        verify(passwordEncoder).matches("password123", "$2a$10$hashedPassword");
        verify(jwtUtil).generateToken(any());
        verify(usuarioRepository).save(testUsuario);
    }

    @Test
    void authenticateUser_UserNotFound_ThrowsException() {
        // Arrange
        when(usuarioRepository.findByCorreoElectronico("test@example.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            authService.authenticateUser(loginRequest));

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(usuarioRepository).findByCorreoElectronico("test@example.com");
        verifyNoInteractions(passwordEncoder, jwtUtil);
    }

    @Test
    void authenticateUser_InvalidPassword_ThrowsException() {
        // Arrange
        when(usuarioRepository.findByCorreoElectronico("test@example.com"))
                .thenReturn(Optional.of(testUsuario));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword"))
                .thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            authService.authenticateUser(loginRequest));

        assertEquals("Contraseña incorrecta", exception.getMessage());
        verify(usuarioRepository).findByCorreoElectronico("test@example.com");
        verify(passwordEncoder).matches("password123", "$2a$10$hashedPassword");
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void registerUser_ValidRequest_CreatesUser() {
        // Arrange
        when(usuarioRepository.findByCorreoElectronico("new@example.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("newPassword123"))
                .thenReturn("$2a$10$hashedNewPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setIdUsuario(2L);
            return usuario;
        });

        // Act
        assertDoesNotThrow(() -> authService.registerUser(registerRequest));

        // Assert
        verify(usuarioRepository).findByCorreoElectronico("new@example.com");
        verify(passwordEncoder).encode("newPassword123");
        verify(usuarioRepository).save(argThat(usuario ->
            usuario.getCorreoElectronico().equals("new@example.com") &&
            usuario.getNombreCompleto().equals("New User") &&
            usuario.getTipoDocumento().equals("CC") &&
            usuario.getNumeroDocumento().equals("12345678") &&
            usuario.getRol().equals(Usuario.Rol.SOLICITANTE) &&
            usuario.getContrasenaHash().equals("$2a$10$hashedNewPassword") &&
            usuario.getEstaActivo() &&
            usuario.getFechaCreacion() != null
        ));
    }

    @Test
    void registerUser_EmailAlreadyExists_ThrowsException() {
        // Arrange
        when(usuarioRepository.findByCorreoElectronico("new@example.com"))
                .thenReturn(Optional.of(testUsuario));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            authService.registerUser(registerRequest));

        assertEquals("El correo electrónico ya está en uso!", exception.getMessage());
        verify(usuarioRepository).findByCorreoElectronico("new@example.com");
        verifyNoInteractions(passwordEncoder);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void registerUser_SetsCorrectDefaults() {
        // Arrange
        when(usuarioRepository.findByCorreoElectronico(anyString()))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString()))
                .thenReturn("$2a$10$hashedPassword");

        // Act
        authService.registerUser(registerRequest);

        // Assert
        verify(usuarioRepository).save(argThat(usuario ->
            usuario.getEstaActivo() == true &&
            usuario.getFechaCreacion() != null &&
            usuario.getFechaCreacion().isBefore(LocalDateTime.now().plusSeconds(1))
        ));
    }
}