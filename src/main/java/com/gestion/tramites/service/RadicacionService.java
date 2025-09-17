package com.gestion.tramites.service;

import com.gestion.tramites.model.Entidad;
import com.gestion.tramites.model.TipoTramite;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class RadicacionService {

    public String generarNumeroRadicacion(Entidad entidad, TipoTramite tipoTramite) {
        String year = String.valueOf(LocalDate.now().getYear());
        String codigoDane = "11001"; // Por defecto Bogotá
        String idEntidad = String.format("%d", entidad.getId());
        String consecutivo = "00001"; // Simplificado por ahora
        
        return String.format("%s-%s-%s-%s", codigoDane, idEntidad, year, consecutivo);
    }

    public boolean validarNumeroRadicacion(String numeroRadicacion) {
        if (numeroRadicacion == null || numeroRadicacion.trim().isEmpty()) {
            return false;
        }
        String pattern = "^\\d{5}-\\d+-\\d{4}-\\d{5}$";
        return numeroRadicacion.matches(pattern);
    }
}