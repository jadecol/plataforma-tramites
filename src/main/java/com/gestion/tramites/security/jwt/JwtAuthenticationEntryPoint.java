package com.gestion.tramites.security.jwt; // O el paquete que hayas elegido

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        // Este método se invoca cuando un usuario no autenticado intenta acceder a un recurso protegido.
        // Aquí enviamos una respuesta de "401 Unauthorized".
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Acceso no autorizado: No se ha proporcionado un token o es inválido.");
    }
}