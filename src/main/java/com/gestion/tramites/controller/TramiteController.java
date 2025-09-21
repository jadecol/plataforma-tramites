package com.gestion.tramites.controller;

import com.gestion.tramites.dto.tramite.TramiteEstadoUpdateDTO;
import com.gestion.tramites.dto.tramite.TramiteRequestDTO;
import com.gestion.tramites.dto.tramite.TramiteResponseDTO;
import com.gestion.tramites.exception.ResourceNotFoundException;
import com.gestion.tramites.model.Tramite; // Import Tramite to access EstadoTramite enum
import com.gestion.tramites.service.TramiteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tramites")
public class TramiteController {

    private final TramiteService tramiteService;

    @Autowired
    public TramiteController(TramiteService tramiteService) {
        this.tramiteService = tramiteService;
    }

    // GET all trámites
    @GetMapping
    public ResponseEntity<List<TramiteResponseDTO>> getAllTramites() {
        List<TramiteResponseDTO> tramites = tramiteService.obtenerTramites();
        return ResponseEntity.ok(tramites);
    }

    // GET trámite by ID
    @GetMapping("/{id}")
    public ResponseEntity<TramiteResponseDTO> getTramiteById(@PathVariable Long id) {
        TramiteResponseDTO tramite = tramiteService.obtenerTramitePorId(id);
        return ResponseEntity.ok(tramite);
    }

    // CREATE new trámite
    @PostMapping
    public ResponseEntity<TramiteResponseDTO> createTramite(@Valid @RequestBody TramiteRequestDTO tramiteRequestDTO) {
        TramiteResponseDTO createdTramite = tramiteService.crearTramite(tramiteRequestDTO);
        return new ResponseEntity<>(createdTramite, HttpStatus.CREATED);
    }

    // UPDATE existing trámite
    @PutMapping("/{id}")
    public ResponseEntity<TramiteResponseDTO> updateTramite(@PathVariable Long id, @Valid @RequestBody TramiteRequestDTO tramiteRequestDTO) {
        TramiteResponseDTO updatedTramite = tramiteService.actualizarTramite(id, tramiteRequestDTO);
        return ResponseEntity.ok(updatedTramite);
    }

    // DELETE trámite
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTramite(@PathVariable Long id) {
        tramiteService.eliminarTramite(id);
        return ResponseEntity.noContent().build();
    }

    // UPDATE trámite status
    @PatchMapping("/{id}/estado")
    public ResponseEntity<TramiteResponseDTO> updateTramiteEstado(@PathVariable Long id, @Valid @RequestBody TramiteEstadoUpdateDTO estadoUpdateDTO) {
        // Corrected: Convert String to Tramite.EstadoTramite enum
        Tramite.EstadoTramite nuevoEstadoEnum = Tramite.EstadoTramite.valueOf(estadoUpdateDTO.getNuevoEstado());
        TramiteResponseDTO updatedTramite = tramiteService.actualizarEstado(id, nuevoEstadoEnum, estadoUpdateDTO.getComentariosRevisor());
        return ResponseEntity.ok(updatedTramite);
    }

    // ASSIGN revisor
    @PatchMapping("/{id}/revisor/{revisorId}")
    public ResponseEntity<TramiteResponseDTO> assignRevisor(@PathVariable Long id, @PathVariable Long revisorId) {
        TramiteResponseDTO updatedTramite = tramiteService.asignarRevisor(id, revisorId);
        return ResponseEntity.ok(updatedTramite);
    }
}
