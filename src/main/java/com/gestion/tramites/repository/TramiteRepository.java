//package com.gestion.tramites.repository;
//
//import com.gestion.tramites.entidad.Tramite;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//@Repository
//public interface TramiteRepository extends JpaRepository<Tramite, Long> {
//    // Puedes añadir métodos de consulta personalizados aquí si los necesitas en el futuro,
//    // por ejemplo, List<Tramite> findBySolicitanteIdUsuario(Long idUsuario);
//}
package com.gestion.tramites.repository;

import com.gestion.tramites.entidad.Tramite; // Esto debería estar bien si Tramite.java se mantiene en 'entidad'
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TramiteRepository extends JpaRepository<Tramite, Long> {
    // ...
}
