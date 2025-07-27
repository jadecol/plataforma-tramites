//package com.gestion.tramites.repository;
//
//import com.gestion.tramites.model.SubtipoTramite;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//@Repository
//public interface SubtipoTramiteRepository extends JpaRepository<SubtipoTramite, Long> {} // ID de la entidad es Long
package com.gestion.tramites.repository;

import com.gestion.tramites.model.SubtipoTramite; // ¡Debe ser SubtipoTramite!
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubtipoTramiteRepository extends JpaRepository<SubtipoTramite, Long> {
    // Puedes añadir métodos personalizados si los necesitas
}