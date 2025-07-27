package com.gestion.tramites.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders; // Importar Decoders para decodificar la clave Base64
import io.jsonwebtoken.security.Keys; // Importar Keys para generar claves seguras (si usas la generación en caliente)
import io.jsonwebtoken.security.SignatureException; // Importar SignatureException

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication; // <-- IMPORTANTE: Añadir este import para el nuevo generateToken
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration; // En milisegundos

    // 1. Obtiene la clave de firma (Key) a partir de la cadena secreta
    private Key getSigningKey() {
        // Tu secreto ya está en Base64, así que lo decodificamos directamente.
        // Asegúrate de que el secreto en application.properties sea una cadena BASE64 válida y lo suficientemente larga.
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // NUEVO MÉTODO: 2. Genera token JWT a partir de un objeto Authentication
    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        String username = userPrincipal.getUsername(); // Esto debería ser el correo electrónico

        // Puedes añadir claims adicionales si los necesitas
        Map<String, Object> claims = new HashMap<>();

        // Si tu UserDetails implementa una interfaz personalizada o tiene métodos para obtener el ID y el Rol,
        // puedes agregarlos aquí. Asegúrate de que tu CustomUserDetailsService devuelva esos datos.
        // Por ejemplo, si tienes una clase CustomUserDetails con getId() y getRol():
        // if (userPrincipal instanceof CustomUserDetails) {
        //     CustomUserDetails customUser = (CustomUserDetails) userPrincipal;
        //     claims.put("id", customUser.getId());
        //     claims.put("rol", customUser.getRol());
        // } else {
        //     // Si no usas CustomUserDetails, puedes intentar obtener el rol de las autoridades si lo necesitas en el token
        //     if (!userPrincipal.getAuthorities().isEmpty()) {
        //         claims.put("rol", userPrincipal.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""));
        //     }
        // }


        return Jwts.builder()
                .setClaims(claims) // Añade los claims
                .setSubject(username) // El "sujeto" del token es el nombre de usuario (correo)
                .setIssuedAt(new Date(System.currentTimeMillis())) // Fecha de emisión
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // Fecha de expiración
                .signWith(getSigningKey(), SignatureAlgorithm.HS512) // Firma el token con la clave y algoritmo
                .compact(); // Construye el token JWT
    }


    // MÉTODO ORIGINAL: 3. Valida el token contra un UserDetails
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // 4. Extrae el nombre de usuario del token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 5. Extrae la fecha de expiración del token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // 6. Extrae un claim específico del token usando una función resolutora
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 7. Extrae todos los claims del token
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 8. Verifica si el token ha expirado
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // NUEVO MÉTODO: 9. Valida el token y captura las excepciones de JWT (útil para el filtro)
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Token JWT inválido: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Token JWT ha expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Token JWT no soportado: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Cadena JWT vacía: {}", e.getMessage());
        } catch (SignatureException e) { // <-- Importante: io.jsonwebtoken.security.SignatureException
            logger.error("Firma JWT inválida: {}", e.getMessage());
        }
        return false;
    }
}
