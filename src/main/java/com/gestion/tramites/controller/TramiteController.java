package com.gestion.tramites.controller;

import com.gestion.tramites.tramite.TramiteService;
import com.gestion.tramites.tramite.dto.TramiteEstadoUpdateDTO;
import com.gestion.tramites.tramite.dto.TramiteRequestDTO;
import com.gestion.tramites.tramite.dto.TramiteResponseDTO;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Importante para la seguridad
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tramites")
public class TramiteController {

    private final TramiteService tramiteService;

    public TramiteController(TramiteService tramiteService) {
        this.tramiteService = tramiteService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_GLOBAL', 'ADMIN_ENTIDAD', 'SOLICITANTE')") // Solicitante o Admin pueden crear
    public ResponseEntity<TramiteResponseDTO> crearTramite(@Valid @RequestBody TramiteRequestDTO tramiteRequestDTO) {
        TramiteResponseDTO nuevoTramite = tramiteService.crearTramite(tramiteRequestDTO);
        return new ResponseEntity<>(nuevoTramite, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_GLOBAL', 'ADMIN_ENTIDAD', 'REVISOR', 'SOLICITANTE')") // Todos pueden ver (con filtros posteriores si aplica)
    public ResponseEntity<List<TramiteResponseDTO>> getAllTramites() {
        List<TramiteResponseDTO> tramites = tramiteService.getAllTramites();
        return ResponseEntity.ok(tramites);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_GLOBAL', 'ADMIN_ENTIDAD', 'REVISOR', 'SOLICITANTE')") // Todos pueden ver su propio trámite o si tienen rol adecuado
    public ResponseEntity<TramiteResponseDTO> getTramiteById(@PathVariable Long id) {
        TramiteResponseDTO tramite = tramiteService.getTramiteById(id);
        return ResponseEntity.ok(tramite);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_GLOBAL', 'ADMIN_ENTIDAD', 'SOLICITANTE')") // Solo quien puede crear, y admins
    public ResponseEntity<TramiteResponseDTO> updateTramite(@PathVariable Long id, @Valid @RequestBody TramiteRequestDTO tramiteRequestDTO) {
        TramiteResponseDTO updatedTramite = tramiteService.updateTramite(id, tramiteRequestDTO);
        return ResponseEntity.ok(updatedTramite);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_GLOBAL', 'ADMIN_ENTIDAD')") // Solo admins pueden eliminar
    public ResponseEntity<Void> deleteTramite(@PathVariable Long id) {
        tramiteService.deleteTramite(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Endpoint para asignar revisor a un trámite
    @PatchMapping("/{id}/asignar-revisor/{idRevisor}")
    @PreAuthorize("hasAnyRole('ADMIN_GLOBAL', 'ADMIN_ENTIDAD')") // Solo admins pueden asignar revisor
    public ResponseEntity<TramiteResponseDTO> asignarRevisor(@PathVariable Long id, @PathVariable Long idRevisor) {
        TramiteResponseDTO tramite = tramiteService.asignarRevisor(id, idRevisor);
        return ResponseEntity.ok(tramite);
    }

    // Endpoint para actualizar el estado del trámite
    @PatchMapping("/{id}/estado")
    // Revisores y Admins pueden actualizar el estado
    @PreAuthorize("hasAnyRole('ADMIN_GLOBAL', 'ADMIN_ENTIDAD', 'REVISOR')")
    public ResponseEntity<TramiteResponseDTO> actualizarEstadoTramite(@PathVariable Long id, @Valid @RequestBody TramiteEstadoUpdateDTO updateDTO) {
        TramiteResponseDTO tramite = tramiteService.actualizarEstadoTramite(id, updateDTO);
        return ResponseEntity.ok(tramite);
    }
}
