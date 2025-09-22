package com.gestion.tramites.service;

import com.gestion.tramites.dto.auth.LoginRequest;
import com.gestion.tramites.dto.auth.JwtResponse;
import com.gestion.tramites.dto.auth.RegisterRequest;
import com.gestion.tramites.model.Usuario;
import com.gestion.tramites.repository.UsuarioRepository;
import com.gestion.tramites.security.jwt.JwtUtil; // NUEVO IMPORT
import org.springframework.security.authentication.AuthenticationManager; // NUEVO IMPORT
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // NUEVO
                                                                                        // IMPORT
import org.springframework.security.core.Authentication; // NUEVO IMPORT
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager; // NUEVO
    private final JwtUtil jwtUtil; // NUEVO

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager, // NUEVO
            JwtUtil jwtUtil) { // NUEVO
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager; // NUEVO
        this.jwtUtil = jwtUtil; // NUEVO
    }

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        // Buscar usuario directamente
        Usuario usuario =
                usuarioRepository.findByCorreoElectronico(loginRequest.getCorreoElectronico())
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar contraseña con BCrypt directo
        if (!passwordEncoder.matches(loginRequest.getContrasena(), usuario.getContrasenaHash())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        // Crear UserDetails para generar token
        CustomUserDetails userDetails = new CustomUserDetails(usuario);

        // Crear Authentication mock
        Authentication mockAuth = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());

        // Generar token JWT
        String token = jwtUtil.generateToken(mockAuth);

        // Actualizar última sesión
        usuario.setFechaUltimaSesion(LocalDateTime.now());
        usuarioRepository.save(usuario);

        return new JwtResponse(token, usuario.getIdUsuario(), usuario.getCorreoElectronico(),
                usuario.getRol().name(), usuario.getNombreCompleto());
    }

    public void registerUser(RegisterRequest registerRequest) {
        if (usuarioRepository.findByCorreoElectronico(registerRequest.getCorreoElectronico())
                .isPresent()) {
            throw new RuntimeException("El correo electrónico ya está en uso!");
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setCorreoElectronico(registerRequest.getCorreoElectronico());
        nuevoUsuario.setNombreCompleto(registerRequest.getNombreCompleto());
        nuevoUsuario.setTipoDocumento(registerRequest.getTipoDocumento());
        nuevoUsuario.setNumeroDocumento(registerRequest.getNumeroDocumento());
        nuevoUsuario.setRol(Usuario.Rol.valueOf(registerRequest.getRol()));
        nuevoUsuario.setFechaCreacion(LocalDateTime.now());
        nuevoUsuario.setEstaActivo(true);

        String encodedPassword = passwordEncoder.encode(registerRequest.getContrasena());
        nuevoUsuario.setContrasenaHash(encodedPassword);

        usuarioRepository.save(nuevoUsuario);
    }
}
