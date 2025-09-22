package com.gestion.tramites.repository;

import com.gestion.tramites.model.ConsecutivoRadicacion;
import com.gestion.tramites.model.Entidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsecutivoRadicacionRepository extends JpaRepository<ConsecutivoRadicacion, Long> {

    /**
     * Busca el consecutivo para una entidad, tipo y año específicos
     * Usa lock pessimista para evitar condiciones de carrera en generación de consecutivos
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ConsecutivoRadicacion c WHERE c.entidad = :entidad AND c.tipoEntidad = :tipoEntidad AND c.ano = :ano AND c.activo = true")
    Optional<ConsecutivoRadicacion> findByEntidadAndTipoAndAnoWithLock(@Param("entidad") Entidad entidad,
                                                                      @Param("tipoEntidad") ConsecutivoRadicacion.TipoEntidadRadicacion tipoEntidad,
                                                                      @Param("ano") Integer ano);

    /**
     * Busca el consecutivo sin lock para consultas de solo lectura
     */
    Optional<ConsecutivoRadicacion> findByEntidadAndTipoEntidadAndAnoAndActivoTrue(Entidad entidad,
                                                                                   ConsecutivoRadicacion.TipoEntidadRadicacion tipoEntidad,
                                                                                   Integer ano);

    /**
     * Busca todos los consecutivos de una entidad para un año específico
     */
    List<ConsecutivoRadicacion> findByEntidadAndAnoAndActivoTrueOrderByTipoEntidad(Entidad entidad, Integer ano);

    /**
     * Busca consecutivos por código DANE y año
     */
    List<ConsecutivoRadicacion> findByCodigoDaneAndAnoAndActivoTrueOrderByTipoEntidad(String codigoDane, Integer ano);

    /**
     * Busca el último consecutivo usado por entidad y tipo
     */
    @Query("SELECT c.ultimoConsecutivo FROM ConsecutivoRadicacion c WHERE c.entidad = :entidad AND c.tipoEntidad = :tipoEntidad AND c.ano = :ano AND c.activo = true")
    Optional<Integer> findUltimoConsecutivoByEntidadAndTipoAndAno(@Param("entidad") Entidad entidad,
                                                                 @Param("tipoEntidad") ConsecutivoRadicacion.TipoEntidadRadicacion tipoEntidad,
                                                                 @Param("ano") Integer ano);

    /**
     * Incrementa atomicamente el consecutivo
     */
    @Modifying
    @Query("UPDATE ConsecutivoRadicacion c SET c.ultimoConsecutivo = c.ultimoConsecutivo + 1, c.fechaActualizacion = CURRENT_TIMESTAMP WHERE c.id = :id")
    int incrementarConsecutivo(@Param("id") Long id);

    /**
     * Verifica si existe un consecutivo para los parámetros dados
     */
    boolean existsByEntidadAndTipoEntidadAndAnoAndActivoTrue(Entidad entidad,
                                                             ConsecutivoRadicacion.TipoEntidadRadicacion tipoEntidad,
                                                             Integer ano);

    /**
     * Busca consecutivos por rango de años para análisis histórico
     */
    @Query("SELECT c FROM ConsecutivoRadicacion c WHERE c.entidad = :entidad AND c.ano BETWEEN :anoInicio AND :anoFin AND c.activo = true ORDER BY c.ano DESC, c.tipoEntidad")
    List<ConsecutivoRadicacion> findByEntidadAndAnoRangeAndActivoTrue(@Param("entidad") Entidad entidad,
                                                                      @Param("anoInicio") Integer anoInicio,
                                                                      @Param("anoFin") Integer anoFin);

    /**
     * Obtiene estadísticas de consecutivos por entidad y año
     */
    @Query("SELECT c.tipoEntidad as tipo, c.ultimoConsecutivo as total, c.ano as ano " +
           "FROM ConsecutivoRadicacion c " +
           "WHERE c.entidad = :entidad AND c.ano = :ano AND c.activo = true")
    List<ConsecutivoEstadistica> findEstadisticasByEntidadAndAno(@Param("entidad") Entidad entidad,
                                                                 @Param("ano") Integer ano);

    /**
     * Busca consecutivos que necesitan migración de año (para proceso automático de cambio de año)
     */
    @Query("SELECT DISTINCT c.entidad FROM ConsecutivoRadicacion c WHERE c.ano = :anoAnterior AND c.activo = true")
    List<Entidad> findEntidadesParaMigracionAno(@Param("anoAnterior") Integer anoAnterior);

    /**
     * Desactiva consecutivos de años anteriores (proceso de limpieza)
     */
    @Modifying
    @Query("UPDATE ConsecutivoRadicacion c SET c.activo = false, c.fechaActualizacion = CURRENT_TIMESTAMP WHERE c.ano < :anoMinimo")
    int desactivarConsecutivosAntiguos(@Param("anoMinimo") Integer anoMinimo);

    /**
     * Valida integridad de consecutivos (no debe haber saltos)
     */
    @Query("SELECT c FROM ConsecutivoRadicacion c WHERE c.entidad = :entidad AND c.tipoEntidad = :tipoEntidad AND c.ano = :ano AND c.activo = true")
    Optional<ConsecutivoRadicacion> findForValidacion(@Param("entidad") Entidad entidad,
                                                      @Param("tipoEntidad") ConsecutivoRadicacion.TipoEntidadRadicacion tipoEntidad,
                                                      @Param("ano") Integer ano);

    /**
     * Método para compatibility con tests legacy
     */
    @Query("SELECT c.ultimoConsecutivo + 1 FROM ConsecutivoRadicacion c WHERE c.entidad.id = :entidadId AND c.ano = :ano AND c.activo = true")
    Optional<Integer> obtenerSiguienteConsecutivo(@Param("entidadId") Long entidadId, @Param("ano") Integer ano);

    /**
     * Interface para proyección de estadísticas
     */
    interface ConsecutivoEstadistica {
        ConsecutivoRadicacion.TipoEntidadRadicacion getTipo();
        Integer getTotal();
        Integer getAno();
    }
}