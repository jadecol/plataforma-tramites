package com.gestion.tramites.tipo;

import com.gestion.tramites.excepciones.ResourceNotFoundException;
import com.gestion.tramites.model.TipoTramite; // ¡Debe ser TipoTramite!

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TipoTramiteService {

    @Autowired
    private TipoTramiteRepository tipoTramiteRepository; // ¡Debe ser tipoTramiteRepository!

    public List<TipoTramite> getAllTipos() { // ¡Debe ser TipoTramite!
        return tipoTramiteRepository.findAll();
    }

    public TipoTramite saveTipo(TipoTramite tipo) { // ¡Debe ser TipoTramite!
        return tipoTramiteRepository.save(tipo);
    }

    public TipoTramite getTipoById(Long id) { // ¡Debe ser TipoTramite!
        return tipoTramiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de Trámite", "ID", id));
    }

    public TipoTramite updateTipo(Long id, TipoTramite detallesTipo) { // ¡Debe ser TipoTramite!
        TipoTramite tipo = getTipoById(id); // ¡Debe ser TipoTramite!
        tipo.setNombre(detallesTipo.getNombre());
        return tipoTramiteRepository.save(tipo);
    }

    public Map<String, Boolean> deleteTipo(Long id) { // ¡Debe ser TipoTramite!
        TipoTramite tipo = getTipoById(id); // ¡Debe ser TipoTramite!
        tipoTramiteRepository.delete(tipo);
        Map<String, Boolean> respuesta = new HashMap<>();
        respuesta.put("eliminado", Boolean.TRUE);
        return respuesta;
    }
}
