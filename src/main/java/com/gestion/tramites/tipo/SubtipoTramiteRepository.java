package com.gestion.tramites.tipo;

import com.gestion.tramites.model.SubtipoTramite; // ¡Debe ser SubtipoTramite!
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubtipoTramiteRepository extends JpaRepository<SubtipoTramite, Long> {
    // Puedes añadir métodos personalizados si los necesitas
}