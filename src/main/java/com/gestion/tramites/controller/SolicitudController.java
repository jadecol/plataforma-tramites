package com.gestion.tramites.controller;

import com.gestion.tramites.model.Solicitud;
import com.gestion.tramites.service.SolicitudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/solicitudes")
public class SolicitudController {

    @Autowired
    private SolicitudService solicitudService;

    // POST: Crear una nueva solicitud
    // Solo Solicitantes pueden crear una solicitud. El ID del solicitante se toma del token.
    @PostMapping
    public ResponseEntity<Solicitud> crearSolicitud(@RequestBody Solicitud solicitud) {
        // El solicitante se establecerá en el servicio a partir del usuario autenticado
        Solicitud nuevaSolicitud = solicitudService.crearSolicitud(solicitud);
        return new ResponseEntity<>(nuevaSolicitud, HttpStatus.CREATED);
    }

    // GET: Obtener todas las solicitudes (filtrado por rol en el servicio)
    // ADMIN ve todas, SOLICITANTE ve las suyas, REVISOR ve las asignadas.
    @GetMapping
    public ResponseEntity<List<Solicitud>> listarSolicitudes() {
        List<Solicitud> solicitudes = solicitudService.getAllSolicitudes();
        return ResponseEntity.ok(solicitudes);
    }

    // GET: Obtener una solicitud por ID (con validación de acceso por rol en el servicio)
    @GetMapping("/{id}")
    public ResponseEntity<Solicitud> obtenerSolicitudPorId(@PathVariable Long id) {
        Solicitud solicitud = solicitudService.getSolicitudById(id);
        return ResponseEntity.ok(solicitud);
    }

    // PUT: Actualizar una solicitud (Solo ADMIN o REVISOR ASIGNADO pueden modificar ciertos campos)
    @PutMapping("/{id}")
    public ResponseEntity<Solicitud> actualizarSolicitud(@PathVariable Long id, @RequestBody Solicitud detallesSolicitud) {
        Solicitud solicitudActualizada = solicitudService.updateSolicitud(id, detallesSolicitud);
        return ResponseEntity.ok(solicitudActualizada);
    }

    // PUT: Asignar Revisor a una solicitud (Solo ADMIN)
    @PutMapping("/{solicitudId}/asignar-revisor/{revisorId}")
    public ResponseEntity<Solicitud> asignarRevisor(
            @PathVariable Long solicitudId,
            @PathVariable Long revisorId) {
        Solicitud solicitud = solicitudService.asignarRevisor(solicitudId, revisorId);
        return ResponseEntity.ok(solicitud);
    }

    // DELETE: Eliminar una solicitud (Solo ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> eliminarSolicitud(@PathVariable Long id) {
        Map<String, Boolean> respuesta = solicitudService.deleteSolicitud(id);
        return ResponseEntity.ok(respuesta);
    }
}


