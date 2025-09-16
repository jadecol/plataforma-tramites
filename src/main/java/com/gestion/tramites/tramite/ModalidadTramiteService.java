package com.gestion.tramites.tramite;

import com.gestion.tramites.excepciones.ResourceNotFoundException;
import com.gestion.tramites.model.ModalidadTramite;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ModalidadTramiteService {

    @Autowired
    private ModalidadTramiteRepository modalidadTramiteRepository;

    public List<ModalidadTramite> getAllModalidades() {
        return modalidadTramiteRepository.findAll();
    }

    public ModalidadTramite saveModalidad(ModalidadTramite modalidad) {
        return modalidadTramiteRepository.save(modalidad);
    }

    public ModalidadTramite getModalidadById(Long id) {
        return modalidadTramiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Modalidad de Trámite", "ID", id)); // Usamos el constructor con 3 args
    }

    public ModalidadTramite updateModalidad(Long id, ModalidadTramite detallesModalidad) {
        ModalidadTramite modalidad = getModalidadById(id);
        modalidad.setNombre(detallesModalidad.getNombre()); // Asume que solo se puede actualizar el nombre
        return modalidadTramiteRepository.save(modalidad);
    }

    public Map<String, Boolean> deleteModalidad(Long id) {
        ModalidadTramite modalidad = getModalidadById(id);
        modalidadTramiteRepository.delete(modalidad);
        Map<String, Boolean> respuesta = new HashMap<>();
        respuesta.put("eliminado", Boolean.TRUE);
        return respuesta;
    }
}
