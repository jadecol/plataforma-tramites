package com.gestion.tramites.controller;

import com.gestion.tramites.dto.auth.LoginRequest;
import com.gestion.tramites.dto.auth.JwtResponse;
import com.gestion.tramites.service.AuthService; // Importa el nuevo servicio
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth") // Asegúrate que esta sea la ruta correcta que ya usas
public class AuthController {

    private final AuthService authService; // Inyecta el servicio

    // Constructor para inyección de dependencias
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // Delega la lógica de autenticación y generación de JWT al AuthService
        JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    // Si tienes otros endpoints en este controlador (ej. para registro), mantenlos o refactorízalos si es necesario.
    // Ejemplo de registro (si lo tienes aquí):
    // @PostMapping("/register")
    // public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
    //     // Lógica de registro...
    // }
}
