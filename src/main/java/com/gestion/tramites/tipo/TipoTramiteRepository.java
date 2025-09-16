package com.gestion.tramites.tipo;

import com.gestion.tramites.model.TipoTramite; // ¡Debe ser TipoTramite!
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoTramiteRepository extends JpaRepository<TipoTramite, Long> {
    // Métodos personalizados si son necesarios
}