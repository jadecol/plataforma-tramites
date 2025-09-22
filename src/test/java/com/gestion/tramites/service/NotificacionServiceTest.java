package com.gestion.tramites.service;

import com.gestion.tramites.model.*;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private NotificacionService notificacionService;

    private Tramite tramitePrueba;
    private Usuario solicitante;
    private Usuario revisor;
    private Entidad entidad;
    private TipoTramite tipoTramite;

    @BeforeEach
    void setUp() {
        // Configurar propiedades del servicio
        ReflectionTestUtils.setField(notificacionService, "notificacionesHabilitadas", true);
        ReflectionTestUtils.setField(notificacionService, "emailFrom", "noreply@test.com");
        ReflectionTestUtils.setField(notificacionService, "emailFromName", "Test Platform");
        ReflectionTestUtils.setField(notificacionService, "baseUrl", "http://localhost:8080");

        // Crear objetos de prueba
        crearDatosPrueba();

        // Configurar mocks
        when(templateEngine.process(any(String.class), any(Context.class)))
                .thenReturn("<html><body>Test Email</body></html>");

        MimeMessage mockMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mockMessage);
    }

    @Test
    void enviarNotificacionTramiteCreado_ConNotificacionesHabilitadas_DebeEnviarEmail() {
        // Act
        notificacionService.enviarNotificacionTramiteCreado(tramitePrueba);

        // Assert
        verify(templateEngine, timeout(1000)).process(eq("emails/tramite-creado"), any(Context.class));
        verify(mailSender, timeout(1000)).send(any(MimeMessage.class));
    }

    @Test
    void enviarNotificacionTramiteCreado_ConNotificacionesDeshabilitadas_NoDebeEnviarEmail() {
        // Arrange
        ReflectionTestUtils.setField(notificacionService, "notificacionesHabilitadas", false);

        // Act
        notificacionService.enviarNotificacionTramiteCreado(tramitePrueba);

        // Assert
        verify(templateEngine, never()).process(any(String.class), any(Context.class));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void enviarNotificacionCambioEstado_EstadoAprobado_DebeUsarTemplateCorrect() {
        // Arrange
        tramitePrueba.setEstadoActual(Tramite.EstadoTramite.APROBADO);
        Tramite.EstadoTramite estadoAnterior = Tramite.EstadoTramite.EN_REVISION;
        String comentarios = "Trámite aprobado satisfactoriamente";

        // Act
        notificacionService.enviarNotificacionCambioEstado(tramitePrueba, estadoAnterior, comentarios);

        // Assert
        verify(templateEngine, timeout(1000)).process(eq("emails/tramite-aprobado"), any(Context.class));
        verify(mailSender, timeout(1000)).send(any(MimeMessage.class));
    }

    @Test
    void enviarNotificacionCambioEstado_EstadoRechazado_DebeUsarTemplateCorrect() {
        // Arrange
        tramitePrueba.setEstadoActual(Tramite.EstadoTramite.RECHAZADO);
        Tramite.EstadoTramite estadoAnterior = Tramite.EstadoTramite.EN_REVISION;
        String comentarios = "No cumple con los requisitos técnicos";

        // Act
        notificacionService.enviarNotificacionCambioEstado(tramitePrueba, estadoAnterior, comentarios);

        // Assert
        verify(templateEngine, timeout(1000)).process(eq("emails/tramite-rechazado"), any(Context.class));
        verify(mailSender, timeout(1000)).send(any(MimeMessage.class));
    }

    @Test
    void enviarNotificacionAsignacionRevisor_DebeEnviarEmailAlRevisor() {
        // Act
        notificacionService.enviarNotificacionAsignacionRevisor(tramitePrueba, revisor);

        // Assert
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine, timeout(1000)).process(eq("emails/revisor-asignado"), contextCaptor.capture());
        verify(mailSender, timeout(1000)).send(any(MimeMessage.class));

        // Verificar que las variables del contexto son correctas
        Context context = contextCaptor.getValue();
        assertEquals("TEST-001", context.getVariable("numeroRadicacion"));
        assertEquals("Juan Revisor", context.getVariable("nombreRevisor"));
    }

    @Test
    void enviarNotificacionDocumentosPendientes_DebeEnviarEmailConDocumentos() {
        // Arrange
        String documentosFaltantes = "Cédula de ciudadanía, Certificado de libertad y tradición";

        // Act
        notificacionService.enviarNotificacionDocumentosPendientes(tramitePrueba, documentosFaltantes);

        // Assert
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine, timeout(1000)).process(eq("emails/documentos-pendientes"), contextCaptor.capture());
        verify(mailSender, timeout(1000)).send(any(MimeMessage.class));

        // Verificar que los documentos faltantes están en el contexto
        Context context = contextCaptor.getValue();
        assertEquals(documentosFaltantes, context.getVariable("documentosFaltantes"));
    }

    @Test
    void enviarNotificacionCambioEstado_ConRevisorDiferente_DebeEnviarAAmbasParte() {
        // Arrange
        tramitePrueba.setRevisorAsignado(revisor);
        tramitePrueba.setEstadoActual(Tramite.EstadoTramite.EN_REVISION);
        Tramite.EstadoTramite estadoAnterior = Tramite.EstadoTramite.ASIGNADO;
        String comentarios = "Iniciando revisión técnica";

        // Act
        notificacionService.enviarNotificacionCambioEstado(tramitePrueba, estadoAnterior, comentarios);

        // Assert - Debe enviar 2 emails: uno al solicitante y otro al revisor
        verify(templateEngine, timeout(1000).times(2)).process(any(String.class), any(Context.class));
        verify(mailSender, timeout(1000).times(2)).send(any(MimeMessage.class));
    }

    @Test
    void enviarNotificacionCambioEstado_ErrorEnEnvio_DebeLoggearError() {
        // Arrange
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Error SMTP"));
        Tramite.EstadoTramite estadoAnterior = Tramite.EstadoTramite.RADICADO;

        // Act & Assert - No debe lanzar excepción, solo loggear
        assertDoesNotThrow(() -> {
            notificacionService.enviarNotificacionCambioEstado(tramitePrueba, estadoAnterior, "Comentarios");
        });
    }

    @Test
    void generarAsuntoPorEstado_EstadoAprobado_DebeGenerarAsuntoCorrect() {
        // Arrange
        tramitePrueba.setEstadoActual(Tramite.EstadoTramite.APROBADO);

        // Act
        notificacionService.enviarNotificacionCambioEstado(tramitePrueba, Tramite.EstadoTramite.EN_REVISION, "");

        // Assert
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine, timeout(1000)).process(any(String.class), contextCaptor.capture());

        // El contexto debe contener las variables correctas
        Context context = contextCaptor.getValue();
        assertEquals("APROBADO", context.getVariable("estadoActual"));
        assertEquals("TEST-001", context.getVariable("numeroRadicacion"));
    }

    private void crearDatosPrueba() {
        entidad = new Entidad();
        entidad.setId(1L);
        entidad.setNombre("Entidad Test");

        solicitante = new Usuario();
        solicitante.setId(1L);
        solicitante.setNombreCompleto("Juan Solicitante");
        solicitante.setCorreoElectronico("solicitante@test.com");
        solicitante.setEntidad(entidad);

        revisor = new Usuario();
        revisor.setId(2L);
        revisor.setNombreCompleto("Juan Revisor");
        revisor.setCorreoElectronico("revisor@test.com");
        revisor.setEntidad(entidad);

        tipoTramite = new TipoTramite();
        tipoTramite.setIdTipoTramite(1L);
        tipoTramite.setNombre("Licencia de Construcción");

        tramitePrueba = new Tramite();
        tramitePrueba.setIdTramite(1L);
        tramitePrueba.setNumeroRadicacion("TEST-001");
        tramitePrueba.setObjetoTramite("Construcción de vivienda unifamiliar");
        tramitePrueba.setEstadoActual(Tramite.EstadoTramite.RADICADO);
        tramitePrueba.setFechaRadicacion(LocalDate.now());
        tramitePrueba.setFechaCreacion(LocalDateTime.now());
        tramitePrueba.setFechaUltimoCambioEstado(LocalDateTime.now());
        tramitePrueba.setEntidad(entidad);
        tramitePrueba.setSolicitante(solicitante);
        tramitePrueba.setTipoTramite(tipoTramite);
    }
}