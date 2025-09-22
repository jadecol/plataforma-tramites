package com.gestion.tramites.repository;

import com.gestion.tramites.model.EntidadGubernamental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EntidadGubernamentalRepository extends JpaRepository<EntidadGubernamental, Long> {

    List<EntidadGubernamental> findByEstadoVerificacion(EntidadGubernamental.EstadoVerificacion estado);

    Optional<EntidadGubernamental> findByDominioOficial(String dominioOficial);

    Optional<EntidadGubernamental> findByCodigoDane(String codigoDane);

    Optional<EntidadGubernamental> findByNit(String nit);

    Optional<EntidadGubernamental> findByEmailOficial(String emailOficial);

    boolean existsByDominioOficial(String dominioOficial);

    boolean existsByCodigoDane(String codigoDane);

    boolean existsByNit(String nit);

    boolean existsByEmailOficial(String emailOficial);

    List<EntidadGubernamental> findByTipoEntidad(EntidadGubernamental.TipoEntidadGubernamental tipoEntidad);

    List<EntidadGubernamental> findByDepartamento(String departamento);

    List<EntidadGubernamental> findByMunicipio(String municipio);

    List<EntidadGubernamental> findByDepartamentoAndMunicipio(String departamento, String municipio);

    @Query("SELECT e FROM EntidadGubernamental e WHERE e.estadoVerificacion = :estado AND e.activo = true")
    List<EntidadGubernamental> findByEstadoVerificacionAndActivo(@Param("estado") EntidadGubernamental.EstadoVerificacion estado);

    @Query("SELECT e FROM EntidadGubernamental e WHERE e.estadoVerificacion = 'VERIFICADA' AND e.activo = true")
    List<EntidadGubernamental> findEntidadesVerificadasActivas();

    @Query("SELECT e FROM EntidadGubernamental e WHERE e.estadoVerificacion = 'PENDIENTE' ORDER BY e.fechaCreacion ASC")
    List<EntidadGubernamental> findEntidadesPendientesOrdenadas();

    @Query("SELECT e FROM EntidadGubernamental e WHERE e.verificadoPor = :username ORDER BY e.fechaVerificacion DESC")
    List<EntidadGubernamental> findByVerificadoPor(@Param("username") String username);

    @Query("SELECT e FROM EntidadGubernamental e WHERE e.fechaVerificacion BETWEEN :fechaInicio AND :fechaFin")
    List<EntidadGubernamental> findByFechaVerificacionBetween(@Param("fechaInicio") LocalDateTime fechaInicio,
                                                             @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT e FROM EntidadGubernamental e WHERE " +
           "LOWER(e.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(e.dominioOficial) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "e.codigoDane LIKE CONCAT('%', :termino, '%') OR " +
           "e.nit LIKE CONCAT('%', :termino, '%')")
    List<EntidadGubernamental> buscarPorTermino(@Param("termino") String termino);

    @Query("SELECT COUNT(e) FROM EntidadGubernamental e WHERE e.estadoVerificacion = :estado")
    long countByEstadoVerificacion(@Param("estado") EntidadGubernamental.EstadoVerificacion estado);

    @Query("SELECT COUNT(e) FROM EntidadGubernamental e WHERE e.tipoEntidad = :tipo AND e.estadoVerificacion = 'VERIFICADA'")
    long countByTipoEntidadVerificadas(@Param("tipo") EntidadGubernamental.TipoEntidadGubernamental tipo);

    @Query("SELECT e FROM EntidadGubernamental e WHERE e.entidadSistema IS NULL AND e.estadoVerificacion = 'VERIFICADA'")
    List<EntidadGubernamental> findVerificadasSinEntidadSistema();

    @Query("SELECT e FROM EntidadGubernamental e WHERE e.tipoEntidad = :tipo AND e.departamento = :departamento AND e.estadoVerificacion = 'VERIFICADA'")
    List<EntidadGubernamental> findByTipoYDepartamentoVerificadas(@Param("tipo") EntidadGubernamental.TipoEntidadGubernamental tipo,
                                                                  @Param("departamento") String departamento);

    @Query("SELECT DISTINCT e.departamento FROM EntidadGubernamental e WHERE e.estadoVerificacion = 'VERIFICADA' ORDER BY e.departamento")
    List<String> findDepartamentosConEntidadesVerificadas();

    @Query("SELECT DISTINCT e.municipio FROM EntidadGubernamental e WHERE e.departamento = :departamento AND e.estadoVerificacion = 'VERIFICADA' ORDER BY e.municipio")
    List<String> findMunicipiosPorDepartamentoVerificadas(@Param("departamento") String departamento);

    @Query("SELECT e FROM EntidadGubernamental e WHERE e.fechaActualizacion < :fecha AND e.estadoVerificacion = 'PENDIENTE'")
    List<EntidadGubernamental> findEntidadesPendientesAntiguas(@Param("fecha") LocalDateTime fecha);
}