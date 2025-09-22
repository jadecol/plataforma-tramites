package com.gestion.tramites.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestion.tramites.model.*;
import com.gestion.tramites.repository.*;
import com.gestion.tramites.security.test.WithMockCustomUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Testcontainers
@Transactional
class ReporteControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb").withUsername("test").withPassword("test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntidadRepository entidadRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TramiteRepository tramiteRepository;

    @Autowired
    private TipoTramiteRepository tipoTramiteRepository;

    private Entidad entidadA, entidadB;
    private TipoTramite tipoTramite;
    private Usuario usuarioA, usuarioB, adminA;

    @BeforeEach
    void setUp() {
        crearDatosPrueba();
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN_ENTIDAD"}, entidadId = 1L)
    void getDashboardMetricas_AdminEntidad_DebeRetornarMetricasFiltradas() throws Exception {
        mockMvc.perform(get("/api/reportes/dashboard").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalTramites", greaterThan(0)))
                .andExpect(jsonPath("$.tramitesUltimoMes", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.tramitesHoy", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.distribucionPorEstado", notNullValue()))
                .andExpect(jsonPath("$.tiempoPromedioProcesamientoDias", notNullValue()))
                .andExpect(jsonPath("$.tramitesVencidos", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.cargaPorRevisor", notNullValue()))
                .andExpect(jsonPath("$.tramitesPorTipo", notNullValue()))
                .andExpect(jsonPath("$.tasaAprobacionUltimoMes", notNullValue()))
                .andExpect(jsonPath("$.fechaGeneracion", notNullValue()))
                .andExpect(jsonPath("$.nombreEntidad", is("Entidad A")));
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN_GLOBAL"})
    void getDashboardMetricas_AdminGlobal_DebeRetornarTodasLasMetricas() throws Exception {
        mockMvc.perform(get("/api/reportes/dashboard").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalTramites", greaterThan(0)))
                .andExpect(jsonPath("$.nombreEntidad").doesNotExist());
    }

    @Test
    @WithMockCustomUser(roles = {"REVISOR"}, entidadId = 1L)
    void getDashboardMetricas_Revisor_DebePermitirAcceso() throws Exception {
        mockMvc.perform(get("/api/reportes/dashboard").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalTramites", greaterThan(0)));
    }

    @Test
    @WithMockCustomUser(roles = {"SOLICITANTE"}, entidadId = 1L)
    void getDashboardMetricas_Solicitante_DebeDenegarAcceso() throws Exception {
        mockMvc.perform(get("/api/reportes/dashboard").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getDashboardMetricas_SinAutenticacion_DebeRetornar401() throws Exception {
        mockMvc.perform(get("/api/reportes/dashboard").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    private void crearDatosPrueba() {
        entidadA = new Entidad();
        entidadA.setNombre("Entidad A");
        entidadA.setCodigo("EA");
        entidadA = entidadRepository.save(entidadA);

        entidadB = new Entidad();
        entidadB.setNombre("Entidad B");
        entidadB.setCodigo("EB");
        entidadB = entidadRepository.save(entidadB);

        usuarioA = new Usuario();
        usuarioA.setNombreCompleto("Usuario A");
        usuarioA.setCorreoElectronico("usuarioa@test.com");
        usuarioA.setContrasena("password");
        usuarioA.setRol(Usuario.Rol.SOLICITANTE);
        usuarioA.setEntidad(entidadA);
        usuarioA = usuarioRepository.save(usuarioA);

        usuarioB = new Usuario();
        usuarioB.setNombreCompleto("Usuario B");
        usuarioB.setCorreoElectronico("usuariob@test.com");
        usuarioB.setContrasena("password");
        usuarioB.setRol(Usuario.Rol.SOLICITANTE);
        usuarioB.setEntidad(entidadB);
        usuarioB = usuarioRepository.save(usuarioB);

        adminA = new Usuario();
        adminA.setNombreCompleto("Admin A");
        adminA.setCorreoElectronico("admina@test.com");
        adminA.setContrasena("password");
        adminA.setRol(Usuario.Rol.ADMIN_ENTIDAD);
        adminA.setEntidad(entidadA);
        adminA = usuarioRepository.save(adminA);

        tipoTramite = new TipoTramite();
        tipoTramite.setNombre("Licencia de Construcción");
        tipoTramite.setEntidad(entidadA);
        tipoTramite = tipoTramiteRepository.save(tipoTramite);

        crearTramitesPrueba();
    }

    private void crearTramitesPrueba() {
        Tramite tramiteA1 = new Tramite();
        tramiteA1.setNumeroRadicacion("EA-001-2024");
        tramiteA1.setEntidad(entidadA);
        tramiteA1.setSolicitante(usuarioA);
        tramiteA1.setTipoTramite(tipoTramite);
        tramiteA1.setObjetoTramite("Construcción casa unifamiliar");
        tramiteA1.setEstadoActual(Tramite.EstadoTramite.RADICADO);
        tramiteA1.setFechaRadicacion(LocalDate.now());
        tramiteA1.setFechaCreacion(LocalDateTime.now());
        tramiteRepository.save(tramiteA1);

        Tramite tramiteA2 = new Tramite();
        tramiteA2.setNumeroRadicacion("EA-002-2024");
        tramiteA2.setEntidad(entidadA);
        tramiteA2.setSolicitante(usuarioA);
        tramiteA2.setTipoTramite(tipoTramite);
        tramiteA2.setObjetoTramite("Ampliación edificio comercial");
        tramiteA2.setEstadoActual(Tramite.EstadoTramite.EN_REVISION);
        tramiteA2.setFechaRadicacion(LocalDate.now().minusDays(15));
        tramiteA2.setFechaCreacion(LocalDateTime.now().minusDays(15));
        tramiteRepository.save(tramiteA2);

        TipoTramite tipoTramiteB = new TipoTramite();
        tipoTramiteB.setNombre("Permiso de Uso");
        tipoTramiteB.setEntidad(entidadB);
        tipoTramiteB = tipoTramiteRepository.save(tipoTramiteB);

        Tramite tramiteB1 = new Tramite();
        tramiteB1.setNumeroRadicacion("EB-001-2024");
        tramiteB1.setEntidad(entidadB);
        tramiteB1.setSolicitante(usuarioB);
        tramiteB1.setTipoTramite(tipoTramiteB);
        tramiteB1.setObjetoTramite("Permiso para restaurante");
        tramiteB1.setEstadoActual(Tramite.EstadoTramite.APROBADO);
        tramiteB1.setFechaRadicacion(LocalDate.now().minusDays(30));
        tramiteB1.setFechaCreacion(LocalDateTime.now().minusDays(30));
        tramiteB1.setFechaFinalizacion(LocalDateTime.now().minusDays(5));
        tramiteRepository.save(tramiteB1);
    }
}
