package com.gestion.tramites.tramite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gestion.tramites.model.Tramite;

@Repository
public interface TramiteRepository extends JpaRepository<Tramite, Long> {
    // ...
}
