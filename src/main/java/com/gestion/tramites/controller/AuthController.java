package com.gestion.tramites.controller;

import com.gestion.tramites.dto.auth.LoginRequest;
import com.gestion.tramites.dto.auth.JwtResponse;
import com.gestion.tramites.dto.auth.RegisterRequest;
import com.gestion.tramites.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Endpoints para autenticación y registro de usuarios")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Autenticar usuario",
            description = "Autentica un usuario con correo electrónico y contraseña, retornando un token JWT válido"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticación exitosa",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = JwtResponse.class),
                            examples = @ExampleObject(
                                    name = "Respuesta exitosa",
                                    value = """
                                            {
                                              "token": "eyJhbGciOiJIUzUxMiJ9...",
                                              "idUsuario": 1,
                                              "correoElectronico": "admin@alcaldia.gov.co",
                                              "rol": "ADMIN",
                                              "nombreCompleto": "Administrador Sistema"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error validación",
                                    value = """
                                            {
                                              "timestamp": "2024-01-01T10:00:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Usuario no encontrado"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciales inválidas",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error autenticación",
                                    value = """
                                            {
                                              "timestamp": "2024-01-01T10:00:00",
                                              "status": 401,
                                              "error": "Unauthorized",
                                              "message": "Contraseña incorrecta"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credenciales de usuario para autenticación",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo login",
                                    value = """
                                            {
                                              "correoElectronico": "admin@alcaldia.gov.co",
                                              "contrasena": "Admin123*"
                                            }
                                            """
                            )
                    )
            )
            LoginRequest loginRequest) {
        JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @Operation(
            summary = "Registrar nuevo usuario",
            description = "Registra un nuevo usuario en el sistema con los datos proporcionados"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario registrado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Registro exitoso",
                                    value = "\"User registered successfully\""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos o usuario ya existe",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error registro",
                                    value = """
                                            {
                                              "timestamp": "2024-01-01T10:00:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "El correo electrónico ya está en uso!"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Error de validación de campos",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error validación campos",
                                    value = """
                                            {
                                              "timestamp": "2024-01-01T10:00:00",
                                              "status": 422,
                                              "error": "Validation Failed",
                                              "message": "correoElectronico: must be a well-formed email address"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del nuevo usuario a registrar",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterRequest.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo registro",
                                    value = """
                                            {
                                              "correoElectronico": "usuario@alcaldia.gov.co",
                                              "contrasena": "MiPassword123*",
                                              "nombreCompleto": "Juan Pérez García",
                                              "tipoDocumento": "CC",
                                              "numeroDocumento": "12345678",
                                              "rol": "USER"
                                            }
                                            """
                            )
                    )
            )
            RegisterRequest registerRequest) {
        authService.registerUser(registerRequest);
        return new ResponseEntity<>("User registered successfully", HttpStatus.CREATED);
    }
}
