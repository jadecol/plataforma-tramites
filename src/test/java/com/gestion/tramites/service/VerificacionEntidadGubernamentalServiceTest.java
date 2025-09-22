package com.gestion.tramites.service;

import com.gestion.tramites.exception.ResourceNotFoundException;
import com.gestion.tramites.model.EntidadGubernamental;
import com.gestion.tramites.repository.EntidadGubernamentalRepository;
import com.gestion.tramites.service.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificacionEntidadGubernamentalServiceTest {

    @Mock
    private EntidadGubernamentalRepository entidadGubernamentalRepository;

    @Mock
    private ValidacionDominiosGubernamentalesService validacionDominiosService;

    @Mock
    private NotificacionService notificacionService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private VerificacionEntidadGubernamentalService verificacionService;

    private CustomUserDetails mockAdminGlobal;
    private CustomUserDetails mockUsuarioNormal;
    private EntidadGubernamental entidadPrueba;
    private VerificacionEntidadGubernamentalService.SolicitudRegistroEntidad solicitudRegistro;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        crearDatosPrueba();
        configurarMocks();
    }

    @Test
    void registrarEntidadGubernamental_DominioValido_DebeCrearEntidad() {
        // Arrange
        when(validacionDominiosService.validarDominioGubernamental(any(), any()))
                .thenReturn(new ValidacionDominiosGubernamentalesService.ResultadoValidacionDominio(
                        true, "Dominio válido", ValidacionDominiosGubernamentalesService.TipoValidacion.LISTA_BLANCA));

        when(entidadGubernamentalRepository.existsByDominioOficial(any())).thenReturn(false);
        when(entidadGubernamentalRepository.existsByCodigoDane(any())).thenReturn(false);
        when(entidadGubernamentalRepository.existsByNit(any())).thenReturn(false);
        when(entidadGubernamentalRepository.existsByEmailOficial(any())).thenReturn(false);

        EntidadGubernamental entidadGuardada = new EntidadGubernamental();
        entidadGuardada.setIdEntidadGubernamental(1L);
        entidadGuardada.setNombre(solicitudRegistro.getNombre());
        when(entidadGubernamentalRepository.save(any())).thenReturn(entidadGuardada);

        // Act
        EntidadGubernamental resultado = verificacionService.registrarEntidadGubernamental(solicitudRegistro);

        // Assert
        assertNotNull(resultado);
        assertEquals(solicitudRegistro.getNombre(), resultado.getNombre());
        verify(validacionDominiosService).validarDominioGubernamental(any(), any());
        verify(entidadGubernamentalRepository).save(any());
    }

    @Test
    void registrarEntidadGubernamental_DominioInvalido_DebeLanzarExcepcion() {
        // Arrange
        when(validacionDominiosService.validarDominioGubernamental(any(), any()))
                .thenReturn(new ValidacionDominiosGubernamentalesService.ResultadoValidacionDominio(
                        false, "Dominio no válido", ValidacionDominiosGubernamentalesService.TipoValidacion.PATRON_INVALIDO));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            verificacionService.registrarEntidadGubernamental(solicitudRegistro);
        });

        assertTrue(exception.getMessage().contains("Dominio no válido"));
        verify(entidadGubernamentalRepository, never()).save(any());
    }

    @Test
    void registrarEntidadGubernamental_DominioDuplicado_DebeLanzarExcepcion() {
        // Arrange
        when(validacionDominiosService.validarDominioGubernamental(any(), any()))
                .thenReturn(new ValidacionDominiosGubernamentalesService.ResultadoValidacionDominio(
                        true, "Dominio válido", ValidacionDominiosGubernamentalesService.TipoValidacion.LISTA_BLANCA));

        when(entidadGubernamentalRepository.existsByDominioOficial(any())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            verificacionService.registrarEntidadGubernamental(solicitudRegistro);
        });

        assertTrue(exception.getMessage().contains("dominio oficial ya está registrado"));
        verify(entidadGubernamentalRepository, never()).save(any());
    }

    @Test
    void verificarEntidad_AdminGlobal_DebePermitirVerificacion() {
        // Arrange
        configurarUsuarioAdminGlobal();

        when(entidadGubernamentalRepository.findById(1L)).thenReturn(Optional.of(entidadPrueba));
        when(validacionDominiosService.validarDominioGubernamental(any(), any()))
                .thenReturn(new ValidacionDominiosGubernamentalesService.ResultadoValidacionDominio(
                        true, "Dominio válido", ValidacionDominiosGubernamentalesService.TipoValidacion.LISTA_BLANCA));

        EntidadGubernamental entidadActualizada = new EntidadGubernamental();
        entidadActualizada.setEstadoVerificacion(EntidadGubernamental.EstadoVerificacion.VERIFICADA);
        when(entidadGubernamentalRepository.save(any())).thenReturn(entidadActualizada);

        VerificacionEntidadGubernamentalService.SolicitudVerificacion solicitud =
                new VerificacionEntidadGubernamentalService.SolicitudVerificacion(true, "Verificación aprobada");

        // Act
        EntidadGubernamental resultado = verificacionService.verificarEntidad(1L, solicitud);

        // Assert
        assertNotNull(resultado);
        assertEquals(EntidadGubernamental.EstadoVerificacion.VERIFICADA, resultado.getEstadoVerificacion());
        verify(entidadGubernamentalRepository).save(any());
    }

    @Test
    void verificarEntidad_UsuarioNormal_DebeLanzarExcepcion() {
        // Arrange
        configurarUsuarioNormal();

        VerificacionEntidadGubernamentalService.SolicitudVerificacion solicitud =
                new VerificacionEntidadGubernamentalService.SolicitudVerificacion(true, "Verificación aprobada");

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            verificacionService.verificarEntidad(1L, solicitud);
        });

        assertTrue(exception.getMessage().contains("Solo administradores globales"));
        verify(entidadGubernamentalRepository, never()).save(any());
    }

    @Test
    void verificarEntidad_EntidadYaVerificada_DebeLanzarExcepcion() {
        // Arrange
        configurarUsuarioAdminGlobal();

        entidadPrueba.setEstadoVerificacion(EntidadGubernamental.EstadoVerificacion.VERIFICADA);
        when(entidadGubernamentalRepository.findById(1L)).thenReturn(Optional.of(entidadPrueba));

        VerificacionEntidadGubernamentalService.SolicitudVerificacion solicitud =
                new VerificacionEntidadGubernamentalService.SolicitudVerificacion(true, "Verificación aprobada");

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            verificacionService.verificarEntidad(1L, solicitud);
        });

        assertTrue(exception.getMessage().contains("ya está verificada"));
        verify(entidadGubernamentalRepository, never()).save(any());
    }

    @Test
    void suspenderEntidad_AdminGlobal_DebePermitirSuspension() {
        // Arrange
        configurarUsuarioAdminGlobal();

        entidadPrueba.setEstadoVerificacion(EntidadGubernamental.EstadoVerificacion.VERIFICADA);
        when(entidadGubernamentalRepository.findById(1L)).thenReturn(Optional.of(entidadPrueba));

        EntidadGubernamental entidadSuspendida = new EntidadGubernamental();
        entidadSuspendida.setEstadoVerificacion(EntidadGubernamental.EstadoVerificacion.SUSPENDIDA);
        when(entidadGubernamentalRepository.save(any())).thenReturn(entidadSuspendida);

        // Act
        EntidadGubernamental resultado = verificacionService.suspenderEntidad(1L, "Motivo de prueba");

        // Assert
        assertNotNull(resultado);
        assertEquals(EntidadGubernamental.EstadoVerificacion.SUSPENDIDA, resultado.getEstadoVerificacion());
        verify(entidadGubernamentalRepository).save(any());
    }

    @Test
    void suspenderEntidad_EntidadNoVerificada_DebeLanzarExcepcion() {
        // Arrange
        configurarUsuarioAdminGlobal();

        entidadPrueba.setEstadoVerificacion(EntidadGubernamental.EstadoVerificacion.PENDIENTE);
        when(entidadGubernamentalRepository.findById(1L)).thenReturn(Optional.of(entidadPrueba));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            verificacionService.suspenderEntidad(1L, "Motivo de prueba");
        });

        assertTrue(exception.getMessage().contains("Solo se pueden suspender entidades verificadas"));
        verify(entidadGubernamentalRepository, never()).save(any());
    }

    @Test
    void obtenerEntidadesPendientesVerificacion_DebeRetornarLista() {
        // Arrange
        List<EntidadGubernamental> entidadesPendientes = Arrays.asList(entidadPrueba);
        when(entidadGubernamentalRepository.findByEstadoVerificacion(
                EntidadGubernamental.EstadoVerificacion.PENDIENTE)).thenReturn(entidadesPendientes);

        // Act
        List<EntidadGubernamental> resultado = verificacionService.obtenerEntidadesPendientesVerificacion();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(entidadGubernamentalRepository).findByEstadoVerificacion(
                EntidadGubernamental.EstadoVerificacion.PENDIENTE);
    }

    @Test
    void validarEntidadParaOperacionCritica_EntidadActiva_DebePermitir() {
        // Arrange
        entidadPrueba.setEstadoVerificacion(EntidadGubernamental.EstadoVerificacion.VERIFICADA);
        entidadPrueba.setActivo(true);
        when(entidadGubernamentalRepository.findById(1L)).thenReturn(Optional.of(entidadPrueba));

        // Act & Assert
        assertDoesNotThrow(() -> {
            verificacionService.validarEntidadParaOperacionCritica(1L);
        });
    }

    @Test
    void validarEntidadParaOperacionCritica_EntidadSuspendida_DebeLanzarExcepcion() {
        // Arrange
        entidadPrueba.setEstadoVerificacion(EntidadGubernamental.EstadoVerificacion.SUSPENDIDA);
        entidadPrueba.setActivo(false);
        when(entidadGubernamentalRepository.findById(1L)).thenReturn(Optional.of(entidadPrueba));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            verificacionService.validarEntidadParaOperacionCritica(1L);
        });

        assertTrue(exception.getMessage().contains("no está verificada o está suspendida"));
    }

    @Test
    void validarEntidadParaOperacionCritica_EntidadNoExistente_DebeLanzarExcepcion() {
        // Arrange
        when(entidadGubernamentalRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            verificacionService.validarEntidadParaOperacionCritica(999L);
        });
    }

    @Test
    void buscarPorDominioOficial_DominioExistente_DebeRetornarEntidad() {
        // Arrange
        when(entidadGubernamentalRepository.findByDominioOficial("www.bogota.gov.co"))
                .thenReturn(Optional.of(entidadPrueba));

        // Act
        Optional<EntidadGubernamental> resultado = verificacionService.buscarPorDominioOficial("www.bogota.gov.co");

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(entidadPrueba, resultado.get());
    }

    @Test
    void buscarPorCodigoDane_CodigoExistente_DebeRetornarEntidad() {
        // Arrange
        when(entidadGubernamentalRepository.findByCodigoDane("11001"))
                .thenReturn(Optional.of(entidadPrueba));

        // Act
        Optional<EntidadGubernamental> resultado = verificacionService.buscarPorCodigoDane("11001");

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(entidadPrueba, resultado.get());
    }

    private void crearDatosPrueba() {
        mockAdminGlobal = mock(CustomUserDetails.class);
        when(mockAdminGlobal.isAdminGlobal()).thenReturn(true);
        when(mockAdminGlobal.getUsername()).thenReturn("admin@test.com");

        mockUsuarioNormal = mock(CustomUserDetails.class);
        when(mockUsuarioNormal.isAdminGlobal()).thenReturn(false);
        when(mockUsuarioNormal.getUsername()).thenReturn("user@test.com");

        entidadPrueba = new EntidadGubernamental();
        entidadPrueba.setIdEntidadGubernamental(1L);
        entidadPrueba.setNombre("Alcaldía de Bogotá");
        entidadPrueba.setCodigoDane("11001");
        entidadPrueba.setNit("123456789-0");
        entidadPrueba.setTipoEntidad(EntidadGubernamental.TipoEntidadGubernamental.ALCALDIA);
        entidadPrueba.setDominioOficial("www.bogota.gov.co");
        entidadPrueba.setEmailOficial("contacto@bogota.gov.co");
        entidadPrueba.setEstadoVerificacion(EntidadGubernamental.EstadoVerificacion.PENDIENTE);
        entidadPrueba.setActivo(true);

        solicitudRegistro = new VerificacionEntidadGubernamentalService.SolicitudRegistroEntidad();
        solicitudRegistro.setNombre("Alcaldía de Bogotá");
        solicitudRegistro.setCodigoDane("11001");
        solicitudRegistro.setNit("123456789-0");
        solicitudRegistro.setTipoEntidad(EntidadGubernamental.TipoEntidadGubernamental.ALCALDIA);
        solicitudRegistro.setDominioOficial("www.bogota.gov.co");
        solicitudRegistro.setEmailOficial("contacto@bogota.gov.co");
        solicitudRegistro.setDepartamento("Cundinamarca");
        solicitudRegistro.setMunicipio("Bogotá");
    }

    private void configurarMocks() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    private void configurarUsuarioAdminGlobal() {
        when(authentication.getPrincipal()).thenReturn(mockAdminGlobal);
    }

    private void configurarUsuarioNormal() {
        when(authentication.getPrincipal()).thenReturn(mockUsuarioNormal);
    }
}