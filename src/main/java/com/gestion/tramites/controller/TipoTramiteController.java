package com.gestion.tramites.controller;

import com.gestion.tramites.model.TipoTramite;
import com.gestion.tramites.service.TipoTramiteService;
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
@RequestMapping("/api/v1/tipos")
@Tag(name = "Tipos de Trámite", description = "Gestión de tipos de trámites disponibles")
@SecurityRequirement(name = "Bearer Authentication")
public class TipoTramiteController {

    @Autowired
    private TipoTramiteService tipoTramiteService; // ¡Debe ser tipoTramiteService!

    // Obtener todos los tipos de trámite
    @GetMapping
    public List<TipoTramite> listarTiposTramite() { // ¡Debe ser TipoTramite!
        return tipoTramiteService.getAllTipos();
    }

    // Guardar un tipo de trámite
    @PostMapping
    public TipoTramite guardarTipoTramite(@RequestBody TipoTramite tipo) { // ¡Debe ser TipoTramite!
        return tipoTramiteService.saveTipo(tipo);
    }

    // Obtener tipo por ID
    @GetMapping("/{id}")
    public ResponseEntity<TipoTramite> obtenerTipoPorId(@PathVariable Long id) { // ¡Debe ser TipoTramite!
        TipoTramite tipo = tipoTramiteService.getTipoById(id);
        return ResponseEntity.ok(tipo);
    }

    // Actualizar tipo por ID
    @PutMapping("/{id}")
    public ResponseEntity<TipoTramite> actualizarTipo(@PathVariable Long id, @RequestBody TipoTramite detallesTipo) { // ¡Debe ser TipoTramite!
        TipoTramite tipoActualizado = tipoTramiteService.updateTipo(id, detallesTipo);
        return ResponseEntity.ok(tipoActualizado);
    }

    // Eliminar tipo por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> eliminarTipo(@PathVariable Long id) { // ¡Debe ser TipoTramite!
        Map<String, Boolean> respuesta = tipoTramiteService.deleteTipo(id);
        return ResponseEntity.ok(respuesta);
    }
}
