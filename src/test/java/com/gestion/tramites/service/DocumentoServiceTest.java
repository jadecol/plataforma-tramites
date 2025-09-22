package com.gestion.tramites.service;

import com.gestion.tramites.exception.ResourceNotFoundException;
import com.gestion.tramites.model.*;
import com.gestion.tramites.repository.DocumentoRepository;
import com.gestion.tramites.repository.TramiteRepository;
import com.gestion.tramites.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentoServiceTest {

    @Mock
    private DocumentoRepository documentoRepository;

    @Mock
    private TramiteRepository tramiteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private DocumentoStorageService storageService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DocumentoService documentoService;

    private CustomUserDetails mockUser;
    private Tramite tramitePrueba;
    private Usuario usuarioPrueba;
    private Entidad entidadPrueba;
    private MockMultipartFile archivoPrueba;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);

        crearDatosPrueba();
        configurarMocks();
    }

    @Test
    void subirDocumento_ArchivoValido_DebeCrearDocumento() throws IOException {
        // Arrange
        when(tramiteRepository.findById(anyLong())).thenReturn(Optional.of(tramitePrueba));
        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.of(usuarioPrueba));
        when(documentoRepository.countByTramiteIdAndEstadoActivo(anyLong())).thenReturn(5L);
        when(documentoRepository.sumTamanoByTramiteIdAndEstadoActivo(anyLong())).thenReturn(1000000L);
        when(documentoRepository.findByHashArchivoAndEstadoActivo(any())).thenReturn(Arrays.asList());
        when(storageService.calcularHashArchivo(any())).thenReturn("abcd1234hash");
        when(storageService.almacenarArchivo(any(), anyLong())).thenReturn("2024/01/tramite_1/test_file.pdf");
        when(storageService.determinarTipoMime(any())).thenReturn("application/pdf");

        Documento documentoGuardado = new Documento();
        documentoGuardado.setIdDocumento(1L);
        documentoGuardado.setNombreOriginal("test_file.pdf");
        when(documentoRepository.save(any(Documento.class))).thenReturn(documentoGuardado);

        // Act
        Documento resultado = documentoService.subirDocumento(
            1L, archivoPrueba, Documento.TipoDocumento.CEDULA_CIUDADANIA, "Descripción test");

        // Assert
        assertNotNull(resultado);
        assertEquals("test_file.pdf", resultado.getNombreOriginal());
        verify(storageService).validarArchivo(archivoPrueba);
        verify(storageService).validarLimitesPorTramite(5, 1000000L, archivoPrueba.getSize());
        verify(documentoRepository).save(any(Documento.class));
    }

    @Test
    void subirDocumento_TramiteNoEncontrado_DebeLanzarExcepcion() throws IOException {
        // Arrange
        when(tramiteRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            documentoService.subirDocumento(
                999L, archivoPrueba, Documento.TipoDocumento.CEDULA_CIUDADANIA, "Descripción");
        });

        verify(storageService, never()).almacenarArchivo(any(), anyLong());
        verify(documentoRepository, never()).save(any());
    }

    @Test
    void subirDocumento_ArchivoDuplicado_DebeLanzarExcepcion() throws IOException {
        // Arrange
        when(tramiteRepository.findById(anyLong())).thenReturn(Optional.of(tramitePrueba));
        when(storageService.calcularHashArchivo(any())).thenReturn("duplicated_hash");

        Documento documentoExistente = new Documento();
        documentoExistente.setTramite(tramitePrueba);
        documentoExistente.setNombreOriginal("archivo_existente.pdf");
        when(documentoRepository.findByHashArchivoAndEstadoActivo("duplicated_hash"))
                .thenReturn(Arrays.asList(documentoExistente));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            documentoService.subirDocumento(
                1L, archivoPrueba, Documento.TipoDocumento.CEDULA_CIUDADANIA, "Descripción");
        });

        assertTrue(exception.getMessage().contains("Ya existe un archivo idéntico"));
        verify(storageService, never()).almacenarArchivo(any(), anyLong());
    }

    @Test
    void descargarDocumento_DocumentoExistente_DebeRetornarResource() {
        // Arrange
        Documento documento = crearDocumentoPrueba();
        when(documentoRepository.findById(1L)).thenReturn(Optional.of(documento));
        when(tramiteRepository.findById(anyLong())).thenReturn(Optional.of(tramitePrueba));

        Resource mockResource = mock(Resource.class);
        when(storageService.cargarArchivo(any())).thenReturn(mockResource);

        // Act
        Resource resultado = documentoService.descargarDocumento(1L);

        // Assert
        assertNotNull(resultado);
        verify(storageService).cargarArchivo(documento.getRutaArchivo());
        verify(documentoRepository).save(any(Documento.class)); // Para actualizar fecha último acceso
    }

    @Test
    void descargarDocumento_DocumentoNoActivo_DebeLanzarExcepcion() {
        // Arrange
        Documento documento = crearDocumentoPrueba();
        documento.setEstado(Documento.EstadoDocumento.ELIMINADO);
        when(documentoRepository.findById(1L)).thenReturn(Optional.of(documento));
        when(tramiteRepository.findById(anyLong())).thenReturn(Optional.of(tramitePrueba));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            documentoService.descargarDocumento(1L);
        });

        assertTrue(exception.getMessage().contains("no está disponible para descarga"));
        verify(storageService, never()).cargarArchivo(any());
    }

    @Test
    void obtenerDocumentosPorTramite_TramiteValido_DebeRetornarLista() {
        // Arrange
        when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramitePrueba));

        Documento doc1 = crearDocumentoPrueba();
        Documento doc2 = crearDocumentoPrueba();
        doc2.setIdDocumento(2L);
        doc2.setNombreOriginal("segundo_documento.pdf");

        when(documentoRepository.findByTramiteIdAndEstadoActivo(1L))
                .thenReturn(Arrays.asList(doc1, doc2));

        // Act
        List<Documento> resultado = documentoService.obtenerDocumentosPorTramite(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(documentoRepository).findByTramiteIdAndEstadoActivo(1L);
    }

    @Test
    void eliminarDocumento_UsuarioAutorizado_DebeMarcarComoEliminado() {
        // Arrange
        Documento documento = crearDocumentoPrueba();
        documento.setUsuarioSubida(usuarioPrueba); // El usuario actual es quien subió el documento

        when(documentoRepository.findById(1L)).thenReturn(Optional.of(documento));
        when(tramiteRepository.findById(anyLong())).thenReturn(Optional.of(tramitePrueba));

        // Act
        documentoService.eliminarDocumento(1L);

        // Assert
        verify(documentoRepository).save(argThat(doc ->
            doc.getEstado() == Documento.EstadoDocumento.ELIMINADO &&
            Boolean.FALSE.equals(doc.getEsVersionActual())
        ));
    }

    @Test
    void eliminarDocumento_UsuarioNoAutorizado_DebeLanzarExcepcion() {
        // Arrange
        Documento documento = crearDocumentoPrueba();
        Usuario otroUsuario = new Usuario();
        otroUsuario.setId(999L);
        documento.setUsuarioSubida(otroUsuario); // Otro usuario subió el documento

        when(mockUser.isAdminGlobal()).thenReturn(false);
        when(mockUser.isAdminEntidad()).thenReturn(false);
        when(mockUser.isRevisor()).thenReturn(false);

        when(documentoRepository.findById(1L)).thenReturn(Optional.of(documento));
        when(tramiteRepository.findById(anyLong())).thenReturn(Optional.of(tramitePrueba));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            documentoService.eliminarDocumento(1L);
        });

        assertTrue(exception.getMessage().contains("No tiene permisos para eliminar"));
        verify(documentoRepository, never()).save(any());
    }

    @Test
    void buscarDocumentosPorNombre_ConResultados_DebeRetornarDocumentos() {
        // Arrange
        when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramitePrueba));

        Documento documento = crearDocumentoPrueba();
        when(documentoRepository.findByTramiteIdAndNombreContainingIgnoreCaseAndEstadoActivo(1L, "cedula"))
                .thenReturn(Arrays.asList(documento));

        // Act
        List<Documento> resultado = documentoService.buscarDocumentosPorNombre(1L, "cedula");

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("test_file.pdf", resultado.get(0).getNombreOriginal());
    }

    @Test
    void obtenerEstadisticas_TramiteValido_DebeRetornarEstadisticas() {
        // Arrange
        when(tramiteRepository.findById(1L)).thenReturn(Optional.of(tramitePrueba));
        when(documentoRepository.countByTramiteIdAndEstadoActivo(1L)).thenReturn(5L);
        when(documentoRepository.sumTamanoByTramiteIdAndEstadoActivo(1L)).thenReturn(10485760L); // 10MB

        // Act
        DocumentoService.EstadisticasDocumentos resultado = documentoService.obtenerEstadisticas(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(5L, resultado.getCantidadDocumentos());
        assertEquals(10485760L, resultado.getTamanoTotal());
        assertEquals("10.0 MB", resultado.getTamanoTotalLegible());
    }

    private void crearDatosPrueba() {
        entidadPrueba = new Entidad();
        entidadPrueba.setId(1L);
        entidadPrueba.setNombre("Entidad Test");

        usuarioPrueba = new Usuario();
        usuarioPrueba.setId(1L);
        usuarioPrueba.setNombreCompleto("Usuario Test");
        usuarioPrueba.setCorreoElectronico("test@test.com");
        usuarioPrueba.setEntidad(entidadPrueba);

        tramitePrueba = new Tramite();
        tramitePrueba.setIdTramite(1L);
        tramitePrueba.setNumeroRadicacion("TEST-001");
        tramitePrueba.setEntidad(entidadPrueba);
        tramitePrueba.setSolicitante(usuarioPrueba);

        archivoPrueba = new MockMultipartFile(
            "archivo",
            "test_file.pdf",
            "application/pdf",
            "Contenido del archivo de prueba".getBytes()
        );

        mockUser = mock(CustomUserDetails.class);
        when(mockUser.getIdUsuario()).thenReturn(1L);
        when(mockUser.getUsername()).thenReturn("test@test.com");
        when(mockUser.isAdminGlobal()).thenReturn(false);
        when(mockUser.isAdminEntidad()).thenReturn(false);
        when(mockUser.isRevisor()).thenReturn(false);
    }

    private void configurarMocks() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockUser);

        doNothing().when(storageService).validarArchivo(any());
        doNothing().when(storageService).validarLimitesPorTramite(anyInt(), anyLong(), anyLong());
    }

    private Documento crearDocumentoPrueba() {
        Documento documento = new Documento();
        documento.setIdDocumento(1L);
        documento.setNombreOriginal("test_file.pdf");
        documento.setNombreArchivo("test_file_123456789.pdf");
        documento.setTipoMime("application/pdf");
        documento.setExtension("pdf");
        documento.setTamanoBytes(1024L);
        documento.setRutaArchivo("2024/01/tramite_1/test_file_123456789.pdf");
        documento.setHashArchivo("abcd1234hash");
        documento.setTipoDocumento(Documento.TipoDocumento.CEDULA_CIUDADANIA);
        documento.setVersion(1);
        documento.setEsVersionActual(true);
        documento.setEstado(Documento.EstadoDocumento.ACTIVO);
        documento.setFechaSubida(LocalDateTime.now());
        documento.setTramite(tramitePrueba);
        documento.setUsuarioSubida(usuarioPrueba);
        documento.setEntidad(entidadPrueba);

        return documento;
    }
}