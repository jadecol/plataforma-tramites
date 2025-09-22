package com.gestion.tramites.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestion.tramites.model.Entidad;
import com.gestion.tramites.model.TipoTramite;
import com.gestion.tramites.model.Tramite;
import com.gestion.tramites.model.Usuario;
import com.gestion.tramites.repository.EntidadRepository;
import com.gestion.tramites.repository.TipoTramiteRepository;
import com.gestion.tramites.repository.TramiteRepository;
import com.gestion.tramites.repository.UsuarioRepository;
import com.gestion.tramites.service.SeguimientoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración para el Portal Ciudadano
 * Verifica el funcionamiento completo de las consultas públicas sin autenticación
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
public class PortalCiudadanoIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private SeguimientoService seguimientoService;

    @Autowired
    private TramiteRepository tramiteRepository;

    @Autowired
    private EntidadRepository entidadRepository;

    @Autowired
    private TipoTramiteRepository tipoTramiteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    // Datos de prueba
    private Entidad entidadTest;
    private TipoTramite tipoTramiteTest;
    private Usuario solicitanteTest;
    private Tramite tramiteTest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Limpiar datos existentes
        tramiteRepository.deleteAll();
        usuarioRepository.deleteAll();
        tipoTramiteRepository.deleteAll();
        entidadRepository.deleteAll();

        // Crear datos de prueba
        crearDatosPrueba();
    }

    @Test
    void deberiaConsultarTramitePorNumeroRadicacion() throws Exception {
        mockMvc.perform(get("/api/public/consulta/tramite/{numeroRadicacion}",
                tramiteTest.getNumeroRadicacion()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.numeroRadicacion", is(tramiteTest.getNumeroRadicacion())))
                .andExpect(jsonPath("$.objetoTramite", is(tramiteTest.getObjetoTramite())))
                .andExpect(jsonPath("$.estadoActual", is(tramiteTest.getEstadoActual().name())))
                .andExpect(jsonPath("$.nombreEntidad", is(entidadTest.getNombre())))
                .andExpect(jsonPath("$.tipoTramite", is(tipoTramiteTest.getNombre())))
                .andExpect(jsonPath("$.descripcionEstado", notNullValue()))
                .andExpect(jsonPath("$.diasTranscurridos", greaterThanOrEqualTo(0)));
    }

    @Test
    void deberiaRetornar404ParaTramiteNoExistente() throws Exception {
        String numeroInexistente = "11001-0-25-9999";

        mockMvc.perform(get("/api/public/consulta/tramite/{numeroRadicacion}", numeroInexistente))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("TRAMITE_NO_ENCONTRADO")))
                .andExpect(jsonPath("$.mensaje", containsString("No se encontró un trámite")))
                .andExpect(jsonPath("$.sugerencia", notNullValue()));
    }

    @Test
    void deberiaValidarFormatoNumeroRadicacion() throws Exception {
        String numeroInvalido = "FORMATO-MALO";

        mockMvc.perform(get("/api/public/consulta/tramite/{numeroRadicacion}", numeroInvalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deberiaConsultarTramitesPorEmail() throws Exception {
        mockMvc.perform(get("/api/public/consulta/tramites/email/{emailSolicitante}",
                solicitanteTest.getCorreoElectronico()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].numeroRadicacion", is(tramiteTest.getNumeroRadicacion())))
                .andExpect(jsonPath("$[0].objetoTramite", is(tramiteTest.getObjetoTramite())))
                .andExpect(jsonPath("$[0].estadoActual", is(tramiteTest.getEstadoActual().name())));
    }

    @Test
    void deberiaRetornarListaVaciaParaEmailSinTramites() throws Exception {
        String emailSinTramites = "sintramites@ejemplo.com";

        mockMvc.perform(get("/api/public/consulta/tramites/email/{emailSolicitante}", emailSinTramites))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deberiaValidarEmailInvalido() throws Exception {
        String emailInvalido = "email-malformado";

        mockMvc.perform(get("/api/public/consulta/tramites/email/{emailSolicitante}", emailInvalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deberiaValidarAccesoATramite() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
            "numeroRadicacion", tramiteTest.getNumeroRadicacion(),
            "emailSolicitante", solicitanteTest.getCorreoElectronico()
        ));

        mockMvc.perform(post("/api/public/consulta/tramite/validar-acceso")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tieneAcceso", is(true)))
                .andExpect(jsonPath("$.numeroRadicacion", is(tramiteTest.getNumeroRadicacion())))
                .andExpect(jsonPath("$.mensaje", containsString("Acceso autorizado")));
    }

    @Test
    void deberiaRechazarAccesoConEmailIncorrecto() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
            "numeroRadicacion", tramiteTest.getNumeroRadicacion(),
            "emailSolicitante", "otro@ejemplo.com"
        ));

        mockMvc.perform(post("/api/public/consulta/tramite/validar-acceso")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tieneAcceso", is(false)))
                .andExpect(jsonPath("$.mensaje", containsString("No se encontró trámite")));
    }

    @Test
    void deberiaConsultarTramitesRecientesDeEntidad() throws Exception {
        mockMvc.perform(get("/api/public/consulta/entidad/{entidadId}/tramites-recientes",
                entidadTest.getId())
                .param("limite", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$", hasSize(lessThanOrEqualTo(5))));
    }

    @Test
    void deberiaLimitarConsultaTramitesRecientes() throws Exception {
        // Probar límite excesivo
        mockMvc.perform(get("/api/public/consulta/entidad/{entidadId}/tramites-recientes",
                entidadTest.getId())
                .param("limite", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(lessThanOrEqualTo(20))));
    }

    @Test
    void deberiaObtenerEstadisticasPublicasDeEntidad() throws Exception {
        mockMvc.perform(get("/api/public/consulta/entidad/{entidadId}/estadisticas",
                entidadTest.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.entidadId", is(entidadTest.getId().intValue())))
                .andExpect(jsonPath("$.tramitesMesActual", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.tramitesAnoActual", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.promedioDiasProcesamiento", greaterThan(0.0)));
    }

    @Test
    void deberiaObtenerDescripcionEstadosTramite() throws Exception {
        mockMvc.perform(get("/api/public/consulta/estados-tramite"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.RADICADO", notNullValue()))
                .andExpect(jsonPath("$.EN_REVISION", notNullValue()))
                .andExpect(jsonPath("$.OBSERVADO", notNullValue()))
                .andExpect(jsonPath("$.APROBADO", notNullValue()))
                .andExpect(jsonPath("$.RECHAZADO", notNullValue()))
                .andExpect(jsonPath("$.ARCHIVADO", notNullValue()));
    }

    @Test
    void deberiaObtenerInformacionFormatosRadicacion() throws Exception {
        mockMvc.perform(get("/api/public/consulta/ayuda/formatos-radicacion"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.secretarias.formato", is("DANE-0-YY-NNNN")))
                .andExpect(jsonPath("$.secretarias.ejemplo", notNullValue()))
                .andExpect(jsonPath("$.curadurias.formato", is("DANE-CUR-YY-NNNN")))
                .andExpect(jsonPath("$.curadurias.ejemplo", notNullValue()))
                .andExpect(jsonPath("$.instrucciones", hasSize(greaterThan(0))));
    }

    @Test
    void deberiaVerificarSaludDelServicio() throws Exception {
        mockMvc.perform(get("/api/public/consulta/salud"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.servicio", is("Portal Ciudadano")))
                .andExpect(jsonPath("$.estado", is("ACTIVO")))
                .andExpect(jsonPath("$.version", notNullValue()))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.descripcion", notNullValue()));
    }

    @Test
    void deberiaFuncionarSeguimientoServiceDirectamente() {
        // Prueba directa del servicio (sin controlador)
        SeguimientoService.ConsultaTramitePublico consulta =
            seguimientoService.consultarPorNumeroRadicacion(tramiteTest.getNumeroRadicacion());

        assert consulta != null;
        assert consulta.getNumeroRadicacion().equals(tramiteTest.getNumeroRadicacion());
        assert consulta.getObjetoTramite().equals(tramiteTest.getObjetoTramite());
        assert consulta.getEstadoActual() == tramiteTest.getEstadoActual();
        assert consulta.getNombreEntidad().equals(entidadTest.getNombre());
        assert consulta.getDiasTranscurridos() >= 0;
    }

    @Test
    void deberiaFiltrarInformacionSensible() {
        // Verificar que las observaciones se filtran correctamente
        SeguimientoService.ConsultaTramitePublico consulta =
            seguimientoService.consultarPorNumeroRadicacion(tramiteTest.getNumeroRadicacion());

        assert consulta.getObservacionesPublicas() != null;
        // Las observaciones no deben contener información sensible original
        assert !consulta.getObservacionesPublicas().contains("contraseña123");
    }

    @Test
    void deberiaValidarAccesoCorrectamente() {
        // Acceso válido
        boolean accesoValido = seguimientoService.validarAccesoPublico(
            tramiteTest.getNumeroRadicacion(),
            solicitanteTest.getCorreoElectronico()
        );
        assert accesoValido;

        // Acceso inválido
        boolean accesoInvalido = seguimientoService.validarAccesoPublico(
            tramiteTest.getNumeroRadicacion(),
            "otro@ejemplo.com"
        );
        assert !accesoInvalido;
    }

    @Test
    void deberiaConsultarMultiplesTramitesPorEmail() {
        // Crear segundo trámite para el mismo solicitante
        Tramite segundoTramite = crearTramiteAdicional();

        List<SeguimientoService.ConsultaTramitePublico> tramites =
            seguimientoService.consultarPorEmailSolicitante(solicitanteTest.getCorreoElectronico());

        assert tramites.size() == 2;
        assert tramites.stream().anyMatch(t -> t.getNumeroRadicacion().equals(tramiteTest.getNumeroRadicacion()));
        assert tramites.stream().anyMatch(t -> t.getNumeroRadicacion().equals(segundoTramite.getNumeroRadicacion()));
    }

    // Métodos auxiliares para crear datos de prueba

    private void crearDatosPrueba() {
        // Crear entidad
        entidadTest = new Entidad();
        entidadTest.setNombre("Secretaría de Planeación Test");
        entidadTest.setNit("900123456-1");
        entidadTest.setCodigoDane("11001");
        entidadTest.setDireccion("Calle Test 123");
        entidadTest.setTelefono("601-1234567");
        entidadTest.setEmail("test@entidad.gov.co");
        entidadTest.setActivo(true);
        entidadTest = entidadRepository.save(entidadTest);

        // Crear tipo de trámite
        tipoTramiteTest = new TipoTramite();
        tipoTramiteTest.setNombre("Licencia de Construcción Test");
        tipoTramiteTest.setDescripcion("Licencia para construcción de prueba");
        tipoTramiteTest.setActivo(true);
        tipoTramiteTest = tipoTramiteRepository.save(tipoTramiteTest);

        // Crear solicitante
        solicitanteTest = new Usuario();
        solicitanteTest.setNombreCompleto("Ciudadano Test");
        solicitanteTest.setCorreoElectronico("ciudadano.test@ejemplo.com");
        solicitanteTest.setContrasena("password123");
        solicitanteTest.setRol(Usuario.Rol.SOLICITANTE);
        solicitanteTest.setEntidad(entidadTest);
        solicitanteTest.setActivo(true);
        solicitanteTest = usuarioRepository.save(solicitanteTest);

        // Crear trámite
        tramiteTest = new Tramite();
        tramiteTest.setNumeroRadicacion("11001-0-25-0001");
        tramiteTest.setObjetoTramite("Construcción de casa unifamiliar de prueba");
        tramiteTest.setFechaRadicacion(LocalDate.now());
        tramiteTest.setFechaCreacion(LocalDateTime.now());
        tramiteTest.setEstadoActual(Tramite.EstadoTramite.RADICADO);
        tramiteTest.setEntidad(entidadTest);
        tramiteTest.setTipoTramite(tipoTramiteTest);
        tramiteTest.setSolicitante(solicitanteTest);
        tramiteTest.setObservaciones("Observaciones de prueba con contraseña123 y datos internos");
        tramiteTest = tramiteRepository.save(tramiteTest);
    }

    private Tramite crearTramiteAdicional() {
        Tramite tramiteAdicional = new Tramite();
        tramiteAdicional.setNumeroRadicacion("11001-0-25-0002");
        tramiteAdicional.setObjetoTramite("Ampliación de vivienda");
        tramiteAdicional.setFechaRadicacion(LocalDate.now().minusDays(5));
        tramiteAdicional.setFechaCreacion(LocalDateTime.now().minusDays(5));
        tramiteAdicional.setEstadoActual(Tramite.EstadoTramite.EN_REVISION);
        tramiteAdicional.setEntidad(entidadTest);
        tramiteAdicional.setTipoTramite(tipoTramiteTest);
        tramiteAdicional.setSolicitante(solicitanteTest);
        return tramiteRepository.save(tramiteAdicional);
    }
}