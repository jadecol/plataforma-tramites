package com.gestion.tramites.service;

import com.gestion.tramites.model.Documento;
import com.gestion.tramites.model.Tramite;
import com.gestion.tramites.model.Usuario;
import com.gestion.tramites.repository.DocumentoRepository;
import com.gestion.tramites.repository.TramiteRepository;
import com.gestion.tramites.repository.UsuarioRepository;
import com.gestion.tramites.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DocumentoService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentoService.class);

    @Autowired
    private DocumentoRepository documentoRepository;

    @Autowired
    private TramiteRepository tramiteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DocumentoStorageService storageService;

    /**
     * Subir un nuevo documento a un trámite
     */
    public Documento subirDocumento(Long tramiteId, MultipartFile archivo,
                                  Documento.TipoDocumento tipoDocumento, String descripcion) throws IOException {
        // Validar acceso al trámite
        Tramite tramite = validarAccesoTramite(tramiteId);
        CustomUserDetails currentUser = getCurrentUser();

        // Validar archivo
        storageService.validarArchivo(archivo);

        // Validar límites por trámite
        Long cantidadActual = documentoRepository.countByTramiteIdAndEstadoActivo(tramiteId);
        Long tamanoTotalActual = documentoRepository.sumTamanoByTramiteIdAndEstadoActivo(tramiteId);
        storageService.validarLimitesPorTramite(cantidadActual.intValue(), tamanoTotalActual, archivo.getSize());

        // Calcular hash del archivo
        String hashArchivo = storageService.calcularHashArchivo(archivo);

        // Verificar si ya existe un archivo idéntico
        List<Documento> documentosConMismoHash = documentoRepository.findByHashArchivoAndEstadoActivo(hashArchivo);
        if (!documentosConMismoHash.isEmpty()) {
            Documento documentoExistente = documentosConMismoHash.get(0);
            if (documentoExistente.getTramite().getIdTramite().equals(tramiteId)) {
                throw new IllegalArgumentException(
                    "Ya existe un archivo idéntico en este trámite: " + documentoExistente.getNombreOriginal());
            }
        }

        // Almacenar archivo físicamente
        String rutaArchivo = storageService.almacenarArchivo(archivo, tramiteId);

        // Obtener usuario actual
        Usuario usuarioSubida = usuarioRepository.findById(currentUser.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", currentUser.getIdUsuario()));

        // Determinar tipo MIME
        String tipoMime = archivo.getContentType();
        if (tipoMime == null || tipoMime.isEmpty()) {
            tipoMime = storageService.determinarTipoMime(archivo.getOriginalFilename());
        }

        // Crear entidad Documento
        String extension = obtenerExtension(archivo.getOriginalFilename());
        Documento documento = new Documento(
            archivo.getOriginalFilename(),
            tipoMime,
            extension,
            archivo.getSize(),
            rutaArchivo,
            hashArchivo,
            tipoDocumento,
            tramite,
            usuarioSubida
        );

        documento.setDescripcion(descripcion);

        // Verificar si es una nueva versión de un documento existente
        manejarVersionado(documento, tramite);

        Documento documentoGuardado = documentoRepository.save(documento);
        logger.info("Documento subido exitosamente: {} para trámite: {}",
                   documento.getNombreOriginal(), tramiteId);

        return documentoGuardado;
    }

    /**
     * Descargar un documento
     */
    @Transactional(readOnly = true)
    public Resource descargarDocumento(Long documentoId) {
        Documento documento = validarAccesoDocumento(documentoId);

        if (!documento.esActivo()) {
            throw new IllegalStateException("El documento no está disponible para descarga");
        }

        // Actualizar fecha de último acceso
        documento.setFechaUltimoAcceso(java.time.LocalDateTime.now());
        documentoRepository.save(documento);

        return storageService.cargarArchivo(documento.getRutaArchivo());
    }

    /**
     * Obtener documentos de un trámite
     */
    @Transactional(readOnly = true)
    public List<Documento> obtenerDocumentosPorTramite(Long tramiteId) {
        validarAccesoTramite(tramiteId);
        return documentoRepository.findByTramiteIdAndEstadoActivo(tramiteId);
    }

    /**
     * Obtener documentos por tipo
     */
    @Transactional(readOnly = true)
    public List<Documento> obtenerDocumentosPorTipoYTramite(Long tramiteId, Documento.TipoDocumento tipoDocumento) {
        validarAccesoTramite(tramiteId);
        return documentoRepository.findByTramiteIdAndTipoDocumentoAndEstadoActivo(tramiteId, tipoDocumento);
    }

    /**
     * Obtener información de un documento
     */
    @Transactional(readOnly = true)
    public Documento obtenerDocumento(Long documentoId) {
        return validarAccesoDocumento(documentoId);
    }

    /**
     * Eliminar un documento (marcado como eliminado)
     */
    public void eliminarDocumento(Long documentoId) {
        Documento documento = validarAccesoDocumento(documentoId);
        CustomUserDetails currentUser = getCurrentUser();

        // Solo el usuario que subió el documento, revisores o admins pueden eliminarlo
        if (!documento.getUsuarioSubida().getId().equals(currentUser.getIdUsuario()) &&
            !currentUser.isAdminGlobal() && !currentUser.isAdminEntidad() && !currentUser.isRevisor()) {
            throw new IllegalStateException("No tiene permisos para eliminar este documento");
        }

        documento.marcarComoEliminado();
        documentoRepository.save(documento);

        logger.info("Documento marcado como eliminado: {} por usuario: {}",
                   documento.getNombreOriginal(), currentUser.getUsername());
    }

    /**
     * Buscar documentos por nombre
     */
    @Transactional(readOnly = true)
    public List<Documento> buscarDocumentosPorNombre(Long tramiteId, String nombre) {
        validarAccesoTramite(tramiteId);
        return documentoRepository.findByTramiteIdAndNombreContainingIgnoreCaseAndEstadoActivo(tramiteId, nombre);
    }

    /**
     * Obtener versiones de un documento
     */
    @Transactional(readOnly = true)
    public List<Documento> obtenerVersionesDocumento(Long documentoId) {
        Documento documento = validarAccesoDocumento(documentoId);
        Long documentoPadreId = documento.getDocumentoPadre() != null ?
                               documento.getDocumentoPadre().getIdDocumento() : documentoId;

        return documentoRepository.findVersionesByDocumentoPadreId(documentoPadreId);
    }

    /**
     * Obtener estadísticas de documentos por trámite
     */
    @Transactional(readOnly = true)
    public EstadisticasDocumentos obtenerEstadisticas(Long tramiteId) {
        validarAccesoTramite(tramiteId);

        Long cantidad = documentoRepository.countByTramiteIdAndEstadoActivo(tramiteId);
        Long tamanoTotal = documentoRepository.sumTamanoByTramiteIdAndEstadoActivo(tramiteId);

        return new EstadisticasDocumentos(cantidad, tamanoTotal);
    }

    private Tramite validarAccesoTramite(Long tramiteId) {
        CustomUserDetails currentUser = getCurrentUser();

        Tramite tramite;
        if (currentUser.isAdminGlobal()) {
            tramite = tramiteRepository.findById(tramiteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tramite", "id", tramiteId));
        } else {
            // Para usuarios no admin global, el filtro multi-tenant se aplica automáticamente
            tramite = tramiteRepository.findById(tramiteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tramite", "id", tramiteId));
        }

        return tramite;
    }

    private Documento validarAccesoDocumento(Long documentoId) {
        Documento documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", "id", documentoId));

        // Validar acceso al trámite asociado (incluye validación multi-tenant)
        validarAccesoTramite(documento.getTramite().getIdTramite());

        return documento;
    }

    private void manejarVersionado(Documento nuevoDocumento, Tramite tramite) {
        // Buscar documentos existentes con el mismo nombre original y tipo
        List<Documento> documentosExistentes = documentoRepository
                .findByTramiteIdAndTipoDocumentoAndEstadoActivo(
                    tramite.getIdTramite(),
                    nuevoDocumento.getTipoDocumento()
                );

        Optional<Documento> documentoMismoNombre = documentosExistentes.stream()
                .filter(d -> d.getNombreOriginal().equals(nuevoDocumento.getNombreOriginal()))
                .filter(Documento::esVersionActual)
                .findFirst();

        if (documentoMismoNombre.isPresent()) {
            Documento documentoAnterior = documentoMismoNombre.get();

            // Archivar versión anterior
            documentoAnterior.archivarPorNuevaVersion();
            documentoRepository.save(documentoAnterior);

            // Configurar nueva versión
            nuevoDocumento.setDocumentoPadre(
                documentoAnterior.getDocumentoPadre() != null ?
                documentoAnterior.getDocumentoPadre() : documentoAnterior
            );
            nuevoDocumento.setVersion(documentoAnterior.getVersion() + 1);
            nuevoDocumento.setEsVersionActual(true);

            logger.info("Creando nueva versión {} del documento: {}",
                       nuevoDocumento.getVersion(), nuevoDocumento.getNombreOriginal());
        }
    }

    private CustomUserDetails getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            throw new IllegalStateException("Usuario no autenticado correctamente");
        }
        return (CustomUserDetails) principal;
    }

    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) {
            return "";
        }
        return nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1).toLowerCase();
    }

    public static class EstadisticasDocumentos {
        private final Long cantidadDocumentos;
        private final Long tamanoTotal;

        public EstadisticasDocumentos(Long cantidadDocumentos, Long tamanoTotal) {
            this.cantidadDocumentos = cantidadDocumentos != null ? cantidadDocumentos : 0L;
            this.tamanoTotal = tamanoTotal != null ? tamanoTotal : 0L;
        }

        public Long getCantidadDocumentos() { return cantidadDocumentos; }
        public Long getTamanoTotal() { return tamanoTotal; }

        public String getTamanoTotalLegible() {
            if (tamanoTotal == null || tamanoTotal == 0) return "0 B";

            long bytes = tamanoTotal;
            if (bytes < 1024) return bytes + " B";
            if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
            if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}