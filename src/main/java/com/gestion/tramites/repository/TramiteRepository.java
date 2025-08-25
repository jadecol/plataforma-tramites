package com.gestion.tramites.repository;

import com.gestion.tramites.entidad.Tramite; // Esto debería estar bien si Tramite.java se mantiene en 'entidad'
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TramiteRepository extends JpaRepository<Tramite, Long> {
    // ...
}
