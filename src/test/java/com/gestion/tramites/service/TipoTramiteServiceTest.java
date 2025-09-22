package com.gestion.tramites.service;

import com.gestion.tramites.exception.ResourceNotFoundException;
import com.gestion.tramites.model.TipoTramite;
import com.gestion.tramites.repository.TipoTramiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TipoTramiteServiceTest {

    @Mock
    private TipoTramiteRepository tipoTramiteRepository;

    @InjectMocks
    private TipoTramiteService tipoTramiteService;

    private TipoTramite testTipo;

    @BeforeEach
    void setUp() {
        testTipo = new TipoTramite();
        testTipo.setIdTipoTramite(1L);
        testTipo.setNombre("Tipo Test");
    }

    @Test
    void getAllTipos_ReturnsListOfTipos() {
        // Arrange
        TipoTramite tipo2 = new TipoTramite();
        tipo2.setIdTipoTramite(2L);
        tipo2.setNombre("Segundo Tipo");

        when(tipoTramiteRepository.findAll())
                .thenReturn(Arrays.asList(testTipo, tipo2));

        // Act
        List<TipoTramite> resultado = tipoTramiteService.getAllTipos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Tipo Test", resultado.get(0).getNombre());
        assertEquals("Segundo Tipo", resultado.get(1).getNombre());
        verify(tipoTramiteRepository).findAll();
    }

    @Test
    void getAllTipos_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(tipoTramiteRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<TipoTramite> resultado = tipoTramiteService.getAllTipos();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(tipoTramiteRepository).findAll();
    }

    @Test
    void saveTipo_ValidTipo_ReturnsSavedTipo() {
        // Arrange
        TipoTramite nuevoTipo = new TipoTramite();
        nuevoTipo.setNombre("Nuevo Tipo");

        TipoTramite tipoGuardado = new TipoTramite();
        tipoGuardado.setIdTipoTramite(3L);
        tipoGuardado.setNombre("Nuevo Tipo");

        when(tipoTramiteRepository.save(nuevoTipo)).thenReturn(tipoGuardado);

        // Act
        TipoTramite resultado = tipoTramiteService.saveTipo(nuevoTipo);

        // Assert
        assertNotNull(resultado);
        assertEquals(3L, resultado.getIdTipoTramite());
        assertEquals("Nuevo Tipo", resultado.getNombre());
        verify(tipoTramiteRepository).save(nuevoTipo);
    }

    @Test
    void getTipoById_ExistingId_ReturnsTipo() {
        // Arrange
        when(tipoTramiteRepository.findById(1L)).thenReturn(Optional.of(testTipo));

        // Act
        TipoTramite resultado = tipoTramiteService.getTipoById(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdTipoTramite());
        assertEquals("Tipo Test", resultado.getNombre());
        verify(tipoTramiteRepository).findById(1L);
    }

    @Test
    void getTipoById_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        when(tipoTramiteRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> tipoTramiteService.getTipoById(999L)
        );

        assertNotNull(exception.getMessage());
        verify(tipoTramiteRepository).findById(999L);
    }

    @Test
    void updateTipo_ExistingId_ReturnsUpdatedTipo() {
        // Arrange
        TipoTramite detallesActualizacion = new TipoTramite();
        detallesActualizacion.setNombre("Tipo Actualizado");

        when(tipoTramiteRepository.findById(1L)).thenReturn(Optional.of(testTipo));
        when(tipoTramiteRepository.save(any(TipoTramite.class)))
                .thenAnswer(invocation -> {
                    TipoTramite tipo = invocation.getArgument(0);
                    return tipo;
                });

        // Act
        TipoTramite resultado = tipoTramiteService.updateTipo(1L, detallesActualizacion);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdTipoTramite());
        assertEquals("Tipo Actualizado", resultado.getNombre());
        verify(tipoTramiteRepository).findById(1L);
        verify(tipoTramiteRepository).save(testTipo);
    }

    @Test
    void updateTipo_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        TipoTramite detallesActualizacion = new TipoTramite();
        detallesActualizacion.setNombre("Tipo Actualizado");

        when(tipoTramiteRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> tipoTramiteService.updateTipo(999L, detallesActualizacion));

        verify(tipoTramiteRepository).findById(999L);
        verify(tipoTramiteRepository, never()).save(any(TipoTramite.class));
    }

    @Test
    void deleteTipo_ExistingId_ReturnsSuccessMap() {
        // Arrange
        when(tipoTramiteRepository.findById(1L)).thenReturn(Optional.of(testTipo));

        // Act
        Map<String, Boolean> resultado = tipoTramiteService.deleteTipo(1L);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.get("eliminado"));
        verify(tipoTramiteRepository).findById(1L);
        verify(tipoTramiteRepository).delete(testTipo);
    }

    @Test
    void deleteTipo_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        when(tipoTramiteRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> tipoTramiteService.deleteTipo(999L));

        verify(tipoTramiteRepository).findById(999L);
        verify(tipoTramiteRepository, never()).delete(any(TipoTramite.class));
    }

    @Test
    void saveTipo_NullTipo_PassesToRepository() {
        // Arrange
        when(tipoTramiteRepository.save(null)).thenReturn(null);

        // Act
        TipoTramite resultado = tipoTramiteService.saveTipo(null);

        // Assert
        assertNull(resultado);
        verify(tipoTramiteRepository).save(null);
    }

    @Test
    void updateTipo_OnlyUpdatesNombre() {
        // Arrange
        TipoTramite originalTipo = new TipoTramite();
        originalTipo.setIdTipoTramite(1L);
        originalTipo.setNombre("Nombre Original");

        TipoTramite detallesActualizacion = new TipoTramite();
        detallesActualizacion.setIdTipoTramite(999L); // ID diferente
        detallesActualizacion.setNombre("Nombre Actualizado");

        when(tipoTramiteRepository.findById(1L)).thenReturn(Optional.of(originalTipo));
        when(tipoTramiteRepository.save(any(TipoTramite.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TipoTramite resultado = tipoTramiteService.updateTipo(1L, detallesActualizacion);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdTipoTramite()); // ID original se mantiene
        assertEquals("Nombre Actualizado", resultado.getNombre()); // Nombre se actualiza
        verify(tipoTramiteRepository).save(originalTipo);
    }

    @Test
    void deleteTipo_CorrectMapStructure() {
        // Arrange
        when(tipoTramiteRepository.findById(1L)).thenReturn(Optional.of(testTipo));

        // Act
        Map<String, Boolean> resultado = tipoTramiteService.deleteTipo(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.containsKey("eliminado"));
        assertEquals(Boolean.TRUE, resultado.get("eliminado"));
    }

    @Test
    void getAllTipos_VerifyRepositoryCall() {
        // Arrange
        when(tipoTramiteRepository.findAll()).thenReturn(Arrays.asList(testTipo));

        // Act
        tipoTramiteService.getAllTipos();

        // Assert
        verify(tipoTramiteRepository, times(1)).findAll();
        verifyNoMoreInteractions(tipoTramiteRepository);
    }

    @Test
    void saveTipo_VerifyRepositoryCall() {
        // Arrange
        when(tipoTramiteRepository.save(testTipo)).thenReturn(testTipo);

        // Act
        tipoTramiteService.saveTipo(testTipo);

        // Assert
        verify(tipoTramiteRepository, times(1)).save(testTipo);
        verifyNoMoreInteractions(tipoTramiteRepository);
    }
}