package com.gestion.tramites.service;

import com.gestion.tramites.dto.notificaciones.NotificacionEmailDTO;
import com.gestion.tramites.model.Tramite;
import com.gestion.tramites.model.Usuario;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class NotificacionService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionService.class);

    // Enum para tipos de notificación
    public enum TipoNotificacion {
        TRAMITE_CREADO,
        CAMBIO_ESTADO,
        ASIGNACION_REVISOR,
        DOCUMENTOS_PENDIENTES,
        RADICACION_EXITOSA
    }

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${notifications.email.enabled:true}")
    private boolean notificacionesHabilitadas;

    @Value("${notifications.email.from}")
    private String emailFrom;

    @Value("${notifications.email.from-name}")
    private String emailFromName;

    @Value("${notifications.email.base-url}")
    private String baseUrl;

    private static final DateTimeFormatter FECHA_FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA_FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Async
    public void enviarNotificacionTramiteCreado(Tramite tramite) {
        if (!notificacionesHabilitadas) {
            logger.debug("Notificaciones deshabilitadas, no se envía email para trámite creado");
            return;
        }

        try {
            Map<String, Object> variables = crearVariablesComunes(tramite);
            variables.put("accion", "creado");

            NotificacionEmailDTO notificacion = new NotificacionEmailDTO(
                tramite.getSolicitante().getCorreoElectronico(),
                tramite.getSolicitante().getNombreCompleto(),
                String.format("Trámite %s - Radicado exitosamente", tramite.getNumeroRadicacion()),
                "tramite-creado",
                variables
            );

            enviarEmail(notificacion);
            logger.info("Notificación de trámite creado enviada a: {}", tramite.getSolicitante().getCorreoElectronico());

        } catch (Exception e) {
            logger.error("Error enviando notificación de trámite creado para: {}", tramite.getNumeroRadicacion(), e);
        }
    }

    @Async
    public void enviarNotificacionCambioEstado(Tramite tramite, Tramite.EstadoTramite estadoAnterior, String comentarios) {
        if (!notificacionesHabilitadas) {
            logger.debug("Notificaciones deshabilitadas, no se envía email para cambio de estado");
            return;
        }

        try {
            // Notificar al solicitante
            enviarNotificacionCambioEstadoASolicitante(tramite, estadoAnterior, comentarios);

            // Si hay revisor asignado y es diferente del solicitante, también notificar
            if (tramite.getRevisorAsignado() != null &&
                !tramite.getRevisorAsignado().getId().equals(tramite.getSolicitante().getId())) {
                enviarNotificacionCambioEstadoARevisor(tramite, estadoAnterior, comentarios);
            }

        } catch (Exception e) {
            logger.error("Error enviando notificación de cambio de estado para: {}", tramite.getNumeroRadicacion(), e);
        }
    }

    @Async
    public void enviarNotificacionAsignacionRevisor(Tramite tramite, Usuario revisor) {
        if (!notificacionesHabilitadas) {
            logger.debug("Notificaciones deshabilitadas, no se envía email para asignación de revisor");
            return;
        }

        try {
            Map<String, Object> variables = crearVariablesComunes(tramite);
            variables.put("nombreRevisor", revisor.getNombreCompleto());

            NotificacionEmailDTO notificacion = new NotificacionEmailDTO(
                revisor.getCorreoElectronico(),
                revisor.getNombreCompleto(),
                String.format("Trámite %s - Asignado para revisión", tramite.getNumeroRadicacion()),
                "revisor-asignado",
                variables
            );

            enviarEmail(notificacion);
            logger.info("Notificación de asignación enviada al revisor: {}", revisor.getCorreoElectronico());

        } catch (Exception e) {
            logger.error("Error enviando notificación de asignación para: {}", tramite.getNumeroRadicacion(), e);
        }
    }

    @Async
    public void enviarNotificacionDocumentosPendientes(Tramite tramite, String documentosFaltantes) {
        if (!notificacionesHabilitadas) {
            logger.debug("Notificaciones deshabilitadas, no se envía email para documentos pendientes");
            return;
        }

        try {
            Map<String, Object> variables = crearVariablesComunes(tramite);
            variables.put("documentosFaltantes", documentosFaltantes);

            NotificacionEmailDTO notificacion = new NotificacionEmailDTO(
                tramite.getSolicitante().getCorreoElectronico(),
                tramite.getSolicitante().getNombreCompleto(),
                String.format("Trámite %s - Documentos pendientes", tramite.getNumeroRadicacion()),
                "documentos-pendientes",
                variables
            );

            enviarEmail(notificacion);
            logger.info("Notificación de documentos pendientes enviada a: {}", tramite.getSolicitante().getCorreoElectronico());

        } catch (Exception e) {
            logger.error("Error enviando notificación de documentos pendientes para: {}", tramite.getNumeroRadicacion(), e);
        }
    }

    private void enviarNotificacionCambioEstadoASolicitante(Tramite tramite, Tramite.EstadoTramite estadoAnterior, String comentarios) {
        Map<String, Object> variables = crearVariablesComunes(tramite);
        variables.put("estadoAnterior", estadoAnterior.getDescripcion());
        variables.put("comentarios", comentarios);
        variables.put("esEstadoFinal", tramite.estaFinalizado());

        String template = determinarTemplatePorEstado(tramite.getEstadoActual());
        String asunto = generarAsuntoPorEstado(tramite);

        NotificacionEmailDTO notificacion = new NotificacionEmailDTO(
            tramite.getSolicitante().getCorreoElectronico(),
            tramite.getSolicitante().getNombreCompleto(),
            asunto,
            template,
            variables
        );

        enviarEmail(notificacion);
        logger.info("Notificación de cambio de estado enviada al solicitante: {}", tramite.getSolicitante().getCorreoElectronico());
    }

    private void enviarNotificacionCambioEstadoARevisor(Tramite tramite, Tramite.EstadoTramite estadoAnterior, String comentarios) {
        Map<String, Object> variables = crearVariablesComunes(tramite);
        variables.put("estadoAnterior", estadoAnterior.getDescripcion());
        variables.put("comentarios", comentarios);
        variables.put("nombreRevisor", tramite.getRevisorAsignado().getNombreCompleto());

        NotificacionEmailDTO notificacion = new NotificacionEmailDTO(
            tramite.getRevisorAsignado().getCorreoElectronico(),
            tramite.getRevisorAsignado().getNombreCompleto(),
            String.format("Trámite %s - Actualización de estado", tramite.getNumeroRadicacion()),
            "cambio-estado-revisor",
            variables
        );

        enviarEmail(notificacion);
        logger.info("Notificación de cambio de estado enviada al revisor: {}", tramite.getRevisorAsignado().getCorreoElectronico());
    }

    private Map<String, Object> crearVariablesComunes(Tramite tramite) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("numeroRadicacion", tramite.getNumeroRadicacion());
        variables.put("objetoTramite", tramite.getObjetoTramite());
        variables.put("tipoTramite", tramite.getTipoTramite() != null ? tramite.getTipoTramite().getNombre() : "N/A");
        variables.put("estadoActual", tramite.getEstadoActual().getDescripcion());
        variables.put("fechaRadicacion", tramite.getFechaRadicacion() != null ?
                      tramite.getFechaRadicacion().format(FECHA_FORMATO) : "N/A");
        variables.put("nombreSolicitante", tramite.getSolicitante().getNombreCompleto());
        variables.put("nombreEntidad", tramite.getEntidad().getNombre());
        variables.put("urlConsulta", baseUrl + "/consulta/" + tramite.getNumeroRadicacion());
        variables.put("fechaActual", java.time.LocalDate.now().format(FECHA_FORMATO));

        if (tramite.getFechaUltimoCambioEstado() != null) {
            variables.put("fechaUltimoCambio", tramite.getFechaUltimoCambioEstado().format(FECHA_HORA_FORMATO));
        }

        return variables;
    }

    private String determinarTemplatePorEstado(Tramite.EstadoTramite estado) {
        return switch (estado) {
            case APROBADO -> "tramite-aprobado";
            case RECHAZADO -> "tramite-rechazado";
            case PENDIENTE_DOCUMENTOS -> "documentos-pendientes";
            case EN_REVISION -> "tramite-en-revision";
            case ASIGNADO -> "tramite-asignado";
            default -> "cambio-estado-general";
        };
    }

    private String generarAsuntoPorEstado(Tramite tramite) {
        String numeroRadicacion = tramite.getNumeroRadicacion();
        return switch (tramite.getEstadoActual()) {
            case APROBADO -> String.format("¡Excelente noticia! Trámite %s APROBADO", numeroRadicacion);
            case RECHAZADO -> String.format("Trámite %s - Resolución final", numeroRadicacion);
            case PENDIENTE_DOCUMENTOS -> String.format("Trámite %s - Documentos requeridos", numeroRadicacion);
            case EN_REVISION -> String.format("Trámite %s - En proceso de revisión", numeroRadicacion);
            case ASIGNADO -> String.format("Trámite %s - Asignado para evaluación", numeroRadicacion);
            default -> String.format("Trámite %s - Actualización de estado", numeroRadicacion);
        };
    }

    private void enviarEmail(NotificacionEmailDTO notificacion) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            try {
                helper.setFrom(emailFrom, emailFromName);
            } catch (java.io.UnsupportedEncodingException e) {
                helper.setFrom(emailFrom);
            }
            helper.setTo(notificacion.getDestinatario());
            helper.setSubject(notificacion.getAsunto());

            Context context = new Context();
            context.setVariables(notificacion.getVariables());
            String contenidoHtml = templateEngine.process("emails/" + notificacion.getTemplate(), context);

            helper.setText(contenidoHtml, true);

            mailSender.send(message);
            logger.debug("Email enviado exitosamente a: {}", notificacion.getDestinatario());

        } catch (MessagingException e) {
            logger.error("Error enviando email a: {}", notificacion.getDestinatario(), e);
            throw new RuntimeException("Error enviando email", e);
        }
    }

    // Método requerido por RadicacionService
    @Async
    public void enviarNotificacionEmail(String destinatario, String asunto, String mensaje, TipoNotificacion tipo) {
        if (!notificacionesHabilitadas) {
            logger.debug("Notificaciones deshabilitadas, no se envía email");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            try {
                helper.setFrom(emailFrom, emailFromName);
            } catch (java.io.UnsupportedEncodingException e) {
                helper.setFrom(emailFrom);
            }
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(mensaje, false); // Como texto plano

            mailSender.send(message);
            logger.info("Notificación {} enviada a: {}", tipo, destinatario);

        } catch (Exception e) {
            logger.error("Error enviando notificación {} a: {}", tipo, destinatario, e);
        }
    }
}