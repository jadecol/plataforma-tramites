package com.gestion.tramites.service;

import com.gestion.tramites.dto.tramite.TramiteRequestDTO;
import com.gestion.tramites.dto.tramite.TramiteResponseDTO;
import com.gestion.tramites.model.Entidad;
import com.gestion.tramites.model.TipoTramite;
import com.gestion.tramites.model.Tramite;
import com.gestion.tramites.model.Usuario;
import com.gestion.tramites.repository.EntidadRepository;
import com.gestion.tramites.repository.TipoTramiteRepository;
import com.gestion.tramites.repository.TramiteRepository;
import com.gestion.tramites.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TramiteServiceTest {

    // @Mock crea una simulación de la dependencia.
    @Mock
    private TramiteRepository tramiteRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private EntidadRepository entidadRepository;
    @Mock
    private TipoTramiteRepository tipoTramiteRepository;
    @Mock
    private RadicacionService radicacionService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;

    // @InjectMocks crea una instancia de TramiteService e inyecta los @Mock de arriba.
    @InjectMocks
    private TramiteService tramiteService;

    @BeforeEach
    void setUp() {
        // Simulamos el contexto de seguridad en cada prueba
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void crearTramite_cuandoUsuarioEsValido_deberiaCrearYGuardarElTramite() {
        // 1. ARRANGE (Organizar)
        
        // Datos de entrada
        TramiteRequestDTO request = new TramiteRequestDTO();
        request.setIdSolicitante(1L);
        request.setIdTipoTramite(1L);
        request.setObjetoTramite("Construcción de vivienda");

        // Simulación del usuario autenticado (CORREGIDO)
        Entidad mockCurrentUserEntidad = new Entidad();
        mockCurrentUserEntidad.setId(10L);
        Usuario mockCurrentUserUsuario = new Usuario();
        mockCurrentUserUsuario.setIdUsuario(100L);
        mockCurrentUserUsuario.setRol("ADMIN_ENTIDAD");
        mockCurrentUserUsuario.setEntidad(mockCurrentUserEntidad);
        CustomUserDetails mockCurrentUser = new CustomUserDetails(mockCurrentUserUsuario);
        when(authentication.getPrincipal()).thenReturn(mockCurrentUser);

        // Simulación de los objetos que los repositorios "encontrarán"
        Usuario mockSolicitante = new Usuario();
        mockSolicitante.setIdUsuario(1L);
        TipoTramite mockTipoTramite = new TipoTramite();
        mockTipoTramite.setIdTipoTramite(1L); // CORREGIDO

        // Simulación de las llamadas a los mocks
        when(entidadRepository.findById(10L)).thenReturn(Optional.of(mockCurrentUserEntidad));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockSolicitante));
        when(tipoTramiteRepository.findById(1L)).thenReturn(Optional.of(mockTipoTramite));
        when(radicacionService.generarNumeroRadicacion(any(Entidad.class), any(TipoTramite.class))).thenReturn("RAD-2025-001");
        
        // Cuando se guarde el trámite, devolvemos el mismo objeto para consistencia
        when(tramiteRepository.save(any(Tramite.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Simulación del mapeo a DTO
        when(modelMapper.map(any(Tramite.class), eq(TramiteResponseDTO.class))).thenReturn(new TramiteResponseDTO());

        // 2. ACT (Actuar)
        TramiteResponseDTO resultado = tramiteService.crearTramite(request);

        // 3. ASSERT (Afirmar)
        
        // Verificamos que el resultado no sea nulo
        assertNotNull(resultado);

        // Usamos un ArgumentCaptor para "capturar" el objeto que se pasó al método save()
        ArgumentCaptor<Tramite> tramiteCaptor = ArgumentCaptor.forClass(Tramite.class);
        verify(tramiteRepository, times(1)).save(tramiteCaptor.capture());
        
        // Obtenemos el trámite capturado
        Tramite tramiteGuardado = tramiteCaptor.getValue();

        // Verificamos que los datos correctos fueron asignados al trámite antes de guardarlo
        assertEquals("RAD-2025-001", tramiteGuardado.getNumeroRadicacion());
        assertEquals("Construcción de vivienda", tramiteGuardado.getObjetoTramite());
        assertEquals(10L, tramiteGuardado.getEntidad().getId());
        assertEquals(1L, tramiteGuardado.getSolicitante().getIdUsuario());
        assertEquals(Tramite.EstadoTramite.RADICADO, tramiteGuardado.getEstadoActual());
    }
}
