package com.gestion.tramites.service;

import com.gestion.tramites.dto.EntidadDTO;
import com.gestion.tramites.exception.ResourceNotFoundException;
import com.gestion.tramites.model.Entidad;
import com.gestion.tramites.repository.EntidadRepository;
import com.gestion.tramites.repository.EntidadGubernamentalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EntidadServiceTest {

    @Mock
    private EntidadRepository entidadRepository;

    @Mock
    private EntidadGubernamentalRepository entidadGubernamentalRepository;

    @Mock
    private VerificacionEntidadGubernamentalService verificacionService;

    @InjectMocks
    private EntidadService entidadService;

    private Entidad testEntidad;
    private EntidadDTO testEntidadDTO;

    @BeforeEach
    void setUp() {
        testEntidad = new Entidad();
        testEntidad.setId(1L);
        testEntidad.setNombre("Alcaldía Test");
        testEntidad.setNit("123456789");
        testEntidad.setDireccion("Calle Test 123");
        testEntidad.setTelefono("3001234567");
        testEntidad.setEmail("test@alcaldia.gov.co");
        testEntidad.setSitioWeb("www.alcaldiatest.gov.co");
        testEntidad.setActivo(true);

        testEntidadDTO = new EntidadDTO(1L, "Alcaldía Test", "123456789",
                "Calle Test 123", "3001234567", "test@alcaldia.gov.co",
                "www.alcaldiatest.gov.co", true);

        // No configurar mocks por defecto aquí para evitar UnnecessaryStubbingException
    }

    @Test
    void crearEntidad_ValidDTO_ReturnsCreatedDTO() {
        // Arrange
        EntidadDTO nuevaEntidadDTO = new EntidadDTO(null, "Nueva Entidad", "987654321",
                "Nueva Dirección", "3009876543", "nueva@entidad.com",
                "www.nueva.com", true);

        Entidad entidadGuardada = new Entidad();
        entidadGuardada.setId(2L);
        entidadGuardada.setNombre("Nueva Entidad");
        entidadGuardada.setNit("987654321");
        entidadGuardada.setDireccion("Nueva Dirección");
        entidadGuardada.setTelefono("3009876543");
        entidadGuardada.setEmail("nueva@entidad.com");
        entidadGuardada.setSitioWeb("www.nueva.com");
        entidadGuardada.setActivo(true);

        when(entidadRepository.save(any(Entidad.class))).thenReturn(entidadGuardada);

        // Act
        EntidadDTO resultado = entidadService.crearEntidad(nuevaEntidadDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(2L, resultado.getId());
        assertEquals("Nueva Entidad", resultado.getNombre());
        assertEquals("987654321", resultado.getNit());
        verify(entidadRepository).save(any(Entidad.class));
    }

    @Test
    void obtenerTodasLasEntidades_ReturnsListOfDTOs() {
        // Arrange
        Entidad entidad2 = new Entidad();
        entidad2.setId(2L);
        entidad2.setNombre("Segunda Entidad");
        entidad2.setNit("987654321");
        entidad2.setActivo(true);

        when(entidadRepository.findAll()).thenReturn(Arrays.asList(testEntidad, entidad2));

        // Act
        List<EntidadDTO> resultado = entidadService.obtenerTodasLasEntidades();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Alcaldía Test", resultado.get(0).getNombre());
        assertEquals("Segunda Entidad", resultado.get(1).getNombre());
        verify(entidadRepository).findAll();
    }

    @Test
    void obtenerEntidadPorId_ExistingId_ReturnsDTO() {
        // Arrange
        when(entidadRepository.findById(1L)).thenReturn(Optional.of(testEntidad));

        // Act
        Optional<EntidadDTO> resultado = entidadService.obtenerEntidadPorId(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
        assertEquals("Alcaldía Test", resultado.get().getNombre());
        verify(entidadRepository).findById(1L);
    }

    @Test
    void obtenerEntidadPorId_NonExistingId_ReturnsEmpty() {
        // Arrange
        when(entidadRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<EntidadDTO> resultado = entidadService.obtenerEntidadPorId(999L);

        // Assert
        assertFalse(resultado.isPresent());
        verify(entidadRepository).findById(999L);
    }

    @Test
    void actualizarEntidad_ExistingId_ReturnsUpdatedDTO() {
        // Arrange
        EntidadDTO entidadActualizadaDTO = new EntidadDTO(1L, "Alcaldía Actualizada", "111222333",
                "Nueva Dirección", "3111111111", "actualizada@alcaldia.gov.co",
                "www.actualizada.gov.co", false);

        when(entidadRepository.findById(1L)).thenReturn(Optional.of(testEntidad));
        when(entidadRepository.save(any(Entidad.class))).thenAnswer(invocation -> {
            Entidad entidad = invocation.getArgument(0);
            entidad.setId(1L);
            return entidad;
        });

        // Act
        EntidadDTO resultado = entidadService.actualizarEntidad(1L, entidadActualizadaDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Alcaldía Actualizada", resultado.getNombre());
        assertEquals("111222333", resultado.getNit());
        assertEquals(false, resultado.isActivo());
        verify(entidadRepository).findById(1L);
        verify(entidadRepository).save(any(Entidad.class));
    }

    @Test
    void actualizarEntidad_NonExistingId_ThrowsException() {
        // Arrange
        when(entidadRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            entidadService.actualizarEntidad(999L, testEntidadDTO));

        verify(entidadRepository).findById(999L);
        verify(entidadRepository, never()).save(any(Entidad.class));
    }

    @Test
    void eliminarEntidad_ExistingId_DeletesSuccessfully() {
        // Arrange
        when(entidadRepository.existsById(1L)).thenReturn(true);

        // Act
        assertDoesNotThrow(() -> entidadService.eliminarEntidad(1L));

        // Assert
        verify(entidadRepository).existsById(1L);
        verify(entidadRepository).deleteById(1L);
    }

    @Test
    void eliminarEntidad_NonExistingId_ThrowsException() {
        // Arrange
        when(entidadRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            entidadService.eliminarEntidad(999L));

        verify(entidadRepository).existsById(999L);
        verify(entidadRepository, never()).deleteById(anyLong());
    }

    @Test
    void cambiarEstadoEntidad_ExistingId_ChangesStatus() {
        // Arrange
        when(entidadRepository.findById(1L)).thenReturn(Optional.of(testEntidad));
        when(entidadRepository.save(any(Entidad.class))).thenAnswer(invocation -> {
            Entidad entidad = invocation.getArgument(0);
            return entidad;
        });

        // Act
        EntidadDTO resultado = entidadService.cambiarEstadoEntidad(1L, false);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(false, resultado.isActivo());
        verify(entidadRepository).findById(1L);
        verify(entidadRepository).save(testEntidad);
    }

    @Test
    void cambiarEstadoEntidad_NonExistingId_ThrowsException() {
        // Arrange
        when(entidadRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            entidadService.cambiarEstadoEntidad(999L, false));

        verify(entidadRepository).findById(999L);
        verify(entidadRepository, never()).save(any(Entidad.class));
    }
}