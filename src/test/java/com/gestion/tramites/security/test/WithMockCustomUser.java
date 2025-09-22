package com.gestion.tramites.security.test;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {
    String username() default "testuser";
    String[] roles() default {"USER"};
    long entityId() default 1L; // ID de la entidad para el usuario mock
    long entidadId() default 1L; // Alias para entityId - compatibilidad
    long userId() default 1L; // ID del usuario mock
}
