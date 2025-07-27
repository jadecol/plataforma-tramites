//package com.gestion.tramites.service;
//
//import com.gestion.tramites.dto.auth.LoginRequest;
//import com.gestion.tramites.dto.auth.JwtResponse;
//import com.gestion.tramites.model.Usuario; // Asegúrate de que esta ruta sea correcta para tu entidad Usuario
//import com.gestion.tramites.repository.UsuarioRepository; // Asegúrate de que esta ruta sea correcta para tu repositorio
//import com.gestion.tramites.security.jwt.JwtUtil; // Asegúrate de que esta ruta sea correcta para tu JwtUtil
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Service;
//
//@Service
//public class AuthService {
//
//    private final AuthenticationManager authenticationManager;
//    private final JwtUtil jwtUtil;
//    private final UsuarioRepository usuarioRepository; // Inyectamos el repositorio para obtener datos adicionales del usuario
//
//    // Constructor para inyección de dependencias
//    public AuthService(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UsuarioRepository usuarioRepository) {
//        this.authenticationManager = authenticationManager;
//        this.jwtUtil = jwtUtil;
//        this.usuarioRepository = usuarioRepository;
//    }
//
//    /**
//     * Autentica al usuario usando las credenciales proporcionadas y genera un token JWT.
//     * @param loginRequest Objeto que contiene el correo electrónico y la contraseña del usuario.
//     * @return Un objeto JwtResponse que contiene el token JWT y la información básica del usuario.
//     * @throws org.springframework.security.core.AuthenticationException si la autenticación falla.
//     */
//    public JwtResponse authenticateUser(LoginRequest loginRequest) {
//        // 1. Autenticar las credenciales del usuario
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(loginRequest.getCorreoElectronico(), loginRequest.getContrasena())
//        );
//
//        // 2. Establecer la autenticación en el contexto de seguridad de Spring
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        // 3. Generar el token JWT
//        String jwt = jwtUtil.generateToken(authentication);
//
//        // 4. Obtener los detalles del usuario autenticado (desde el UserDetailsService)
//        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//
//        // 5. Cargar la entidad Usuario completa para obtener el ID, nombre y rol
//        // Esto es necesario porque UserDetails por defecto solo tiene username, password y authorities.
//        Usuario usuario = usuarioRepository.findByCorreoElectronico(userDetails.getUsername())
//                                            .orElseThrow(() -> new RuntimeException("Usuario no encontrado después de autenticación."));
//
//        // 6. Construir y devolver la respuesta JWT
//        return new JwtResponse(
//            jwt,
//            usuario.getIdUsuario(),        // Suponiendo que tu entidad Usuario tiene un método getIdUsuario()
//            usuario.getCorreoElectronico(),
//            usuario.getRol(),              // Suponiendo que tu entidad Usuario tiene un método getRol() que devuelve el String del rol
//            usuario.getNombreCompleto()    // Suponiendo que tu entidad Usuario tiene un método getNombreCompleto()
//        );
//    }
//}
// src/main/java/com/gestion/tramites/service/AuthService.java
package com.gestion.tramites.service;

import com.gestion.tramites.dto.auth.LoginRequest;
import com.gestion.tramites.dto.auth.JwtResponse;
import com.gestion.tramites.model.Usuario; // Asegúrate de esta ruta
import com.gestion.tramites.repository.UsuarioRepository; // Asegúrate de esta ruta
import com.gestion.tramites.security.jwt.JwtUtil; // Asegúrate de esta ruta
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;

    public AuthService(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UsuarioRepository usuarioRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
    }

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getCorreoElectronico(), loginRequest.getContrasena())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtil.generateToken(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Cargar la entidad Usuario completa para obtener el ID, nombre y rol
        Usuario usuario = usuarioRepository.findByCorreoElectronico(userDetails.getUsername())
                                            .orElseThrow(() -> new RuntimeException("Usuario no encontrado después de autenticación."));

        return new JwtResponse(
            jwt,
            usuario.getIdUsuario(),
            usuario.getCorreoElectronico(),
            usuario.getRol(),
            usuario.getNombreCompleto()
        );
    }
}
