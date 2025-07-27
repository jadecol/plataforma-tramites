package com.gestion.tramites.service;

import com.gestion.tramites.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails /*, Serializable */ {
    private static final long serialVersionUID = 1L; // <-- Añade esta línea

    private Long idUsuario; // <--- ESTE CAMPO ES CLAVE
    private String correoElectronico;
    private String contrasenaHash;
    private String rol;
    private Boolean estaActivo;

    public CustomUserDetails(Usuario usuario) {
        this.idUsuario = usuario.getIdUsuario(); // <--- ASIGNACIÓN EN EL CONSTRUCTOR
        this.correoElectronico = usuario.getCorreoElectronico();
        this.contrasenaHash = usuario.getContrasenaHash();
        this.rol = usuario.getRol();
        this.estaActivo = usuario.getEstaActivo();
    }

    public Long getIdUsuario() { // <--- ESTE MÉTODO ES CLAVE PARA RECUPERAR EL ID
        return idUsuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    @Override
    public String getPassword() {
        return contrasenaHash;
    }

    @Override
    public String getUsername() {
        return correoElectronico;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() {
        return estaActivo != null ? estaActivo : false;
    }
}
