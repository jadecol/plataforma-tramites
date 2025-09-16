package com.gestion.tramites.auth;

import com.gestion.tramites.security.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Esta es la clase de configuración de seguridad de la aplicación.
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Habilita la seguridad basada en métodos con @PreAuthorize
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Constructor para inyectar las dependencias necesarias.
    public SecurityConfig(CustomUserDetailsService customUserDetailsService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // Bean para el codificador de contraseñas.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Bean para el proveedor de autenticación.
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // Bean para el administrador de autenticación.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Bean principal que configura la cadena de filtros de seguridad.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilita CSRF, ya que las APIs REST no lo necesitan.
            .csrf(csrf -> csrf.disable())
            // Configura la política de creación de sesiones como sin estado para JWT.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Configura las reglas de autorización de las peticiones HTTP.
            .authorizeHttpRequests(authorize -> authorize
                // Permite el acceso público a todas las rutas de autenticación.
                .requestMatchers("/api/v1/auth/**").permitAll()
                // Cualquier otra petición debe estar autenticada.
                .anyRequest().authenticated()
            )
            // Agrega el proveedor de autenticación personalizado.
            .authenticationProvider(authenticationProvider())
            // Agrega el filtro de autenticación JWT antes del filtro de autenticación de nombre de usuario/contraseña de Spring.
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

