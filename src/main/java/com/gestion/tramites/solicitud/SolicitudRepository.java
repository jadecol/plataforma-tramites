package com.gestion.tramites.solicitud;

import com.gestion.tramites.model.Solicitud;
import com.gestion.tramites.usuario.Usuario;

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