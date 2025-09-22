package com.gestion.tramites.integration;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Testcontainers
@Transactional
class DocumentoIntegrationTest {

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
    private EntidadRepository entidadRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TipoTramiteRepository tipoTramiteRepository;

    @Autowired
    private TramiteRepository tramiteRepository;

    @Autowired
    private DocumentoRepository documentoRepository;

    private Entidad entidad;
    private Usuario solicitante;
    private Usuario revisor;
    private TipoTramite tipoTramite;
    private Tramite tramite;

    @BeforeEach
    void setUp() {
        crearDatosPrueba();
    }

    @Test
    @WithMockCustomUser(roles = {"SOLICITANTE"}, entidadId = 1L, userId = 1L)
    void subirDocumento_UsuarioSolicitante_DebePermitirSubida() throws Exception {
        // Arrange
        MockMultipartFile archivo = new MockMultipartFile(
            "archivo",
            "cedula_ciudadania.pdf",
            "application/pdf",
            "Contenido del archivo PDF de prueba".getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/documentos/tramite/{tramiteId}", tramite.getIdTramite())
                .file(archivo)
                .param("tipoDocumento", "CEDULA_CIUDADANIA")
                .param("descripcion", "Documento de identificación del solicitante")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nombreOriginal", is("cedula_ciudadania.pdf")))
                .andExpect(jsonPath("$.tipoDocumento", is("CEDULA_CIUDADANIA")))
                .andExpect(jsonPath("$.descripcionTipoDocumento", is("Cédula de Ciudadanía")))
                .andExpect(jsonPath("$.tamanoBytes", greaterThan(0)))
                .andExpect(jsonPath("$.version", is(1)))
                .andExpect(jsonPath("$.esVersionActual", is(true)))
                .andExpect(jsonPath("$.estado", is("ACTIVO")))
                .andExpect(jsonPath("$.tramiteId", is(tramite.getIdTramite().intValue())))
                .andExpect(jsonPath("$.nombreUsuarioSubida", is(solicitante.getNombreCompleto())));
    }

    @Test
    @WithMockCustomUser(roles = {"SOLICITANTE"}, entidadId = 1L, userId = 1L)
    void subirDocumento_ExtensionNoPermitida_DebeRetornar400() throws Exception {
        // Arrange
        MockMultipartFile archivo = new MockMultipartFile(
            "archivo",
            "virus.exe",
            "application/x-executable",
            "Contenido ejecutable".getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/documentos/tramite/{tramiteId}", tramite.getIdTramite())
                .file(archivo)
                .param("tipoDocumento", "OTRO")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", containsString("Extensión de archivo no permitida")));
    }

    @Test
    @WithMockCustomUser(roles = {"REVISOR"}, entidadId = 1L, userId = 2L)
    void obtenerDocumentosPorTramite_UsuarioRevisor_DebeRetornarDocumentos() throws Exception {
        // Arrange
        crearDocumentoPrueba();

        // Act & Assert
        mockMvc.perform(get("/api/documentos/tramite/{tramiteId}", tramite.getIdTramite())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombreOriginal", is("documento_prueba.pdf")))
                .andExpect(jsonPath("$[0].tipoDocumento", is("CEDULA_CIUDADANIA")));
    }

    @Test
    @WithMockCustomUser(roles = {"SOLICITANTE"}, entidadId = 2L, userId = 3L)
    void obtenerDocumentosPorTramite_UsuarioOtraEntidad_DebeRetornar404() throws Exception {
        // Arrange
        crearDocumentoPrueba();

        // Act & Assert
        mockMvc.perform(get("/api/documentos/tramite/{tramiteId}", tramite.getIdTramite())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser(roles = {"SOLICITANTE"}, entidadId = 1L, userId = 1L)
    void descargarDocumento_DocumentoExistente_DebeRetornarArchivo() throws Exception {
        // Arrange
        Documento documento = crearDocumentoPrueba();

        // Act & Assert
        mockMvc.perform(get("/api/documentos/{documentoId}/download", documento.getIdDocumento())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        containsString("attachment; filename=\"documento_prueba.pdf\"")));
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN_ENTIDAD"}, entidadId = 1L, userId = 4L)
    void eliminarDocumento_UsuarioAdmin_DebePermitirEliminacion() throws Exception {
        // Arrange
        Documento documento = crearDocumentoPrueba();

        // Act & Assert
        mockMvc.perform(delete("/api/documentos/{documentoId}", documento.getIdDocumento())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verificar que el documento está marcado como eliminado
        Documento documentoActualizado = documentoRepository.findById(documento.getIdDocumento()).orElse(null);
        assert documentoActualizado != null;
        assert documentoActualizado.getEstado() == Documento.EstadoDocumento.ELIMINADO;
    }

    @Test
    @WithMockCustomUser(roles = {"SOLICITANTE"}, entidadId = 1L, userId = 1L)
    void buscarDocumentos_ConTerminoBusqueda_DebeRetornarResultados() throws Exception {
        // Arrange
        crearDocumentoPrueba();

        // Act & Assert
        mockMvc.perform(get("/api/documentos/tramite/{tramiteId}/buscar", tramite.getIdTramite())
                .param("q", "documento")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombreOriginal", containsString("documento")));
    }

    @Test
    @WithMockCustomUser(roles = {"REVISOR"}, entidadId = 1L, userId = 2L)
    void obtenerDocumentosPorTipo_TipoEspecifico_DebeRetornarSoloEseTipo() throws Exception {
        // Arrange
        crearDocumentoPrueba();
        crearDocumentoPrueba("planos_arquitectonicos.dwg", Documento.TipoDocumento.PLANOS_ARQUITECTONICOS);

        // Act & Assert
        mockMvc.perform(get("/api/documentos/tramite/{tramiteId}/tipo/{tipoDocumento}",
                tramite.getIdTramite(), "CEDULA_CIUDADANIA")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipoDocumento", is("CEDULA_CIUDADANIA")));
    }

    @Test
    @WithMockCustomUser(roles = {"SOLICITANTE"}, entidadId = 1L, userId = 1L)
    void obtenerEstadisticas_TramiteConDocumentos_DebeRetornarEstadisticasCorrectas() throws Exception {
        // Arrange
        crearDocumentoPrueba();
        crearDocumentoPrueba("segundo_documento.pdf", Documento.TipoDocumento.FORMATO_UNICO_SOLICITUD);

        // Act & Assert
        mockMvc.perform(get("/api/documentos/tramite/{tramiteId}/estadisticas", tramite.getIdTramite())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cantidadDocumentos", is(2)))
                .andExpect(jsonPath("$.tamanoTotalBytes", greaterThan(0)))
                .andExpect(jsonPath("$.tamanoTotalLegible", notNullValue()))
                .andExpect(jsonPath("$.tramiteId", is(tramite.getIdTramite().intValue())));
    }

    @Test
    void subirDocumento_SinAutenticacion_DebeRetornar401() throws Exception {
        // Arrange
        MockMultipartFile archivo = new MockMultipartFile(
            "archivo",
            "test.pdf",
            "application/pdf",
            "contenido".getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/documentos/tramite/{tramiteId}", tramite.getIdTramite())
                .file(archivo)
                .param("tipoDocumento", "CEDULA_CIUDADANIA"))
                .andExpect(status().isUnauthorized());
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

        tramite = new Tramite();
        tramite.setNumeroRadicacion("TEST-001-2024");
        tramite.setObjetoTramite("Construcción de vivienda unifamiliar");
        tramite.setEstadoActual(Tramite.EstadoTramite.RADICADO);
        tramite.setFechaRadicacion(LocalDate.now());
        tramite.setFechaCreacion(LocalDateTime.now());
        tramite.setEntidad(entidad);
        tramite.setSolicitante(solicitante);
        tramite.setTipoTramite(tipoTramite);
        tramite = tramiteRepository.save(tramite);
    }

    private Documento crearDocumentoPrueba() {
        return crearDocumentoPrueba("documento_prueba.pdf", Documento.TipoDocumento.CEDULA_CIUDADANIA);
    }

    private Documento crearDocumentoPrueba(String nombreArchivo, Documento.TipoDocumento tipoDocumento) {
        Documento documento = new Documento();
        documento.setNombreOriginal(nombreArchivo);
        documento.setNombreArchivo(nombreArchivo.replace(".", "_" + System.currentTimeMillis() + "."));
        documento.setTipoMime("application/pdf");
        documento.setExtension("pdf");
        documento.setTamanoBytes(1024L);
        documento.setRutaArchivo("test/path/" + documento.getNombreArchivo());
        documento.setHashArchivo("test_hash_" + System.currentTimeMillis());
        documento.setTipoDocumento(tipoDocumento);
        documento.setVersion(1);
        documento.setEsVersionActual(true);
        documento.setEstado(Documento.EstadoDocumento.ACTIVO);
        documento.setTramite(tramite);
        documento.setUsuarioSubida(solicitante);
        documento.setEntidad(entidad);

        return documentoRepository.save(documento);
    }
}