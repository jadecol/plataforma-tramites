package com.gestion.tramites.repository;

import com.gestion.tramites.model.Tramite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TramiteRepository extends JpaRepository<Tramite, Long> {

    // Método básico para consulta pública
    @Query("SELECT t FROM Tramite t WHERE t.numeroRadicacion = :numeroRadicacion")
    Optional<Tramite> findByNumeroRadicacion(@Param("numeroRadicacion") String numeroRadicacion);

    // Por ahora usamos métodos básicos, después agregaremos los multi-tenant
}
