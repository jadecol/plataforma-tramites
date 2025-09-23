package com.gestion.tramites.interceptor;

import com.gestion.tramites.context.EntityContext;
import com.gestion.tramites.service.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.persistence.EntityManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class EntityInterceptor implements HandlerInterceptor {

    private final EntityManager entityManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception {

        // Skip interceptor for permitted paths
        String requestPath = request.getRequestURI();
        if (requestPath.startsWith("/api/v1/auth/") ||
            requestPath.startsWith("/api/public/") ||
            requestPath.startsWith("/api/test/") ||
            requestPath.equals("/actuator/health")) {
            log.debug("SKIPPING EntityInterceptor for permitted path: {}", requestPath);
            return true;
        }

        // Obtener el usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // SEGURIDAD CRÍTICA: Rechazar requests sin autenticación válida
        if (authentication == null || !authentication.isAuthenticated() ||
            "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("SECURITY: Request denied - No valid authentication for URI: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // Validar que el principal sea del tipo esperado
        if (!(authentication.getPrincipal() instanceof CustomUserDetails)) {
            log.error("SECURITY: Invalid principal type for authenticated user: {}",
                    authentication.getPrincipal().getClass().getSimpleName());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long entityId = userDetails.getIdEntidad();

        // SEGURIDAD CRÍTICA: EntityId es obligatorio para multi-tenancy
        if (entityId == null) {
            log.error("SECURITY: User {} has no entityId assigned - blocking request to {}",
                    userDetails.getUsername(), request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        try {
            // Establecer el ID de entidad en el contexto y activar el filtro de Hibernate
            EntityContext.setCurrentEntityId(entityId);
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("entityFilter").setParameter("entityId", entityId);

            log.debug("SECURITY: Multi-tenant filter activated for entity {} - user: {} - URI: {}",
                    entityId, userDetails.getUsername(), request.getRequestURI());

        } catch (Exception e) {
            log.error("SECURITY: Failed to activate multi-tenant filter for entity {} - user: {}",
                    entityId, userDetails.getUsername(), e);
            EntityContext.clear(); // Limpiar en caso de error
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return false;
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) throws Exception {

        try {
            // SEGURIDAD CRÍTICA: Deshabilitar filtro y limpiar contexto
            Long currentEntityId = EntityContext.getCurrentEntityId();
            if (currentEntityId != null) {
                Session session = entityManager.unwrap(Session.class);
                session.disableFilter("entityFilter");
                log.debug("SECURITY: Multi-tenant filter disabled for entity {}", currentEntityId);
            }
        } catch (Exception e) {
            log.error("SECURITY: Error disabling multi-tenant filter", e);
        } finally {
            // Siempre limpiar el contexto
            EntityContext.clear();
        }
    }
}
