package com.gestion.tramites.service;

import com.gestion.tramites.model.Usuario;
import com.gestion.tramites.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
// Ya no necesitas GrantedAuthority ni SimpleGrantedAuthority aquí, ya que CustomUserDetails lo maneja
// import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
// import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String correoElectronico) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreoElectronico(correoElectronico)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con correo: " + correoElectronico));

        // --- ¡CAMBIO CRÍTICO AQUÍ! Retornar tu CustomUserDetails
        return new CustomUserDetails(usuario);
    }
}
