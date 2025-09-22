package com.gestion.tramites.controller;

import com.gestion.tramites.model.ModalidadTramite;
import com.gestion.tramites.service.ModalidadTramiteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/modalidades-tramite")
@Tag(name = "Modalidades de Trámite", description = "Gestión de modalidades de trámites")
@SecurityRequirement(name = "Bearer Authentication")
public class ModalidadTramiteController {

    @Autowired
    private ModalidadTramiteService modalidadTramiteService;

    @GetMapping
    public List<ModalidadTramite> listarModalidadesTramite() {
        return modalidadTramiteService.getAllModalidades();
    }

    @PostMapping
    public ModalidadTramite guardarModalidadTramite(@RequestBody ModalidadTramite modalidad) {
        return modalidadTramiteService.saveModalidad(modalidad);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModalidadTramite> obtenerModalidadPorId(@PathVariable Long id) {
        ModalidadTramite modalidad = modalidadTramiteService.getModalidadById(id);
        return ResponseEntity.ok(modalidad);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModalidadTramite> actualizarModalidad(@PathVariable Long id, @RequestBody ModalidadTramite detallesModalidad) {
        ModalidadTramite modalidadActualizada = modalidadTramiteService.updateModalidad(id, detallesModalidad);
        return ResponseEntity.ok(modalidadActualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> eliminarModalidad(@PathVariable Long id) {
        Map<String, Boolean> respuesta = modalidadTramiteService.deleteModalidad(id);
        return ResponseEntity.ok(respuesta);
    }
}