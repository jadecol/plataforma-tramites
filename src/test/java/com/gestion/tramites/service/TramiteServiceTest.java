package com.gestion.tramites.service;

import com.gestion.tramites.dto.tramite.TramiteResponseDTO;
import com.gestion.tramites.exception.ResourceNotFoundException;
import com.gestion.tramites.model.Entidad;
import com.gestion.tramites.model.TipoTramite;
import com.gestion.tramites.model.Tramite;
import com.gestion.tramites.model.Usuario;
import com.gestion.tramites.repository.TipoTramiteRepository;
import com.gestion.tramites.repository.TramiteRepository;
import com.gestion.tramites.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TramiteServiceTest {

    @Mock
    private TramiteRepository tramiteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TipoTramiteRepository tipoTramiteRepository;

    @Mock
    private RadicacionService radicacionService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private TramiteService tramiteService;

    private Usuario solicitante;
    private Usuario revisor;
    private Usuario adminUser;
    private Tramite tramite;
    private TipoTramite tipoTramite;
    private Entidad entidad;

    @BeforeEach
    void setUp() {
        entidad = new Entidad();
        entidad.setId(1L);
        entidad.setNombre("Curaduría 1");

        solicitante = new Usuario();
        solicitante.setIdUsuario(1L);
        solicitante.setNombreCompleto("Juan Solicitante");
        solicitante.setEntidad(entidad);

        revisor = new Usuario();
        revisor.setIdUsuario(2L);
        revisor.setNombreCompleto("Ana Revisora");
        revisor.setRol("REVISOR");
        revisor.setEntidad(entidad);

        adminUser = new Usuario();
        adminUser.setIdUsuario(99L);
        adminUser.setNombreCompleto("Admin Global");
        adminUser.setRol("ADMIN_GLOBAL");
        adminUser.setEntidad(entidad);

        tipoTramite = new TipoTramite();
        tipoTramite.setIdTipoTramite(1L);
        tipoTramite.setNombre("Licencia de Construcción");

        tramite = new Tramite();
        tramite.setIdTramite(100L);
        tramite.setEntidad(entidad);
        tramite.setSolicitante(solicitante);
        tramite.setEstadoActual(Tramite.EstadoTramite.RADICADO);
        tramite.setNumeroRadicacion("RAD-2025-100");

        // --- MOCK DEL CONTEXTO DE SEGURIDAD ---
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(new CustomUserDetails(adminUser));
    }

    @Test
    void cuandoBuscaTramitePorId_yExiste_debeRetornarTramite() {
        // Given
        when(tramiteRepository.findById(100L)).thenReturn(Optional.of(tramite));
        when(modelMapper.map(tramite, TramiteResponseDTO.class)).thenReturn(new TramiteResponseDTO());

        // When
        TramiteResponseDTO tramiteEncontrado = tramiteService.obtenerTramitePorId(100L);

        // Then
        assertNotNull(tramiteEncontrado);
        verify(tramiteRepository).findById(100L);
    }

    @Test
    void cuandoBuscaTramitePorId_yNoExiste_debeLanzarResourceNotFoundException() {
        // Given
        when(tramiteRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            tramiteService.obtenerTramitePorId(999L);
        });
    }

    @Test
    void cuandoActualizaEstado_conTransicionValida_debeCambiarEstadoYGuardar() {
        // Given
        tramite.setEstadoActual(Tramite.EstadoTramite.ASIGNADO); // CORRECCIÓN: Iniciar desde un estado válido para la transición
        when(tramiteRepository.findById(100L)).thenReturn(Optional.of(tramite));
        when(tramiteRepository.save(any(Tramite.class))).thenReturn(tramite); // CORRECCIÓN: Mockear el save
        when(modelMapper.map(any(Tramite.class), eq(TramiteResponseDTO.class))).thenReturn(new TramiteResponseDTO());


        Tramite.EstadoTramite nuevoEstado = Tramite.EstadoTramite.EN_REVISION;

        // When
        tramiteService.actualizarEstado(100L, nuevoEstado, "Iniciando revisión técnica");

        // Then
        assertEquals(nuevoEstado, tramite.getEstadoActual());
        assertEquals("Iniciando revisión técnica", tramite.getComentariosRevisor());
        verify(tramiteRepository).save(tramite);
    }

    @Test
    void cuandoAsignaRevisor_conDatosValidos_debeAsignarYGuardar() {
        // Given
        when(tramiteRepository.findById(100L)).thenReturn(Optional.of(tramite));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(revisor));
        when(tramiteRepository.save(any(Tramite.class))).thenReturn(tramite); // CORRECCIÓN: Mockear el save para que devuelva el trámite
        when(modelMapper.map(any(Tramite.class), eq(TramiteResponseDTO.class))).thenReturn(new TramiteResponseDTO());

        // When
        tramiteService.asignarRevisor(100L, 2L);

        // Then
        assertEquals(revisor, tramite.getRevisorAsignado());
        assertEquals(Tramite.EstadoTramite.ASIGNADO, tramite.getEstadoActual());
        verify(tramiteRepository).save(tramite);
    }
}
