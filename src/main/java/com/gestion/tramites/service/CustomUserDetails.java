package com.gestion.tramites.service;

import com.gestion.tramites.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private Long idUsuario;
    private String correoElectronico;
    private String contrasenaHash;
    private String rol;
    private Boolean estaActivo;
    private Long idEntidad; // NUEVO: Para multi-tenancy
    private String nombreEntidad; // NUEVO: Para mostrar en UI

    public CustomUserDetails(Usuario usuario) {
        this.idUsuario = usuario.getIdUsuario();
        this.correoElectronico = usuario.getCorreoElectronico();
        this.contrasenaHash = usuario.getContrasenaHash();
        this.rol = usuario.getRol().name();
        this.estaActivo = usuario.getEstaActivo();

        // Extraer información de la entidad
        if (usuario.getEntidad() != null) {
            this.idEntidad = usuario.getEntidad().getId();
            this.nombreEntidad = usuario.getEntidad().getNombre();
        }
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
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return estaActivo != null ? estaActivo : false;
    }

    // Getters adicionales para multi-tenancy
    public Long getIdUsuario() {
        return idUsuario;
    }

    public String getRol() {
        return rol;
    }

    public Long getIdEntidad() {
        return idEntidad;
    }

    public String getNombreEntidad() {
        return nombreEntidad;
    }

    // Métodos de utilidad para roles
    public boolean isAdminGlobal() {
        return "ADMIN_GLOBAL".equals(rol);
    }

    public boolean isAdminEntidad() {
        return "ADMIN_ENTIDAD".equals(rol);
    }

    public boolean isRevisor() {
        return "REVISOR".equals(rol);
    }

    public boolean isSolicitante() {
        return "SOLICITANTE".equals(rol);
    }

    public boolean tieneEntidad() {
        return idEntidad != null;
    }
}
