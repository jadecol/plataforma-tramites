package com.gestion.tramites.repository;

import com.gestion.tramites.model.Entidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // <--- Importa Optional si aún no lo está

@Repository
public interface EntidadRepository extends JpaRepository<Entidad, Long> {
    // Método para buscar una entidad por su NIT
    Optional<Entidad> findByNit(String nit); // <--- ¡AÑADE ESTA LÍNEA!
    // Asegúrate de que el nombre del campo en tu Entidad es 'nit' (es lo que definimos).
}
