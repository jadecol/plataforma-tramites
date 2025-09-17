package com.gestion.tramites.controller;

import com.gestion.tramites.model.Entidad; // Sigue siendo necesario para la clase
import com.gestion.tramites.service.EntidadService;
import com.gestion.tramites.exception.ResourceNotFoundException;
import com.gestion.tramites.dto.EntidadDTO; // <-- Importa el EntidadDTO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/entidades")
public class EntidadController {

    private final EntidadService entidadService;

    @Autowired
    public EntidadController(EntidadService entidadService) {
        this.entidadService = entidadService;
    }

    @PreAuthorize("hasRole('ADMIN_GLOBAL')")
    @PostMapping
    public ResponseEntity<EntidadDTO> crearEntidad(@RequestBody EntidadDTO entidadDto) { // Recibe
                                                                                         // DTO
        EntidadDTO nuevaEntidad = entidadService.crearEntidad(entidadDto); // Servicio devuelve DTO
        return new ResponseEntity<>(nuevaEntidad, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN_GLOBAL', 'ADMIN_ENTIDAD')")
    @GetMapping
    public ResponseEntity<List<EntidadDTO>> obtenerTodasLasEntidades() { // Devuelve lista de DTOs
        List<EntidadDTO> entidades = entidadService.obtenerTodasLasEntidades();
        return new ResponseEntity<>(entidades, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN_GLOBAL', 'ADMIN_ENTIDAD')")
    @GetMapping("/{id}")
    public ResponseEntity<EntidadDTO> obtenerEntidadPorId(@PathVariable Long id) { // Devuelve DTO
        try {
            return entidadService.obtenerEntidadPorId(id)
                    .map(entidadDto -> new ResponseEntity<>(entidadDto, HttpStatus.OK)) // Mapea a
                                                                                        // DTO
                    .orElseThrow(() -> new ResourceNotFoundException("Entidad", "id", id));
        } catch (ResourceNotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PreAuthorize("hasRole('ADMIN_GLOBAL')")
    @PutMapping("/{id}")
    public ResponseEntity<EntidadDTO> actualizarEntidad(@PathVariable Long id,
            @RequestBody EntidadDTO entidadDto) { // Recibe y devuelve DTO
        try {
            EntidadDTO entidadActualizada = entidadService.actualizarEntidad(id, entidadDto); // Servicio
                                                                                              // devuelve
                                                                                              // DTO
            return new ResponseEntity<>(entidadActualizada, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PreAuthorize("hasRole('ADMIN_GLOBAL')")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<EntidadDTO> cambiarEstadoEntidad(@PathVariable Long id,
            @RequestParam boolean activo) { // Devuelve DTO
        try {
            EntidadDTO entidadActualizada = entidadService.cambiarEstadoEntidad(id, activo); // Servicio
                                                                                             // devuelve
                                                                                             // DTO
            return new ResponseEntity<>(entidadActualizada, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PreAuthorize("hasRole('ADMIN_GLOBAL')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEntidad(@PathVariable Long id) {
        try {
            entidadService.eliminarEntidad(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ResourceNotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
