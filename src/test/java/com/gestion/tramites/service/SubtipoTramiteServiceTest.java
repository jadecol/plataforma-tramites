package com.gestion.tramites.service;

import com.gestion.tramites.exception.ResourceNotFoundException;
import com.gestion.tramites.model.SubtipoTramite;
import com.gestion.tramites.repository.SubtipoTramiteRepository;
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
class SubtipoTramiteServiceTest {

    @Mock
    private SubtipoTramiteRepository subtipoTramiteRepository;

    @InjectMocks
    private SubtipoTramiteService subtipoTramiteService;

    private SubtipoTramite testSubtipo;

    @BeforeEach
    void setUp() {
        testSubtipo = new SubtipoTramite();
        testSubtipo.setIdSubtipoTramite(1L);
        testSubtipo.setNombre("Subtipo Test");
    }

    @Test
    void getAllSubtipos_ReturnsListOfSubtipos() {
        // Arrange
        SubtipoTramite subtipo2 = new SubtipoTramite();
        subtipo2.setIdSubtipoTramite(2L);
        subtipo2.setNombre("Segundo Subtipo");

        when(subtipoTramiteRepository.findAll())
                .thenReturn(Arrays.asList(testSubtipo, subtipo2));

        // Act
        List<SubtipoTramite> resultado = subtipoTramiteService.getAllSubtipos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Subtipo Test", resultado.get(0).getNombre());
        assertEquals("Segundo Subtipo", resultado.get(1).getNombre());
        verify(subtipoTramiteRepository).findAll();
    }

    @Test
    void getAllSubtipos_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(subtipoTramiteRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<SubtipoTramite> resultado = subtipoTramiteService.getAllSubtipos();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(subtipoTramiteRepository).findAll();
    }

    @Test
    void saveSubtipo_ValidSubtipo_ReturnsSavedSubtipo() {
        // Arrange
        SubtipoTramite nuevoSubtipo = new SubtipoTramite();
        nuevoSubtipo.setNombre("Nuevo Subtipo");

        SubtipoTramite subtipoGuardado = new SubtipoTramite();
        subtipoGuardado.setIdSubtipoTramite(3L);
        subtipoGuardado.setNombre("Nuevo Subtipo");

        when(subtipoTramiteRepository.save(nuevoSubtipo)).thenReturn(subtipoGuardado);

        // Act
        SubtipoTramite resultado = subtipoTramiteService.saveSubtipo(nuevoSubtipo);

        // Assert
        assertNotNull(resultado);
        assertEquals(3L, resultado.getIdSubtipoTramite());
        assertEquals("Nuevo Subtipo", resultado.getNombre());
        verify(subtipoTramiteRepository).save(nuevoSubtipo);
    }

    @Test
    void getSubtipoById_ExistingId_ReturnsSubtipo() {
        // Arrange
        when(subtipoTramiteRepository.findById(1L)).thenReturn(Optional.of(testSubtipo));

        // Act
        SubtipoTramite resultado = subtipoTramiteService.getSubtipoById(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdSubtipoTramite());
        assertEquals("Subtipo Test", resultado.getNombre());
        verify(subtipoTramiteRepository).findById(1L);
    }

    @Test
    void getSubtipoById_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        when(subtipoTramiteRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> subtipoTramiteService.getSubtipoById(999L)
        );

        assertNotNull(exception.getMessage());
        verify(subtipoTramiteRepository).findById(999L);
    }

    @Test
    void updateSubtipo_ExistingId_ReturnsUpdatedSubtipo() {
        // Arrange
        SubtipoTramite detallesActualizacion = new SubtipoTramite();
        detallesActualizacion.setNombre("Subtipo Actualizado");

        when(subtipoTramiteRepository.findById(1L)).thenReturn(Optional.of(testSubtipo));
        when(subtipoTramiteRepository.save(any(SubtipoTramite.class)))
                .thenAnswer(invocation -> {
                    SubtipoTramite subtipo = invocation.getArgument(0);
                    return subtipo;
                });

        // Act
        SubtipoTramite resultado = subtipoTramiteService.updateSubtipo(1L, detallesActualizacion);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdSubtipoTramite());
        assertEquals("Subtipo Actualizado", resultado.getNombre());
        verify(subtipoTramiteRepository).findById(1L);
        verify(subtipoTramiteRepository).save(testSubtipo);
    }

    @Test
    void updateSubtipo_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        SubtipoTramite detallesActualizacion = new SubtipoTramite();
        detallesActualizacion.setNombre("Subtipo Actualizado");

        when(subtipoTramiteRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> subtipoTramiteService.updateSubtipo(999L, detallesActualizacion));

        verify(subtipoTramiteRepository).findById(999L);
        verify(subtipoTramiteRepository, never()).save(any(SubtipoTramite.class));
    }

    @Test
    void deleteSubtipo_ExistingId_ReturnsSuccessMap() {
        // Arrange
        when(subtipoTramiteRepository.findById(1L)).thenReturn(Optional.of(testSubtipo));

        // Act
        Map<String, Boolean> resultado = subtipoTramiteService.deleteSubtipo(1L);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.get("eliminado"));
        verify(subtipoTramiteRepository).findById(1L);
        verify(subtipoTramiteRepository).delete(testSubtipo);
    }

    @Test
    void deleteSubtipo_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        when(subtipoTramiteRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> subtipoTramiteService.deleteSubtipo(999L));

        verify(subtipoTramiteRepository).findById(999L);
        verify(subtipoTramiteRepository, never()).delete(any(SubtipoTramite.class));
    }

    @Test
    void saveSubtipo_NullSubtipo_PassesToRepository() {
        // Arrange
        when(subtipoTramiteRepository.save(null)).thenReturn(null);

        // Act
        SubtipoTramite resultado = subtipoTramiteService.saveSubtipo(null);

        // Assert
        assertNull(resultado);
        verify(subtipoTramiteRepository).save(null);
    }

    @Test
    void updateSubtipo_OnlyUpdatesNombre() {
        // Arrange
        SubtipoTramite originalSubtipo = new SubtipoTramite();
        originalSubtipo.setIdSubtipoTramite(1L);
        originalSubtipo.setNombre("Nombre Original");

        SubtipoTramite detallesActualizacion = new SubtipoTramite();
        detallesActualizacion.setIdSubtipoTramite(999L); // ID diferente
        detallesActualizacion.setNombre("Nombre Actualizado");

        when(subtipoTramiteRepository.findById(1L)).thenReturn(Optional.of(originalSubtipo));
        when(subtipoTramiteRepository.save(any(SubtipoTramite.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        SubtipoTramite resultado = subtipoTramiteService.updateSubtipo(1L, detallesActualizacion);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdSubtipoTramite()); // ID original se mantiene
        assertEquals("Nombre Actualizado", resultado.getNombre()); // Nombre se actualiza
        verify(subtipoTramiteRepository).save(originalSubtipo);
    }

    @Test
    void deleteSubtipo_CorrectMapStructure() {
        // Arrange
        when(subtipoTramiteRepository.findById(1L)).thenReturn(Optional.of(testSubtipo));

        // Act
        Map<String, Boolean> resultado = subtipoTramiteService.deleteSubtipo(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.containsKey("eliminado"));
        assertEquals(Boolean.TRUE, resultado.get("eliminado"));
    }
}