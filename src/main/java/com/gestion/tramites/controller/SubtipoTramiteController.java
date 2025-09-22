package com.gestion.tramites.controller;

import com.gestion.tramites.model.SubtipoTramite;
import com.gestion.tramites.service.SubtipoTramiteService;
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
@RequestMapping("/api/v1/subtipos")
@Tag(name = "Subtipos de Trámite", description = "Gestión de subtipos de trámites")
@SecurityRequirement(name = "Bearer Authentication")
public class SubtipoTramiteController {

    @Autowired
    private SubtipoTramiteService subtipoTramiteService; // ¡Debe ser subtipoTramiteService!

    // Obtener todos los subtipos de trámite
    @GetMapping
    public List<SubtipoTramite> listarSubtiposTramite() { // ¡Debe ser SubtipoTramite!
        return subtipoTramiteService.getAllSubtipos();
    }

    // Guardar un subtipo de trámite
    @PostMapping
    public SubtipoTramite guardarSubtipoTramite(@RequestBody SubtipoTramite subtipo) { // ¡Debe ser SubtipoTramite!
        return subtipoTramiteService.saveSubtipo(subtipo);
    }

    // Obtener subtipo por ID
    @GetMapping("/{id}")
    public ResponseEntity<SubtipoTramite> obtenerSubtipoPorId(@PathVariable Long id) { // ¡Debe ser SubtipoTramite!
        SubtipoTramite subtipo = subtipoTramiteService.getSubtipoById(id);
        return ResponseEntity.ok(subtipo);
    }

    // Actualizar subtipo por ID
    @PutMapping("/{id}")
    public ResponseEntity<SubtipoTramite> actualizarSubtipo(@PathVariable Long id, @RequestBody SubtipoTramite detallesSubtipo) { // ¡Debe ser SubtipoTramite!
        SubtipoTramite subtipoActualizado = subtipoTramiteService.updateSubtipo(id, detallesSubtipo);
        return ResponseEntity.ok(subtipoActualizado);
    }

    // Eliminar subtipo por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> eliminarSubtipo(@PathVariable Long id) { // ¡Debe ser SubtipoTramite!
        Map<String, Boolean> respuesta = subtipoTramiteService.deleteSubtipo(id);
        return ResponseEntity.ok(respuesta);
    }
}
