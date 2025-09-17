package com.gestion.tramites.service;

import com.gestion.tramites.exception.ResourceNotFoundException;
import com.gestion.tramites.model.SubtipoTramite; // ¡Debe ser SubtipoTramite!
import com.gestion.tramites.repository.SubtipoTramiteRepository; // ¡Debe ser
                                                                 // SubtipoTramiteRepository!
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SubtipoTramiteService { // ¡Debe ser SubtipoTramiteService!

    @Autowired
    private SubtipoTramiteRepository subtipoTramiteRepository; // ¡Debe ser
                                                               // subtipoTramiteRepository!

    public List<SubtipoTramite> getAllSubtipos() { // ¡Debe ser SubtipoTramite!
        return subtipoTramiteRepository.findAll();
    }

    public SubtipoTramite saveSubtipo(SubtipoTramite subtipo) { // ¡Debe ser SubtipoTramite!
        return subtipoTramiteRepository.save(subtipo);
    }

    public SubtipoTramite getSubtipoById(Long id) { // ¡Debe ser SubtipoTramite!
        return subtipoTramiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subtipo de Trámite", "ID", id)); // Mensaje
                                                                                                   // más
                                                                                                   // específico
    }

    public SubtipoTramite updateSubtipo(Long id, SubtipoTramite detallesSubtipo) { // ¡Debe ser
                                                                                   // SubtipoTramite!
        SubtipoTramite subtipo = getSubtipoById(id); // ¡Debe ser SubtipoTramite!
        subtipo.setNombre(detallesSubtipo.getNombre()); // Asume que solo se puede actualizar el
                                                        // nombre
        return subtipoTramiteRepository.save(subtipo);
    }

    public Map<String, Boolean> deleteSubtipo(Long id) { // ¡Debe ser SubtipoTramite!
        SubtipoTramite subtipo = getSubtipoById(id); // ¡Debe ser SubtipoTramite!
        subtipoTramiteRepository.delete(subtipo);
        Map<String, Boolean> respuesta = new HashMap<>();
        respuesta.put("eliminado", Boolean.TRUE);
        return respuesta;
    }
}
