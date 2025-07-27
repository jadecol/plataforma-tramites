package com.gestion.tramites.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component; // Importa esta anotación

@Component // ¡Añade esta anotación! Esto lo convierte en un bean de Spring.
public class PasswordGenerator {

    // Instancia de BCryptPasswordEncoder para ser reutilizada
    private final BCryptPasswordEncoder passwordEncoder;

    // Constructor para inicializar BCryptPasswordEncoder
    public PasswordGenerator() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // Método para hashear una contraseña
    public String encode(CharSequence rawPassword) { // Usa CharSequence para compatibilidad con PasswordEncoder
        return passwordEncoder.encode(rawPassword);
    }

    // Opcional: Método para verificar si una contraseña coincide con un hash
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    // Remueve el método main, ya no es necesario aquí.
    // public static void main(String[] args) { ... }
}
