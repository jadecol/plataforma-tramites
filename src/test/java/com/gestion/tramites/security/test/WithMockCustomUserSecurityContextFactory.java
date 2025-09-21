package com.gestion.tramites.security.test;

import com.gestion.tramites.model.Entidad;
import com.gestion.tramites.model.Usuario;
import com.gestion.tramites.service.CustomUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockCustomUserSecurityContextFactory
        implements WithSecurityContextFactory<WithMockCustomUser> {
    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Usuario mockUsuario = new Usuario();
        mockUsuario.setIdUsuario(customUser.userId());
        mockUsuario.setUsername(customUser.username());
        mockUsuario.setRol(customUser.roles()[0]); // Asumiendo un solo rol para simplicidad

        Entidad mockEntidad = new Entidad();
        mockEntidad.setId(customUser.entityId());
        mockEntidad.setNombre("Entidad " + customUser.entityId()); // Nombre de entidad para
                                                                   // referencia
        mockUsuario.setEntidad(mockEntidad);

        CustomUserDetails principal = new CustomUserDetails(mockUsuario);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal,
                "password", principal.getAuthorities());
        context.setAuthentication(authentication);
        return context;
    }
}
