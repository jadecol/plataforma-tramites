package com.gestion.tramites.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestion.tramites.model.EntidadGubernamental;
import com.gestion.tramites.repository.EntidadGubernamentalRepository;
import com.gestion.tramites.security.test.WithMockCustomUser;
import com.gestion.tramites.service.VerificacionEntidadGubernamentalService;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Testcontainers
@Transactional
class EntidadGubernamentalIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntidadGubernamentalRepository entidadGubernamentalRepository;

    private VerificacionEntidadGubernamentalService.SolicitudRegistroEntidad solicitudRegistro;
    private EntidadGubernamental entidadPrueba;

    @BeforeEach
    void setUp() {
        crearDatosPrueba();
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN_ENTIDAD"}, entidadId = 1L)
    void registrarEntidad_DatosValidos_DebeCrearEntidad() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/entidades-gubernamentales/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solicitudRegistro)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nombre", is(solicitudRegistro.getNombre())))
                .andExpect(jsonPath("$.codigoDane", is(solicitudRegistro.getCodigoDane())))
                .andExpect(jsonPath("$.nit", is(solicitudRegistro.getNit())))
                .andExpect(jsonPath("$.dominioOficial", is(solicitudRegistro.getDominioOficial())))
                .andExpect(jsonPath("$.estadoVerificacion", is("PENDIENTE")))
                .andExpect(jsonPath("$.activo", is(true)));
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN_ENTIDAD"}, entidadId = 1L)
    void registrarEntidad_DominioInvalido_DebeRetornar400() throws Exception {
        // Arrange
        solicitudRegistro.setDominioOficial("entidad-falsa.com");

        // Act & Assert
        mockMvc.perform(post("/api/entidades-gubernamentales/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solicitudRegistro)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", containsString("Dominio no válido")));
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN_GLOBAL"}, entidadId = 1L)
    void verificarEntidad_AdminGlobal_DebePermitirVerificacion() throws Exception {
        // Arrange
        entidadPrueba = crearEntidadPendiente();

        VerificacionEntidadGubernamentalService.SolicitudVerificacion solicitud =
                new VerificacionEntidadGubernamentalService.SolicitudVerificacion(true, "Verificación aprobada");

        // Act & Assert
        mockMvc.perform(post("/api/entidades-gubernamentales/{entidadId}/verificar", entidadPrueba.getIdEntidadGubernamental())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.estadoVerificacion", is("VERIFICADA")))
                .andExpect(jsonPath("$.verificadoPor", notNullValue()))
                .andExpect(jsonPath("$.fechaVerificacion", notNullValue()));
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN_ENTIDAD"}, entidadId = 1L)
    void verificarEntidad_AdminEntidad_DebeRetornar403() throws Exception {
        // Arrange
        entidadPrueba = crearEntidadPendiente();

        VerificacionEntidadGubernamentalService.SolicitudVerificacion solicitud =
                new VerificacionEntidadGubernamentalService.SolicitudVerificacion(true, "Verificación aprobada");

        // Act & Assert
        mockMvc.perform(post("/api/entidades-gubernamentales/{entidadId}/verificar", entidadPrueba.getIdEntidadGubernamental())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN_GLOBAL"}, entidadId = 1L)
    void suspenderEntidad_EntidadVerificada_DebePermitirSuspension() throws Exception {
        // Arrange
        entidadPrueba = crearEntidadVerificada();

        // Act & Assert
        mockMvc.perform(post("/api/entidades-gubernamentales/{entidadId}/suspender", entidadPrueba.getIdEntidadGubernamental())
                .param("motivo", "Irregularidades detectadas")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.estadoVerificacion", is("SUSPENDIDA")))
                .andExpect(jsonPath("$.activo", is(false)));
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN_GLOBAL"}, entidadId = 1L)
    void obtenerEntidadesPendientes_DebeRetornarLista() throws Exception {
        // Arrange
        crearEntidadPendiente();

        // Act & Assert
        mockMvc.perform(get("/api/entidades-gubernamentales/pendientes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].estadoVerificacion", is("PENDIENTE")));
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN_ENTIDAD"}, entidadId = 1L)
    void buscarPorDominio_DominioExistente_DebeRetornarEntidad() throws Exception {
        // Arrange
        entidadPrueba = crearEntidadVerificada();

        // Act & Assert
        mockMvc.perform(get("/api/entidades-gubernamentales/buscar/dominio/{dominio}", "www.bogota.gov.co")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.dominioOficial", is("www.bogota.gov.co")))
                .andExpect(jsonPath("$.nombre", notNullValue()));
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN_ENTIDAD"}, entidadId = 1L)
    void buscarPorDominio_DominioNoExistente_DebeRetornar404() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/entidades-gubernamentales/buscar/dominio/{dominio}", "www.noexiste.gov.co")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN_ENTIDAD"}, entidadId = 1L)
    void validarDominio_DominioOficial_DebeRetornarValido() throws Exception {
        // Arrange
        String requestBody = """
                {
                    "dominio": "www.bogota.gov.co",
                    "tipoEntidad": "ALCALDIA"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/entidades-gubernamentales/validar-dominio")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.valido", is(true)))
                .andExpect(jsonPath("$.mensaje", containsString("verificado en lista oficial")))
                .andExpect(jsonPath("$.tipoValidacion", is("LISTA_BLANCA")));
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN_ENTIDAD"}, entidadId = 1L)
    void validarDominio_DominioInvalido_DebeRetornarInvalido() throws Exception {
        // Arrange
        String requestBody = """
                {
                    "dominio": "www.entidad-falsa.com",
                    "tipoEntidad": "ALCALDIA"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/entidades-gubernamentales/validar-dominio")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.valido", is(false)))
                .andExpect(jsonPath("$.mensaje", containsString("no cumple")));
    }

    @Test
    @WithMockCustomUser(roles = {"REVISOR"}, entidadId = 1L)
    void validarParaOperacionCritica_EntidadVerificada_DebeRetornarValido() throws Exception {
        // Arrange
        entidadPrueba = crearEntidadVerificada();

        // Act & Assert
        mockMvc.perform(post("/api/entidades-gubernamentales/{entidadId}/validar-operacion", entidadPrueba.getIdEntidadGubernamental())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("valid")))
                .andExpect(jsonPath("$.message", containsString("válida para operación crítica")));
    }

    @Test
    @WithMockCustomUser(roles = {"REVISOR"}, entidadId = 1L)
    void validarParaOperacionCritica_EntidadSuspendida_DebeRetornarInvalido() throws Exception {
        // Arrange
        entidadPrueba = crearEntidadSuspendida();

        // Act & Assert
        mockMvc.perform(post("/api/entidades-gubernamentales/{entidadId}/validar-operacion", entidadPrueba.getIdEntidadGubernamental())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("invalid")))
                .andExpect(jsonPath("$.message", containsString("no está verificada o está suspendida")));
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN_ENTIDAD"}, entidadId = 1L)
    void obtenerDominiosOficiales_DebeRetornarLista() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/entidades-gubernamentales/dominios-oficiales")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$", hasItem("www.bogota.gov.co")))
                .andExpect(jsonPath("$", hasItem("curaduria1bogota.com")));
    }

    @Test
    void obtenerTiposEntidad_SinAutenticacion_DebeRetornarTipos() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/entidades-gubernamentales/tipos-entidad")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$", hasItem("ALCALDIA")))
                .andExpect(jsonPath("$", hasItem("CURADURIA_URBANA")))
                .andExpect(jsonPath("$", hasItem("GOBERNACION")));
    }

    @Test
    void registrarEntidad_SinAutenticacion_DebeRetornar401() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/entidades-gubernamentales/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solicitudRegistro)))
                .andExpect(status().isUnauthorized());
    }

    private void crearDatosPrueba() {
        solicitudRegistro = new VerificacionEntidadGubernamentalService.SolicitudRegistroEntidad();
        solicitudRegistro.setNombre("Alcaldía de Bogotá Test");
        solicitudRegistro.setCodigoDane("11001");
        solicitudRegistro.setNit("123456789-0");
        solicitudRegistro.setTipoEntidad(EntidadGubernamental.TipoEntidadGubernamental.ALCALDIA);
        solicitudRegistro.setDominioOficial("www.bogota.gov.co");
        solicitudRegistro.setSitioWebOficial("https://www.bogota.gov.co");
        solicitudRegistro.setEmailOficial("contacto@bogota.gov.co");
        solicitudRegistro.setTelefonoOficial("+57 1 3820000");
        solicitudRegistro.setDireccionFisica("Carrera 8 No. 10-65");
        solicitudRegistro.setDepartamento("Cundinamarca");
        solicitudRegistro.setMunicipio("Bogotá");
    }

    private EntidadGubernamental crearEntidadPendiente() {
        EntidadGubernamental entidad = new EntidadGubernamental();
        entidad.setNombre("Alcaldía de Bogotá Test");
        entidad.setCodigoDane("11001");
        entidad.setNit("123456789-0");
        entidad.setTipoEntidad(EntidadGubernamental.TipoEntidadGubernamental.ALCALDIA);
        entidad.setDominioOficial("www.bogota.gov.co");
        entidad.setSitioWebOficial("https://www.bogota.gov.co");
        entidad.setEmailOficial("contacto@bogota.gov.co");
        entidad.setTelefonoOficial("+57 1 3820000");
        entidad.setDireccionFisica("Carrera 8 No. 10-65");
        entidad.setDepartamento("Cundinamarca");
        entidad.setMunicipio("Bogotá");
        entidad.setEstadoVerificacion(EntidadGubernamental.EstadoVerificacion.PENDIENTE);
        entidad.setActivo(true);

        return entidadGubernamentalRepository.save(entidad);
    }

    private EntidadGubernamental crearEntidadVerificada() {
        EntidadGubernamental entidad = crearEntidadPendiente();
        entidad.verificar("admin@test.com", "Verificación automática para pruebas");
        return entidadGubernamentalRepository.save(entidad);
    }

    private EntidadGubernamental crearEntidadSuspendida() {
        EntidadGubernamental entidad = crearEntidadVerificada();
        entidad.suspender("admin@test.com", "Suspensión para pruebas");
        return entidadGubernamentalRepository.save(entidad);
    }
}