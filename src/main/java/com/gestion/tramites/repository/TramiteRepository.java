package com.gestion.tramites.repository;

import com.gestion.tramites.model.Tramite;
import com.gestion.tramites.model.Entidad;
import com.gestion.tramites.model.ConsecutivoRadicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TramiteRepository extends JpaRepository<Tramite, Long> {

    // Método básico para consulta pública
    @Query("SELECT t FROM Tramite t WHERE t.numeroRadicacion = :numeroRadicacion")
    Optional<Tramite> findByNumeroRadicacion(@Param("numeroRadicacion") String numeroRadicacion);

    // Métodos para validación de radicación
    @Query("SELECT COUNT(t) FROM Tramite t WHERE t.entidad = :entidad AND YEAR(t.fechaRadicacion) = :ano")
    long countByEntidadAndAno(@Param("entidad") Entidad entidad, @Param("ano") Integer ano);

    // Extraer consecutivos usados por entidad, tipo y año
    @Query("SELECT CAST(SUBSTRING(t.numeroRadicacion, LENGTH(t.numeroRadicacion) - 3) AS INTEGER) " +
           "FROM Tramite t WHERE t.entidad = :entidad " +
           "AND t.numeroRadicacion LIKE CONCAT(t.entidad.codigoDane, '-', :tipoCode, '-', :anoCorto, '-%') " +
           "ORDER BY t.numeroRadicacion")
    List<Integer> findConsecutivosUsadosByEntidadAndTipoAndAno(@Param("entidad") Entidad entidad,
                                                              @Param("tipoCode") String tipoCode,
                                                              @Param("anoCorto") String anoCorto);

    // Detectar consecutivos duplicados
    @Query("SELECT CAST(SUBSTRING(t.numeroRadicacion, LENGTH(t.numeroRadicacion) - 3) AS INTEGER) " +
           "FROM Tramite t WHERE t.entidad = :entidad " +
           "AND t.numeroRadicacion LIKE CONCAT(t.entidad.codigoDane, '-', :tipoCode, '-', :anoCorto, '-%') " +
           "GROUP BY t.numeroRadicacion HAVING COUNT(t.numeroRadicacion) > 1")
    List<Integer> findConsecutivosDuplicadosByEntidadAndTipoAndAno(@Param("entidad") Entidad entidad,
                                                                  @Param("tipoCode") String tipoCode,
                                                                  @Param("anoCorto") String anoCorto);

    // Buscar trámites por entidad (para multi-tenant)
    List<Tramite> findByEntidad(Entidad entidad);

    // Buscar trámites por entidad y año
    @Query("SELECT t FROM Tramite t WHERE t.entidad = :entidad AND YEAR(t.fechaRadicacion) = :ano")
    List<Tramite> findByEntidadAndAno(@Param("entidad") Entidad entidad, @Param("ano") Integer ano);

    // Métodos para consulta pública (Portal Ciudadano)
    List<Tramite> findBySolicitanteCorreoElectronicoOrderByFechaRadicacionDesc(String correoElectronico);

    // Métodos para estadísticas públicas
    @Query("SELECT t FROM Tramite t WHERE t.entidad.id = :entidadId AND t.fechaRadicacion > :fechaLimite ORDER BY t.fechaRadicacion DESC")
    List<Tramite> findByEntidadIdAndFechaRadicacionAfterOrderByFechaRadicacionDesc(@Param("entidadId") Long entidadId, @Param("fechaLimite") LocalDate fechaLimite);

    @Query("SELECT COUNT(t) FROM Tramite t WHERE t.entidad.id = :entidadId AND t.fechaRadicacion > :fechaLimite")
    long countByEntidadIdAndFechaRadicacionAfter(@Param("entidadId") Long entidadId, @Param("fechaLimite") LocalDate fechaLimite);

    @Query("SELECT t FROM Tramite t WHERE t.entidad.id = :entidadId AND t.fechaRadicacion > :fechaLimite AND t.estadoActual IN :estados")
    List<Tramite> findByEntidadIdAndFechaRadicacionAfterAndEstadoActualIn(@Param("entidadId") Long entidadId, @Param("fechaLimite") LocalDate fechaLimite, @Param("estados") List<Tramite.EstadoTramite> estados);

    // Por ahora usamos métodos básicos, después agregaremos más multi-tenant según necesidades
}
