//package com.gestion.tramites.controller;
//
//// import com.gestion.tramites.excepciones.ResourceNotFoundException; // No es necesaria aquí
//import com.gestion.tramites.model.TipoTramite; // CAMBIAR AQUÍ
//import com.gestion.tramites.service.TipoTramiteService; // CAMBIAR AQUÍ
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//// import java.util.HashMap; // No es necesaria aquí
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/v1/tipos") // CAMBIAR LA RUTA BASE
//public class TipoTramiteController { // CAMBIAR AQUÍ
//
//    @Autowired
//    private TipoTramiteService tipoTramiteService; // CAMBIAR AQUÍ
//
//    // Obtener todos los tipos de trámite
//    @GetMapping
//    public List<TipoTramite> listarTiposTramite() { // CAMBIAR AQUÍ
//        return tipoTramiteService.getAllTipos(); // CAMBIAR AQUÍ
//    }
//
//    // Guardar un tipo de trámite
//    @PostMapping
//    public TipoTramite guardarTipoTramite(@RequestBody TipoTramite tipo) { // CAMBIAR AQUÍ
//        return tipoTramiteService.saveTipo(tipo); // CAMBIAR AQUÍ
//    }
//
//    // Obtener tipo por ID
//    @GetMapping("/{id}")
//    public ResponseEntity<TipoTramite> obtenerTipoPorId(@PathVariable Long id) { // CAMBIAR AQUÍ
//        TipoTramite tipo = tipoTramiteService.getTipoById(id); // CAMBIAR AQUÍ
//        return ResponseEntity.ok(tipo);
//    }
//
//    // Actualizar tipo por ID
//    @PutMapping("/{id}")
//    public ResponseEntity<TipoTramite> actualizarTipo(@PathVariable Long id, @RequestBody TipoTramite detallesTipo) { // CAMBIAR AQUÍ
//        TipoTramite tipoActualizado = tipoTramiteService.updateTipo(id, detallesTipo); // CAMBIAR AQUÍ
//        return ResponseEntity.ok(tipoActualizado);
//    }
//
//    // Eliminar tipo por ID
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Map<String, Boolean>> eliminarTipo(@PathVariable Long id) { // CAMBIAR AQUÍ
//        Map<String, Boolean> respuesta = tipoTramiteService.deleteTipo(id); // CAMBIAR AQUÍ
//        return ResponseEntity.ok(respuesta);
//    }
//}
package com.gestion.tramites.controller;

import com.gestion.tramites.model.TipoTramite; // ¡Debe ser TipoTramite!
import com.gestion.tramites.service.TipoTramiteService; // ¡Debe ser TipoTramiteService!

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tipos") // ¡Esta es la ruta para tipos!
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
