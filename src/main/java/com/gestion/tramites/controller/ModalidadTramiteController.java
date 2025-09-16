package com.gestion.tramites.controller;

import com.gestion.tramites.model.ModalidadTramite;
import com.gestion.tramites.tramite.ModalidadTramiteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/modalidades-tramite") // <--- ¡CAMBIADO AQUÍ!
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