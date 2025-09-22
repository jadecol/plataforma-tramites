package com.gestion.tramites.service;

import com.gestion.tramites.model.Entidad;
import com.gestion.tramites.model.TipoTramite;
import com.gestion.tramites.repository.EntidadRepository;
import com.gestion.tramites.repository.ConsecutivoRadicacionRepository;
import com.gestion.tramites.repository.TramiteRepository;
import com.gestion.tramites.repository.TipoTramiteRepository;
import com.gestion.tramites.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RadicacionServiceTest {

    @Mock
    private EntidadRepository entidadRepository;

    @Mock
    private ConsecutivoRadicacionRepository consecutivoRepository;

    @Mock
    private TramiteRepository tramiteRepository;

    @Mock
    private TipoTramiteRepository tipoTramiteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ValidacionRadicacionService validacionService;

    @Mock
    private NotificacionService notificacionService;

    @InjectMocks
    private RadicacionService radicacionService;

    private Entidad testEntidad;
    private TipoTramite testTipoTramite;

    @BeforeEach
    void setUp() {
        testEntidad = new Entidad();
        testEntidad.setId(123L);
        testEntidad.setNombre("Test Entidad");

        testTipoTramite = new TipoTramite();
        testTipoTramite.setIdTipoTramite(1L);
        testTipoTramite.setNombre("Licencia de Construcción");

        // Configurar mocks por defecto
        when(entidadRepository.findById(anyLong())).thenReturn(Optional.of(testEntidad));
        when(consecutivoRepository.obtenerSiguienteConsecutivo(anyLong(), anyInt())).thenReturn(Optional.of(1));
        when(validacionService.validarFormato(anyString())).thenReturn(new ValidacionRadicacionService.ResultadoValidacionRadicacion(true, "", ValidacionRadicacionService.TipoValidacionRadicacion.FORMATO));
    }

    @Test
    void generarNumeroRadicacion_ValidInputs_ReturnsCorrectFormat() {
        // Act
        String numeroRadicacion = radicacionService.generarNumeroRadicacion(testEntidad, testTipoTramite);

        // Assert
        assertNotNull(numeroRadicacion);

        String currentYear = String.valueOf(LocalDate.now().getYear());
        String expectedPattern = "11001-123-" + currentYear + "-00001";
        assertEquals(expectedPattern, numeroRadicacion);

        // Verificar que sigue el patrón esperado
        assertTrue(numeroRadicacion.matches("^\\d{5}-\\d+-\\d{4}-\\d{5}$"));
    }

    @Test
    void generarNumeroRadicacion_DifferentEntidadId_ReturnsCorrectId() {
        // Arrange
        testEntidad.setId(999L);

        // Act
        String numeroRadicacion = radicacionService.generarNumeroRadicacion(testEntidad, testTipoTramite);

        // Assert
        assertNotNull(numeroRadicacion);
        assertTrue(numeroRadicacion.contains("-999-"));

        String currentYear = String.valueOf(LocalDate.now().getYear());
        String expectedPattern = "11001-999-" + currentYear + "-00001";
        assertEquals(expectedPattern, numeroRadicacion);
    }

    @Test
    void generarNumeroRadicacion_LargeEntidadId_HandlesCorrectly() {
        // Arrange
        testEntidad.setId(12345678L);

        // Act
        String numeroRadicacion = radicacionService.generarNumeroRadicacion(testEntidad, testTipoTramite);

        // Assert
        assertNotNull(numeroRadicacion);
        assertTrue(numeroRadicacion.contains("-12345678-"));

        String currentYear = String.valueOf(LocalDate.now().getYear());
        String expectedPattern = "11001-12345678-" + currentYear + "-00001";
        assertEquals(expectedPattern, numeroRadicacion);
    }

    @Test
    void validarNumeroRadicacion_ValidFormat_ReturnsTrue() {
        // Arrange
        String numeroValido = "11001-123-2024-00001";

        // Act
        boolean resultado = radicacionService.validarNumeroRadicacion(numeroValido);

        // Assert
        assertTrue(resultado);
    }

    @Test
    void validarNumeroRadicacion_ValidFormatWithLargeNumbers_ReturnsTrue() {
        // Arrange
        String numeroValido = "12345-999999-2024-99999";

        // Act
        boolean resultado = radicacionService.validarNumeroRadicacion(numeroValido);

        // Assert
        assertTrue(resultado);
    }

    @Test
    void validarNumeroRadicacion_NullInput_ReturnsFalse() {
        // Act
        boolean resultado = radicacionService.validarNumeroRadicacion(null);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void validarNumeroRadicacion_EmptyString_ReturnsFalse() {
        // Act
        boolean resultado = radicacionService.validarNumeroRadicacion("");

        // Assert
        assertFalse(resultado);
    }

    @Test
    void validarNumeroRadicacion_WhitespaceOnly_ReturnsFalse() {
        // Act
        boolean resultado = radicacionService.validarNumeroRadicacion("   ");

        // Assert
        assertFalse(resultado);
    }

    @Test
    void validarNumeroRadicacion_InvalidFormat_MissingDashes_ReturnsFalse() {
        // Arrange
        String numeroInvalido = "1100112320240001";

        // Act
        boolean resultado = radicacionService.validarNumeroRadicacion(numeroInvalido);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void validarNumeroRadicacion_InvalidFormat_IncorrectLength_ReturnsFalse() {
        // Arrange
        String numeroInvalido = "110-123-2024-00001"; // Código DANE muy corto

        // Act
        boolean resultado = radicacionService.validarNumeroRadicacion(numeroInvalido);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void validarNumeroRadicacion_InvalidFormat_NonNumericCharacters_ReturnsFalse() {
        // Arrange
        String numeroInvalido = "11ABC-123-2024-00001";

        // Act
        boolean resultado = radicacionService.validarNumeroRadicacion(numeroInvalido);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void validarNumeroRadicacion_InvalidFormat_IncorrectYearLength_ReturnsFalse() {
        // Arrange
        String numeroInvalido = "11001-123-24-00001"; // Año de 2 dígitos

        // Act
        boolean resultado = radicacionService.validarNumeroRadicacion(numeroInvalido);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void validarNumeroRadicacion_InvalidFormat_IncorrectConsecutiveLength_ReturnsFalse() {
        // Arrange
        String numeroInvalido = "11001-123-2024-001"; // Consecutivo de 3 dígitos

        // Act
        boolean resultado = radicacionService.validarNumeroRadicacion(numeroInvalido);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void validarNumeroRadicacion_InvalidFormat_ExtraSegments_ReturnsFalse() {
        // Arrange
        String numeroInvalido = "11001-123-2024-00001-extra";

        // Act
        boolean resultado = radicacionService.validarNumeroRadicacion(numeroInvalido);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void generarNumeroRadicacion_ConsistentFormat_AlwaysUsesBogotaCode() {
        // Act
        String numeroRadicacion1 = radicacionService.generarNumeroRadicacion(testEntidad, testTipoTramite);

        testEntidad.setId(456L);
        String numeroRadicacion2 = radicacionService.generarNumeroRadicacion(testEntidad, testTipoTramite);

        // Assert
        assertTrue(numeroRadicacion1.startsWith("11001-"));
        assertTrue(numeroRadicacion2.startsWith("11001-"));
    }

    @Test
    void generarNumeroRadicacion_UsesCurrentYear() {
        // Act
        String numeroRadicacion = radicacionService.generarNumeroRadicacion(testEntidad, testTipoTramite);

        // Assert
        String currentYear = String.valueOf(LocalDate.now().getYear());
        assertTrue(numeroRadicacion.contains("-" + currentYear + "-"));
    }
}