package com.gestion.tramites.security.filter;

import com.gestion.tramites.security.jwt.JwtUtil;
import com.gestion.tramites.service.CustomUserDetailsService; // Tu CustomUserDetailsService
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter; // Importante para que se ejecute una vez por solicitud

import java.io.IOException;

// Asegúrate de importar LoggerFactory y Logger para los logs
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Instancia del logger para esta clase
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip JWT processing for permitted paths
        String requestPath = request.getRequestURI();
        if (requestPath.startsWith("/api/v1/auth/") ||
            requestPath.startsWith("/api/public/") ||
            requestPath.startsWith("/api/test/") ||
            requestPath.equals("/actuator/health")) {
            logger.info("SKIPPING JWT processing for permitted path: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        logger.info("PROCESSING JWT for path: {}", requestPath);

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                // Log de advertencia si el token no puede ser parseado o es inválido/expirado
                logger.warn("JWT Token inválido o expirado para la ruta {}: {}", request.getRequestURI(), e.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                // **NUEVA LÍNEA DE LOG:** Confirma que el usuario fue autenticado y puesto en el SecurityContextHolder
                logger.info("Usuario '{}' autenticado y establecido en SecurityContextHolder para la ruta: {}. Roles: {}",
                            userDetails.getUsername(), request.getRequestURI(), userDetails.getAuthorities());
            } else {
                // Este else es importante para capturar si validateToken devuelve false
                logger.warn("Validación de JWT fallida para usuario: {}. Token: {}", username, jwt);
            }
        }

        filterChain.doFilter(request, response);
    }
}
