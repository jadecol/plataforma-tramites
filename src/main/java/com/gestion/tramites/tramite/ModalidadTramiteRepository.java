package com.gestion.tramites.tramite;

import com.gestion.tramites.model.ModalidadTramite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModalidadTramiteRepository extends JpaRepository<ModalidadTramite, Long> {
    // Puedes añadir métodos personalizados si los necesitas
}


