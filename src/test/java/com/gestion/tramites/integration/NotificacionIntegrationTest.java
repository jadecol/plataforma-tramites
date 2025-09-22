package com.gestion.tramites.integration;

import com.gestion.tramites.model.*;
import com.gestion.tramites.repository.*;
import com.gestion.tramites.service.NotificacionService;
import com.gestion.tramites.service.TramiteService;
import com.gestion.tramites.dto.tramite.TramiteRequestDTO;
import com.gestion.tramites.security.test.WithMockCustomUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
class NotificacionIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private TramiteService tramiteService;

    @SpyBean
    private NotificacionService notificacionService;

    @Autowired
    private EntidadRepository entidadRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TipoTramiteRepository tipoTramiteRepository;

    @Autowired
    private TramiteRepository tramiteRepository;

    private Entidad entidad;
    private Usuario solicitante;
    private Usuario revisor;
    private TipoTramite tipoTramite;

    @BeforeEach
    void setUp() {
        crearDatosPrueba();
    }

    @Test
    @WithMockCustomUser(roles = {"SOLICITANTE"}, entidadId = 1L, userId = 1L)
    void crearTramite_DebeEnviarNotificacionTramiteCreado() throws Exception {
        // Arrange
        TramiteRequestDTO requestDTO = new TramiteRequestDTO();
        requestDTO.setObjetoTramite("Construcción de vivienda unifamiliar");
        requestDTO.setDescripcionProyecto("Casa de 2 pisos en zona residencial");
        requestDTO.setDireccionInmueble("Calle 123 #45-67");
        requestDTO.setIdSolicitante(solicitante.getId());
        requestDTO.setIdTipoTramite(tipoTramite.getIdTipoTramite());

        // Act
        tramiteService.crearTramite(requestDTO);

        // Assert
        verify(notificacionService, timeout(2000)).enviarNotificacionTramiteCreado(any(Tramite.class));
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN_ENTIDAD"}, entidadId = 1L, userId = 2L)
    void asignarRevisor_DebeEnviarNotificacionAsignacion() throws Exception {
        // Arrange
        TramiteRequestDTO requestDTO = new TramiteRequestDTO();
        requestDTO.setObjetoTramite("Permiso de uso del suelo");
        requestDTO.setIdSolicitante(solicitante.getId());
        requestDTO.setIdTipoTramite(tipoTramite.getIdTipoTramite());

        var tramiteCreado = tramiteService.crearTramite(requestDTO);

        // Act
        tramiteService.asignarRevisor(tramiteCreado.getId(), revisor.getId());

        // Assert
        verify(notificacionService, timeout(2000)).enviarNotificacionTramiteCreado(any(Tramite.class));
        verify(notificacionService, timeout(2000)).enviarNotificacionAsignacionRevisor(any(Tramite.class), any(Usuario.class));
    }

    @Test
    @WithMockCustomUser(roles = {"REVISOR"}, entidadId = 1L, userId = 3L)
    void actualizarEstado_EstadoAprobado_DebeEnviarNotificacionCambioEstado() throws Exception {
        // Arrange
        Tramite tramite = crearTramiteParaPruebas();
        tramite.setEstadoActual(Tramite.EstadoTramite.EN_REVISION);
        tramite = tramiteRepository.save(tramite);

        // Act
        tramiteService.actualizarEstado(tramite.getIdTramite(), Tramite.EstadoTramite.APROBADO,
                                      "Trámite aprobado - Cumple con todos los requisitos");

        // Assert
        verify(notificacionService, timeout(2000)).enviarNotificacionCambioEstado(
            any(Tramite.class),
            eq(Tramite.EstadoTramite.EN_REVISION),
            eq("Trámite aprobado - Cumple con todos los requisitos")
        );
    }

    @Test
    @WithMockCustomUser(roles = {"REVISOR"}, entidadId = 1L, userId = 3L)
    void actualizarEstado_EstadoPendienteDocumentos_DebeEnviarNotificacionEspecifica() throws Exception {
        // Arrange
        Tramite tramite = crearTramiteParaPruebas();
        tramite.setEstadoActual(Tramite.EstadoTramite.EN_REVISION);
        tramite = tramiteRepository.save(tramite);

        String documentosFaltantes = "Faltan: cédula de ciudadanía, certificado de libertad y tradición";

        // Act
        tramiteService.actualizarEstado(tramite.getIdTramite(), Tramite.EstadoTramite.PENDIENTE_DOCUMENTOS,
                                      documentosFaltantes);

        // Assert
        verify(notificacionService, timeout(2000)).enviarNotificacionCambioEstado(
            any(Tramite.class),
            eq(Tramite.EstadoTramite.EN_REVISION),
            eq(documentosFaltantes)
        );
        verify(notificacionService, timeout(2000)).enviarNotificacionDocumentosPendientes(
            any(Tramite.class),
            eq(documentosFaltantes)
        );
    }

    @Test
    @WithMockCustomUser(roles = {"REVISOR"}, entidadId = 1L, userId = 3L)
    void actualizarEstado_EstadoRechazado_DebeEnviarNotificacionCambioEstado() throws Exception {
        // Arrange
        Tramite tramite = crearTramiteParaPruebas();
        tramite.setEstadoActual(Tramite.EstadoTramite.EN_REVISION);
        tramite = tramiteRepository.save(tramite);

        String motivoRechazo = "No cumple con la normatividad de construcción vigente";

        // Act
        tramiteService.actualizarEstado(tramite.getIdTramite(), Tramite.EstadoTramite.RECHAZADO,
                                      motivoRechazo);

        // Assert
        verify(notificacionService, timeout(2000)).enviarNotificacionCambioEstado(
            any(Tramite.class),
            eq(Tramite.EstadoTramite.EN_REVISION),
            eq(motivoRechazo)
        );
    }

    private void crearDatosPrueba() {
        entidad = new Entidad();
        entidad.setNombre("Entidad Test");
        entidad.setCodigo("ET");
        entidad = entidadRepository.save(entidad);

        solicitante = new Usuario();
        solicitante.setNombreCompleto("Juan Solicitante");
        solicitante.setCorreoElectronico("solicitante@test.com");
        solicitante.setContrasena("password");
        solicitante.setRol(Usuario.Rol.SOLICITANTE);
        solicitante.setEntidad(entidad);
        solicitante = usuarioRepository.save(solicitante);

        revisor = new Usuario();
        revisor.setNombreCompleto("María Revisora");
        revisor.setCorreoElectronico("revisora@test.com");
        revisor.setContrasena("password");
        revisor.setRol(Usuario.Rol.REVISOR);
        revisor.setEntidad(entidad);
        revisor = usuarioRepository.save(revisor);

        tipoTramite = new TipoTramite();
        tipoTramite.setNombre("Licencia de Construcción");
        tipoTramite.setEntidad(entidad);
        tipoTramite = tipoTramiteRepository.save(tipoTramite);
    }

    private Tramite crearTramiteParaPruebas() {
        Tramite tramite = new Tramite();
        tramite.setNumeroRadicacion("TEST-001-2024");
        tramite.setObjetoTramite("Construcción de vivienda unifamiliar");
        tramite.setDescripcionProyecto("Casa de 2 pisos en zona residencial");
        tramite.setDireccionInmueble("Calle 123 #45-67");
        tramite.setEstadoActual(Tramite.EstadoTramite.RADICADO);
        tramite.setEntidad(entidad);
        tramite.setSolicitante(solicitante);
        tramite.setTipoTramite(tipoTramite);

        return tramiteRepository.save(tramite);
    }
}