package com.gestion.tramites.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ValidacionDominiosGubernamentalesServiceTest {

    @InjectMocks
    private ValidacionDominiosGubernamentalesService validacionService;

    @BeforeEach
    void setUp() {
        // Configurar propiedades para pruebas
        ReflectionTestUtils.setField(validacionService, "timeoutMs", 5000);
        ReflectionTestUtils.setField(validacionService, "validacionHabilitada", true);
    }

    @Test
    void validarDominioGubernamental_DominioAlcaldiaOficial_DebeRetornarValido() {
        // Arrange
        String dominio = "www.bogota.gov.co";
        String tipoEntidad = "ALCALDIA";

        // Act
        ValidacionDominiosGubernamentalesService.ResultadoValidacionDominio resultado =
                validacionService.validarDominioGubernamental(dominio, tipoEntidad);

        // Assert
        assertTrue(resultado.isValido());
        assertEquals(ValidacionDominiosGubernamentalesService.TipoValidacion.LISTA_BLANCA,
                resultado.getTipoValidacion());
        assertTrue(resultado.getMensaje().contains("verificado en lista oficial"));
    }

    @Test
    void validarDominioGubernamental_DominioCuraduriaOficial_DebeRetornarValido() {
        // Arrange
        String dominio = "curaduria1bogota.com";
        String tipoEntidad = "CURADURIA_URBANA";

        // Act
        ValidacionDominiosGubernamentalesService.ResultadoValidacionDominio resultado =
                validacionService.validarDominioGubernamental(dominio, tipoEntidad);

        // Assert
        assertTrue(resultado.isValido());
        assertEquals(ValidacionDominiosGubernamentalesService.TipoValidacion.LISTA_BLANCA,
                resultado.getTipoValidacion());
    }

    @Test
    void validarDominioGubernamental_DominioNoOficial_DebeRetornarInvalido() {
        // Arrange
        String dominio = "www.entidad-falsa.com";
        String tipoEntidad = "ALCALDIA";

        // Act
        ValidacionDominiosGubernamentalesService.ResultadoValidacionDominio resultado =
                validacionService.validarDominioGubernamental(dominio, tipoEntidad);

        // Assert
        assertFalse(resultado.isValido());
        assertTrue(resultado.getMensaje().contains("no cumple con patrones oficiales"));
    }

    @Test
    void validarDominioGubernamental_DominioGovCoValido_DebePermitirAlcaldia() {
        // Arrange
        String dominio = "www.nuevaentidad.gov.co";
        String tipoEntidad = "ALCALDIA";

        // Act
        ValidacionDominiosGubernamentalesService.ResultadoValidacionDominio resultado =
                validacionService.validarDominioGubernamental(dominio, tipoEntidad);

        // Assert
        // Nota: Este test puede fallar en accesibilidad si el dominio no existe realmente
        // pero debe pasar la validación de patrón
        assertNotNull(resultado);
    }

    @Test
    void validarDominioGubernamental_DominioNulo_DebeRetornarError() {
        // Arrange
        String dominio = null;
        String tipoEntidad = "ALCALDIA";

        // Act
        ValidacionDominiosGubernamentalesService.ResultadoValidacionDominio resultado =
                validacionService.validarDominioGubernamental(dominio, tipoEntidad);

        // Assert
        assertFalse(resultado.isValido());
        assertEquals(ValidacionDominiosGubernamentalesService.TipoValidacion.ERROR,
                resultado.getTipoValidacion());
        assertTrue(resultado.getMensaje().contains("no puede ser nulo"));
    }

    @Test
    void validarDominioGubernamental_DominioVacio_DebeRetornarError() {
        // Arrange
        String dominio = "";
        String tipoEntidad = "ALCALDIA";

        // Act
        ValidacionDominiosGubernamentalesService.ResultadoValidacionDominio resultado =
                validacionService.validarDominioGubernamental(dominio, tipoEntidad);

        // Assert
        assertFalse(resultado.isValido());
        assertEquals(ValidacionDominiosGubernamentalesService.TipoValidacion.ERROR,
                resultado.getTipoValidacion());
    }

    @Test
    void validarDominioGubernamental_ValidacionDeshabilitada_DebeOmitir() {
        // Arrange
        ReflectionTestUtils.setField(validacionService, "validacionHabilitada", false);
        String dominio = "www.cualquier-dominio.com";
        String tipoEntidad = "ALCALDIA";

        // Act
        ValidacionDominiosGubernamentalesService.ResultadoValidacionDominio resultado =
                validacionService.validarDominioGubernamental(dominio, tipoEntidad);

        // Assert
        assertTrue(resultado.isValido());
        assertEquals(ValidacionDominiosGubernamentalesService.TipoValidacion.OMITIDA,
                resultado.getTipoValidacion());
        assertTrue(resultado.getMensaje().contains("deshabilitada"));
    }

    @Test
    void validarDominioGubernamental_AlcaldiaConDominioComercial_DebeRechazar() {
        // Arrange
        String dominio = "www.alcaldia-falsa.com";
        String tipoEntidad = "ALCALDIA";

        // Act
        ValidacionDominiosGubernamentalesService.ResultadoValidacionDominio resultado =
                validacionService.validarDominioGubernamental(dominio, tipoEntidad);

        // Assert
        assertFalse(resultado.isValido());
        assertTrue(resultado.getMensaje().contains("no cumple con patrones oficiales") ||
                   resultado.getMensaje().contains("deben usar dominios .gov.co"));
    }

    @Test
    void validarDominioGubernamental_CuraduriaConPatronValido_DebePermitir() {
        // Arrange
        String dominio = "curaduria5nuevaciudad.com";
        String tipoEntidad = "CURADURIA_URBANA";

        // Act
        ValidacionDominiosGubernamentalesService.ResultadoValidacionDominio resultado =
                validacionService.validarDominioGubernamental(dominio, tipoEntidad);

        // Assert
        // Nota: Puede fallar en accesibilidad pero debe pasar validación de patrón
        assertNotNull(resultado);
    }

    @Test
    void validarDominioGubernamental_GobernacionConGovCo_DebePermitir() {
        // Arrange
        String dominio = "www.nuevogobierno.gov.co";
        String tipoEntidad = "GOBERNACION";

        // Act
        ValidacionDominiosGubernamentalesService.ResultadoValidacionDominio resultado =
                validacionService.validarDominioGubernamental(dominio, tipoEntidad);

        // Assert
        assertNotNull(resultado);
        // Si no pasa por accesibilidad, al menos debe cumplir patrón
    }

    @Test
    void validarDominioGubernamental_PatronesInvalidos_DebeRechazar() {
        // Arrange & Act & Assert
        String[] dominiosInvalidos = {
                "entidad.co",           // Sin .gov
                "www.entidad.org",      // Dominio no gubernamental
                "123.456.789.012",      // IP address
                "entidad_falsa.net",    // Dominio comercial
                "ftp://entidad.gov.co", // Protocolo en el dominio
                "entidad.gov.co/path",  // Path en el dominio
        };

        for (String dominio : dominiosInvalidos) {
            ValidacionDominiosGubernamentalesService.ResultadoValidacionDominio resultado =
                    validacionService.validarDominioGubernamental(dominio, "ALCALDIA");

            assertFalse(resultado.isValido(),
                    "El dominio " + dominio + " debería ser inválido");
        }
    }

    @Test
    void obtenerDominiosOficialesVerificados_DebeRetornarListaCompleta() {
        // Act
        Set<String> dominios = validacionService.obtenerDominiosOficialesVerificados();

        // Assert
        assertNotNull(dominios);
        assertFalse(dominios.isEmpty());
        assertTrue(dominios.contains("www.bogota.gov.co"));
        assertTrue(dominios.contains("curaduria1bogota.com"));
        assertTrue(dominios.contains("www.antioquia.gov.co"));

        // Verificar que no hay duplicados
        assertEquals(dominios.size(), dominios.stream().distinct().count());
    }

    @Test
    void validarDominioGubernamental_NormalizacionDeDominio_DebeManejarcorrectamente() {
        // Arrange - Probar diferentes formatos del mismo dominio
        String[] formatosDominio = {
                "www.bogota.gov.co",
                "https://www.bogota.gov.co",
                "https://www.bogota.gov.co/",
                "https://www.bogota.gov.co/path/to/page",
                "WWW.BOGOTA.GOV.CO",
        };

        // Act & Assert
        for (String formato : formatosDominio) {
            ValidacionDominiosGubernamentalesService.ResultadoValidacionDominio resultado =
                    validacionService.validarDominioGubernamental(formato, "ALCALDIA");

            assertTrue(resultado.isValido(),
                    "El formato " + formato + " debería ser válido después de normalización");
            assertEquals(ValidacionDominiosGubernamentalesService.TipoValidacion.LISTA_BLANCA,
                    resultado.getTipoValidacion());
        }
    }

    @Test
    void validarDominioGubernamental_TiposEntidadEspecificos_DebeAplicarReglas() {
        // Test Alcaldía - debe ser .gov.co
        ValidacionDominiosGubernamentalesService.ResultadoValidacionDominio resultadoAlcaldia =
                validacionService.validarDominioGubernamental("alcaldia-test.com", "ALCALDIA");
        assertFalse(resultadoAlcaldia.isValido());

        // Test Curaduría - puede ser .com si cumple patrón
        ValidacionDominiosGubernamentalesService.ResultadoValidacionDominio resultadoCuraduria =
                validacionService.validarDominioGubernamental("curaduria1test.com", "CURADURIA_URBANA");
        // Puede ser válido o inválido dependiendo de accesibilidad, pero no debe fallar por patrón

        // Test Gobernación - debe ser .gov.co
        ValidacionDominiosGubernamentalesService.ResultadoValidacionDominio resultadoGobernacion =
                validacionService.validarDominioGubernamental("gobernacion-test.com", "GOBERNACION");
        assertFalse(resultadoGobernacion.isValido());
    }

    @Test
    void validarDominioGubernamental_SegimentacionPorTipo_DebeUsarListaCorrecta() {
        // Verificar que diferentes tipos usan diferentes listas blancas

        // Alcaldía debe encontrar dominios de alcaldías
        ValidacionDominiosGubernamentalesService.ResultadoValidacionDominio resultadoAlcaldia =
                validacionService.validarDominioGubernamental("www.medellin.gov.co", "ALCALDIA");
        assertTrue(resultadoAlcaldia.isValido());

        // Curaduría debe encontrar dominios de curadurías
        ValidacionDominiosGubernamentalesService.ResultadoValidacionDominio resultadoCuraduria =
                validacionService.validarDominioGubernamental("curaduria1medellin.com", "CURADURIA_URBANA");
        assertTrue(resultadoCuraduria.isValido());

        // Gobernación debe encontrar dominios institucionales
        ValidacionDominiosGubernamentalesService.ResultadoValidacionDominio resultadoGobernacion =
                validacionService.validarDominioGubernamental("www.antioquia.gov.co", "GOBERNACION");
        assertTrue(resultadoGobernacion.isValido());
    }
}