package com.gestion.tramites.dto.notificaciones;

import java.util.Map;

public class NotificacionEmailDTO {

    private String destinatario;
    private String nombreDestinatario;
    private String asunto;
    private String template;
    private Map<String, Object> variables;

    public NotificacionEmailDTO() {}

    public NotificacionEmailDTO(String destinatario, String nombreDestinatario, String asunto,
                               String template, Map<String, Object> variables) {
        this.destinatario = destinatario;
        this.nombreDestinatario = nombreDestinatario;
        this.asunto = asunto;
        this.template = template;
        this.variables = variables;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getNombreDestinatario() {
        return nombreDestinatario;
    }

    public void setNombreDestinatario(String nombreDestinatario) {
        this.nombreDestinatario = nombreDestinatario;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
}