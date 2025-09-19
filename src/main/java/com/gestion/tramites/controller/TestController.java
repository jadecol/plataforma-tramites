package com.gestion.tramites.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.gestion.tramites.service.CustomUserDetailsService;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @GetMapping("/hash/{password}")
    public String generateHash(@PathVariable String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(password);
    }

    @GetMapping("/verify/{password}/{hash}")
    public String verifyHash(@PathVariable String password, @PathVariable String hash) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Debug: mostrar lo que estamos comparando
        System.out.println("Password: " + password);
        System.out.println("Hash recibido: " + hash);
        System.out.println("Hash procesado: " + hash.replace("SLASH", "/"));

        String processedHash = hash.replace("SLASH", "/");
        boolean matches = encoder.matches(password, processedHash);

        return "Password: " + password + "\nHash: " + processedHash + "\nMatches: " + matches;
    }

    @GetMapping("/debug-user/{email}")
    public String debugUser(@PathVariable String email) {
        try {
            UserDetails user = userDetailsService.loadUserByUsername(email);
            return "Usuario encontrado: " + user.getUsername() + "\nEnabled: " + user.isEnabled()
                    + "\nAccountNonExpired: " + user.isAccountNonExpired() + "\nAccountNonLocked: "
                    + user.isAccountNonLocked() + "\nCredentialsNonExpired: "
                    + user.isCredentialsNonExpired() + "\nAuthorities: " + user.getAuthorities()
                    + "\nPassword Hash: " + user.getPassword();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
