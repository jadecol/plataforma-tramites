package com.gestion.tramites.context;

import org.springframework.stereotype.Component;

@Component("entityContext")
public class EntityContext {

    private static final ThreadLocal<Long> currentEntityId = new ThreadLocal<>();

    public static void setCurrentEntityId(Long entityId) {
        currentEntityId.set(entityId);
    }

    public static Long getCurrentEntityId() {
        return currentEntityId.get();
    }

    public static void clear() {
        currentEntityId.remove();
    }

    // Para uso en @Query de repositorios (diferente firma)
    public Long getCurrentEntityIdForQuery() {
        return EntityContext.getCurrentEntityId();
    }
}
