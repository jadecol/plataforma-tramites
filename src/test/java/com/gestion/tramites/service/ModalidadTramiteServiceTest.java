package com.gestion.tramites.service;

import com.gestion.tramites.exception.ResourceNotFoundException;
import com.gestion.tramites.model.ModalidadTramite;
import com.gestion.tramites.repository.ModalidadTramiteRepository;
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
class ModalidadTramiteServiceTest {

    @Mock
    private ModalidadTramiteRepository modalidadTramiteRepository;

    @InjectMocks
    private ModalidadTramiteService modalidadTramiteService;

    private ModalidadTramite testModalidad;

    @BeforeEach
    void setUp() {
        testModalidad = new ModalidadTramite();
        testModalidad.setIdModalidadTramite(1L);
        testModalidad.setNombre("Modalidad Test");
    }

    @Test
    void getAllModalidades_ReturnsListOfModalidades() {
        // Arrange
        ModalidadTramite modalidad2 = new ModalidadTramite();
        modalidad2.setIdModalidadTramite(2L);
        modalidad2.setNombre("Segunda Modalidad");

        when(modalidadTramiteRepository.findAll())
                .thenReturn(Arrays.asList(testModalidad, modalidad2));

        // Act
        List<ModalidadTramite> resultado = modalidadTramiteService.getAllModalidades();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Modalidad Test", resultado.get(0).getNombre());
        assertEquals("Segunda Modalidad", resultado.get(1).getNombre());
        verify(modalidadTramiteRepository).findAll();
    }

    @Test
    void getAllModalidades_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(modalidadTramiteRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<ModalidadTramite> resultado = modalidadTramiteService.getAllModalidades();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(modalidadTramiteRepository).findAll();
    }

    @Test
    void saveModalidad_ValidModalidad_ReturnsSavedModalidad() {
        // Arrange
        ModalidadTramite nuevaModalidad = new ModalidadTramite();
        nuevaModalidad.setNombre("Nueva Modalidad");

        ModalidadTramite modalidadGuardada = new ModalidadTramite();
        modalidadGuardada.setIdModalidadTramite(3L);
        modalidadGuardada.setNombre("Nueva Modalidad");

        when(modalidadTramiteRepository.save(nuevaModalidad)).thenReturn(modalidadGuardada);

        // Act
        ModalidadTramite resultado = modalidadTramiteService.saveModalidad(nuevaModalidad);

        // Assert
        assertNotNull(resultado);
        assertEquals(3L, resultado.getIdModalidadTramite());
        assertEquals("Nueva Modalidad", resultado.getNombre());
        verify(modalidadTramiteRepository).save(nuevaModalidad);
    }

    @Test
    void getModalidadById_ExistingId_ReturnsModalidad() {
        // Arrange
        when(modalidadTramiteRepository.findById(1L)).thenReturn(Optional.of(testModalidad));

        // Act
        ModalidadTramite resultado = modalidadTramiteService.getModalidadById(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdModalidadTramite());
        assertEquals("Modalidad Test", resultado.getNombre());
        verify(modalidadTramiteRepository).findById(1L);
    }

    @Test
    void getModalidadById_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        when(modalidadTramiteRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> modalidadTramiteService.getModalidadById(999L)
        );

        assertNotNull(exception.getMessage());
        verify(modalidadTramiteRepository).findById(999L);
    }

    @Test
    void updateModalidad_ExistingId_ReturnsUpdatedModalidad() {
        // Arrange
        ModalidadTramite detallesActualizacion = new ModalidadTramite();
        detallesActualizacion.setNombre("Modalidad Actualizada");

        when(modalidadTramiteRepository.findById(1L)).thenReturn(Optional.of(testModalidad));
        when(modalidadTramiteRepository.save(any(ModalidadTramite.class)))
                .thenAnswer(invocation -> {
                    ModalidadTramite modalidad = invocation.getArgument(0);
                    return modalidad;
                });

        // Act
        ModalidadTramite resultado = modalidadTramiteService.updateModalidad(1L, detallesActualizacion);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdModalidadTramite());
        assertEquals("Modalidad Actualizada", resultado.getNombre());
        verify(modalidadTramiteRepository).findById(1L);
        verify(modalidadTramiteRepository).save(testModalidad);
    }

    @Test
    void updateModalidad_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        ModalidadTramite detallesActualizacion = new ModalidadTramite();
        detallesActualizacion.setNombre("Modalidad Actualizada");

        when(modalidadTramiteRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> modalidadTramiteService.updateModalidad(999L, detallesActualizacion));

        verify(modalidadTramiteRepository).findById(999L);
        verify(modalidadTramiteRepository, never()).save(any(ModalidadTramite.class));
    }

    @Test
    void deleteModalidad_ExistingId_ReturnsSuccessMap() {
        // Arrange
        when(modalidadTramiteRepository.findById(1L)).thenReturn(Optional.of(testModalidad));

        // Act
        Map<String, Boolean> resultado = modalidadTramiteService.deleteModalidad(1L);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.get("eliminado"));
        verify(modalidadTramiteRepository).findById(1L);
        verify(modalidadTramiteRepository).delete(testModalidad);
    }

    @Test
    void deleteModalidad_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        when(modalidadTramiteRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> modalidadTramiteService.deleteModalidad(999L));

        verify(modalidadTramiteRepository).findById(999L);
        verify(modalidadTramiteRepository, never()).delete(any(ModalidadTramite.class));
    }

    @Test
    void saveModalidad_NullModalidad_PassesToRepository() {
        // Arrange
        when(modalidadTramiteRepository.save(null)).thenReturn(null);

        // Act
        ModalidadTramite resultado = modalidadTramiteService.saveModalidad(null);

        // Assert
        assertNull(resultado);
        verify(modalidadTramiteRepository).save(null);
    }
}