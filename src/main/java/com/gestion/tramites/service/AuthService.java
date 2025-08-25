package com.gestion.tramites.service;

import com.gestion.tramites.dto.auth.LoginRequest;
import com.gestion.tramites.dto.auth.JwtResponse; // Importa la clase corregida
import com.gestion.tramites.dto.auth.RegisterRequest;
import com.gestion.tramites.model.Usuario;
import com.gestion.tramites.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Usuario usuario = usuarioRepository.findByCorreoElectronico(loginRequest.getCorreoElectronico())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (!passwordEncoder.matches(loginRequest.getContrasena(), usuario.getContrasenaHash())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        String token = "token_generado_ejemplo";
        
        // ¡Esta llamada ahora es válida porque @AllArgsConstructor crea este constructor!
        return new JwtResponse(
            token, 
            usuario.getIdUsuario(), 
            usuario.getCorreoElectronico(), 
            usuario.getRol(), 
            usuario.getNombreCompleto()
        );
    }

    public void registerUser(RegisterRequest registerRequest) {
        if (usuarioRepository.findByCorreoElectronico(registerRequest.getCorreoElectronico()).isPresent()) {
            throw new RuntimeException("El correo electrónico ya está en uso!");
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setCorreoElectronico(registerRequest.getCorreoElectronico());
        nuevoUsuario.setNombreCompleto(registerRequest.getNombreCompleto()); // **¡Corregido! Ahora toma el nombre de la petición**
        nuevoUsuario.setTipoDocumento(registerRequest.getTipoDocumento()); // **¡Nuevo! Asigna el tipo de documento**
        nuevoUsuario.setNumeroDocumento(registerRequest.getNumeroDocumento()); // **¡Nuevo! Asigna el número de documento**
        nuevoUsuario.setRol(registerRequest.getRol());
        nuevoUsuario.setFechaCreacion(LocalDateTime.now());
        nuevoUsuario.setEstaActivo(true);
        
        String encodedPassword = passwordEncoder.encode(registerRequest.getContrasena());
        nuevoUsuario.setContrasenaHash(encodedPassword);

        usuarioRepository.save(nuevoUsuario);
    }
}
