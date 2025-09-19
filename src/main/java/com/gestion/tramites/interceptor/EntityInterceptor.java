package com.gestion.tramites.interceptor;

import com.gestion.tramites.context.EntityContext;
import com.gestion.tramites.service.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.persistence.EntityManager;

@Component
@RequiredArgsConstructor
public class EntityInterceptor implements HandlerInterceptor {

    private final EntityManager entityManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception {

        // Obtener el usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof CustomUserDetails) {

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long entityId = userDetails.getIdEntidad();

            // Establecer el ID de entidad en el contexto y activar el filtro de Hibernate
            if (entityId != null) {
                EntityContext.setCurrentEntityId(entityId);
                Session session = entityManager.unwrap(Session.class);
                session.enableFilter("entityFilter").setParameter("entityId", entityId);
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) throws Exception {
        // Limpiar el contexto después de la petición
        EntityContext.clear();
    }
}
