package com.gestion.tramites.service;

import com.gestion.tramites.dto.reportes.DashboardMetricasDTO;
import com.gestion.tramites.model.*;
import com.gestion.tramites.repository.TramiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private TramiteRepository tramiteRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ReporteService reporteService;

    private CustomUserDetails mockUser;
    private List<Tramite> tramitesDePrueba;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);

        mockUser = mock(CustomUserDetails.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockUser);

        crearTramitesDePrueba();
    }

    @Test
    void generarDashboardMetricas_AdminGlobal_DebeIncluirTodosTramites() {
        when(mockUser.isAdminGlobal()).thenReturn(true);
        when(tramiteRepository.findAll()).thenReturn(tramitesDePrueba);

        DashboardMetricasDTO resultado = reporteService.generarDashboardMetricas();

        assertNotNull(resultado);
        assertEquals(3L, resultado.getTotalTramites());
        assertEquals(2L, resultado.getTramitesUltimoMes());
        assertEquals(1L, resultado.getTramitesHoy());
        assertNotNull(resultado.getDistribucionPorEstado());
        assertEquals(2L, resultado.getDistribucionPorEstado().get("RADICADO"));
        assertEquals(1L, resultado.getDistribucionPorEstado().get("APROBADO"));
        assertNull(resultado.getNombreEntidad());
    }

    @Test
    void generarDashboardMetricas_AdminEntidad_DebeFiltrarPorEntidad() {
        when(mockUser.isAdminGlobal()).thenReturn(false);
        when(mockUser.tieneEntidad()).thenReturn(true);
        when(mockUser.getNombreEntidad()).thenReturn("Alcaldía Test");
        when(tramiteRepository.findAll()).thenReturn(tramitesDePrueba);

        DashboardMetricasDTO resultado = reporteService.generarDashboardMetricas();

        assertNotNull(resultado);
        assertEquals("Alcaldía Test", resultado.getNombreEntidad());
        assertEquals(3L, resultado.getTotalTramites());
    }

    @Test
    void generarDashboardMetricas_DebeCalcularTiempoPromedioProcesamiento() {
        when(mockUser.isAdminGlobal()).thenReturn(true);
        when(tramiteRepository.findAll()).thenReturn(tramitesDePrueba);

        DashboardMetricasDTO resultado = reporteService.generarDashboardMetricas();

        assertNotNull(resultado.getTiempoPromedioProcesamientoDias());
        assertEquals(10.0, resultado.getTiempoPromedioProcesamientoDias());
    }

    @Test
    void generarDashboardMetricas_DebeCalcularCargaRevisores() {
        when(mockUser.isAdminGlobal()).thenReturn(true);
        when(tramiteRepository.findAll()).thenReturn(tramitesDePrueba);

        DashboardMetricasDTO resultado = reporteService.generarDashboardMetricas();

        assertNotNull(resultado.getCargaPorRevisor());
        assertEquals(1L, resultado.getCargaPorRevisor().get("Revisor Test"));
    }

    @Test
    void generarDashboardMetricas_DebeCalcularTramitesPorTipo() {
        when(mockUser.isAdminGlobal()).thenReturn(true);
        when(tramiteRepository.findAll()).thenReturn(tramitesDePrueba);

        DashboardMetricasDTO resultado = reporteService.generarDashboardMetricas();

        assertNotNull(resultado.getTramitesPorTipo());
        assertEquals(3L, resultado.getTramitesPorTipo().get("Licencia de Construcción"));
    }

    @Test
    void generarDashboardMetricas_DebeCalcularTasaAprobacion() {
        when(mockUser.isAdminGlobal()).thenReturn(true);
        when(tramiteRepository.findAll()).thenReturn(tramitesDePrueba);

        DashboardMetricasDTO resultado = reporteService.generarDashboardMetricas();

        assertNotNull(resultado.getTasaAprobacionUltimoMes());
        assertEquals(100.0, resultado.getTasaAprobacionUltimoMes());
    }

    @Test
    void generarDashboardMetricas_DebeCalcularTramitesVencidos() {
        when(mockUser.isAdminGlobal()).thenReturn(true);
        when(tramiteRepository.findAll()).thenReturn(tramitesDePrueba);

        DashboardMetricasDTO resultado = reporteService.generarDashboardMetricas();

        assertNotNull(resultado.getTramitesVencidos());
        assertEquals(1L, resultado.getTramitesVencidos());
    }

    private void crearTramitesDePrueba() {
        Entidad entidad = new Entidad();
        entidad.setId(1L);
        entidad.setNombre("Entidad Test");

        TipoTramite tipoTramite = new TipoTramite();
        tipoTramite.setIdTipoTramite(1L);
        tipoTramite.setNombre("Licencia de Construcción");

        Usuario solicitante = new Usuario();
        solicitante.setId(1L);
        solicitante.setNombreCompleto("Solicitante Test");

        Usuario revisor = new Usuario();
        revisor.setId(2L);
        revisor.setNombreCompleto("Revisor Test");

        Tramite tramite1 = new Tramite();
        tramite1.setIdTramite(1L);
        tramite1.setNumeroRadicacion("TEST-001");
        tramite1.setEntidad(entidad);
        tramite1.setSolicitante(solicitante);
        tramite1.setTipoTramite(tipoTramite);
        tramite1.setEstadoActual(Tramite.EstadoTramite.RADICADO);
        tramite1.setFechaRadicacion(LocalDate.now());
        tramite1.setFechaCreacion(LocalDateTime.now().minusDays(5));
        tramite1.setRevisorAsignado(revisor);
        tramite1.setFechaLimiteCompletar(LocalDate.now().minusDays(1));

        Tramite tramite2 = new Tramite();
        tramite2.setIdTramite(2L);
        tramite2.setNumeroRadicacion("TEST-002");
        tramite2.setEntidad(entidad);
        tramite2.setSolicitante(solicitante);
        tramite2.setTipoTramite(tipoTramite);
        tramite2.setEstadoActual(Tramite.EstadoTramite.RADICADO);
        tramite2.setFechaRadicacion(LocalDate.now().minusDays(10));
        tramite2.setFechaCreacion(LocalDateTime.now().minusDays(15));

        Tramite tramite3 = new Tramite();
        tramite3.setIdTramite(3L);
        tramite3.setNumeroRadicacion("TEST-003");
        tramite3.setEntidad(entidad);
        tramite3.setSolicitante(solicitante);
        tramite3.setTipoTramite(tipoTramite);
        tramite3.setEstadoActual(Tramite.EstadoTramite.APROBADO);
        tramite3.setFechaRadicacion(LocalDate.now().minusDays(20));
        tramite3.setFechaCreacion(LocalDateTime.now().minusDays(25));
        tramite3.setFechaFinalizacion(LocalDateTime.now().minusDays(15));

        tramitesDePrueba = Arrays.asList(tramite1, tramite2, tramite3);
    }
}