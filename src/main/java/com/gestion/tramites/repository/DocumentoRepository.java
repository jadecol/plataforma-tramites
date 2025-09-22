package com.gestion.tramites.repository;

import com.gestion.tramites.model.Documento;
import com.gestion.tramites.model.Tramite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    /**
     * Buscar documentos por ID de trámite
     */
    @Query("SELECT d FROM Documento d WHERE d.tramite.idTramite = :tramiteId AND d.estado = 'ACTIVO'")
    List<Documento> findByTramiteIdAndEstadoActivo(@Param("tramiteId") Long tramiteId);

    /**
     * Buscar documentos activos por trámite
     */
    List<Documento> findByTramiteAndEstado(Tramite tramite, Documento.EstadoDocumento estado);

    /**
     * Buscar documentos por tipo de documento y trámite
     */
    @Query("SELECT d FROM Documento d WHERE d.tramite.idTramite = :tramiteId " +
           "AND d.tipoDocumento = :tipoDocumento AND d.estado = 'ACTIVO'")
    List<Documento> findByTramiteIdAndTipoDocumentoAndEstadoActivo(
            @Param("tramiteId") Long tramiteId,
            @Param("tipoDocumento") Documento.TipoDocumento tipoDocumento);

    /**
     * Buscar versiones de un documento
     */
    @Query("SELECT d FROM Documento d WHERE d.documentoPadre.idDocumento = :documentoPadreId " +
           "OR d.idDocumento = :documentoPadreId ORDER BY d.version DESC")
    List<Documento> findVersionesByDocumentoPadreId(@Param("documentoPadreId") Long documentoPadreId);

    /**
     * Buscar versión actual de un documento
     */
    @Query("SELECT d FROM Documento d WHERE (d.documentoPadre.idDocumento = :documentoId " +
           "OR d.idDocumento = :documentoId) AND d.esVersionActual = true AND d.estado = 'ACTIVO'")
    Optional<Documento> findVersionActualByDocumentoId(@Param("documentoId") Long documentoId);

    /**
     * Buscar documentos por hash para detectar duplicados
     */
    @Query("SELECT d FROM Documento d WHERE d.hashArchivo = :hash AND d.estado = 'ACTIVO'")
    List<Documento> findByHashArchivoAndEstadoActivo(@Param("hash") String hash);

    /**
     * Buscar documentos por usuario que los subió
     */
    @Query("SELECT d FROM Documento d WHERE d.usuarioSubida.id = :usuarioId " +
           "AND d.estado = 'ACTIVO' ORDER BY d.fechaSubida DESC")
    List<Documento> findByUsuarioSubidaIdAndEstadoActivoOrderByFechaSubidaDesc(@Param("usuarioId") Long usuarioId);

    /**
     * Contar documentos por trámite
     */
    @Query("SELECT COUNT(d) FROM Documento d WHERE d.tramite.idTramite = :tramiteId AND d.estado = 'ACTIVO'")
    Long countByTramiteIdAndEstadoActivo(@Param("tramiteId") Long tramiteId);

    /**
     * Obtener tamaño total de documentos por trámite
     */
    @Query("SELECT COALESCE(SUM(d.tamanoBytes), 0) FROM Documento d " +
           "WHERE d.tramite.idTramite = :tramiteId AND d.estado = 'ACTIVO'")
    Long sumTamanoByTramiteIdAndEstadoActivo(@Param("tramiteId") Long tramiteId);

    /**
     * Buscar documentos por nombre original (búsqueda parcial)
     */
    @Query("SELECT d FROM Documento d WHERE d.tramite.idTramite = :tramiteId " +
           "AND LOWER(d.nombreOriginal) LIKE LOWER(CONCAT('%', :nombre, '%')) " +
           "AND d.estado = 'ACTIVO' ORDER BY d.fechaSubida DESC")
    List<Documento> findByTramiteIdAndNombreContainingIgnoreCaseAndEstadoActivo(
            @Param("tramiteId") Long tramiteId,
            @Param("nombre") String nombre);

    /**
     * Buscar documentos próximos a ser archivados por nueva versión
     */
    @Query("SELECT d FROM Documento d WHERE d.tramite.idTramite = :tramiteId " +
           "AND d.esVersionActual = false AND d.estado = 'ACTIVO'")
    List<Documento> findVersionesAnterioresByTramiteId(@Param("tramiteId") Long tramiteId);

    /**
     * Buscar documentos por extensión
     */
    @Query("SELECT d FROM Documento d WHERE d.tramite.idTramite = :tramiteId " +
           "AND d.extension = :extension AND d.estado = 'ACTIVO'")
    List<Documento> findByTramiteIdAndExtensionAndEstadoActivo(
            @Param("tramiteId") Long tramiteId,
            @Param("extension") String extension);

    /**
     * Verificar si existe documento con el mismo nombre en el trámite
     */
    @Query("SELECT COUNT(d) > 0 FROM Documento d WHERE d.tramite.idTramite = :tramiteId " +
           "AND d.nombreOriginal = :nombreOriginal AND d.estado = 'ACTIVO' " +
           "AND (:documentoId IS NULL OR d.idDocumento != :documentoId)")
    boolean existsByTramiteIdAndNombreOriginalAndEstadoActivo(
            @Param("tramiteId") Long tramiteId,
            @Param("nombreOriginal") String nombreOriginal,
            @Param("documentoId") Long documentoId);
}