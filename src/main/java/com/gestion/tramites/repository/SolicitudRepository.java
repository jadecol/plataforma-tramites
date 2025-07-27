//package com.gestion.tramites.repository;
//
//import com.gestion.tramites.model.Solicitud;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//@Repository
//public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {
//    // Puedes añadir métodos personalizados si los necesitas,
//    // por ejemplo, para buscar por número de radicación
//    // Optional<Solicitud> findByNumeroRadicacion(String numeroRadicacion);
//}
package com.gestion.tramites.repository;

import com.gestion.tramites.model.Solicitud;
import com.gestion.tramites.model.Usuario; // Importa Usuario si necesitas filtrar por él
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {
    // Métodos de consulta personalizados si son necesarios
    List<Solicitud> findBySolicitante(Usuario solicitante);
    List<Solicitud> findByRevisorAsignado(Usuario revisor);
    List<Solicitud> findByEstadoActual(String estado);
    Optional<Solicitud> findByNumeroRadicacion(String numeroRadicacion);
}