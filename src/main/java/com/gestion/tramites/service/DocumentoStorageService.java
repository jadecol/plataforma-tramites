package com.gestion.tramites.service;

import com.gestion.tramites.config.DocumentStorageConfig;
import com.gestion.tramites.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Year;
import java.util.List;

@Service
public class DocumentoStorageService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentoStorageService.class);

    @Autowired
    private DocumentStorageConfig storageConfig;

    private static final List<String> TIPOS_MIME_PERMITIDOS = List.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/zip",
            "application/x-rar-compressed",
            "application/dwg",
            "application/dxf"
    );

    /**
     * Almacenar un archivo en el sistema de archivos
     */
    public String almacenarArchivo(MultipartFile archivo, Long tramiteId) throws IOException {
        validarArchivo(archivo);

        String nombreArchivo = generarNombreUnico(archivo.getOriginalFilename());
        String rutaRelativa = generarRutaArchivo(tramiteId, nombreArchivo);
        Path rutaCompleta = Paths.get(storageConfig.getStoragePath()).resolve(rutaRelativa);

        // Crear directorios padre si no existen
        Files.createDirectories(rutaCompleta.getParent());

        // Copiar archivo
        try (InputStream inputStream = archivo.getInputStream()) {
            Files.copy(inputStream, rutaCompleta, StandardCopyOption.REPLACE_EXISTING);
        }

        logger.info("Archivo almacenado exitosamente: {}", rutaCompleta);
        return rutaRelativa;
    }

    /**
     * Cargar un archivo como Resource
     */
    public Resource cargarArchivo(String rutaArchivo) {
        try {
            Path archivo = Paths.get(storageConfig.getStoragePath()).resolve(rutaArchivo);
            Resource resource = new UrlResource(archivo.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("Archivo", "ruta", rutaArchivo);
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Archivo", "ruta", rutaArchivo);
        }
    }

    /**
     * Eliminar un archivo del sistema de archivos
     */
    public boolean eliminarArchivo(String rutaArchivo) {
        try {
            Path archivo = Paths.get(storageConfig.getStoragePath()).resolve(rutaArchivo);
            boolean eliminado = Files.deleteIfExists(archivo);

            if (eliminado) {
                logger.info("Archivo eliminado exitosamente: {}", archivo);
            } else {
                logger.warn("El archivo no existe o no se pudo eliminar: {}", archivo);
            }

            return eliminado;
        } catch (IOException e) {
            logger.error("Error eliminando archivo: {}", rutaArchivo, e);
            return false;
        }
    }

    /**
     * Verificar si un archivo existe
     */
    public boolean existeArchivo(String rutaArchivo) {
        Path archivo = Paths.get(storageConfig.getStoragePath()).resolve(rutaArchivo);
        return Files.exists(archivo);
    }

    /**
     * Obtener el tamaño de un archivo
     */
    public long obtenerTamanoArchivo(String rutaArchivo) throws IOException {
        Path archivo = Paths.get(storageConfig.getStoragePath()).resolve(rutaArchivo);
        return Files.size(archivo);
    }

    /**
     * Calcular hash SHA-256 de un archivo
     */
    public String calcularHashArchivo(MultipartFile archivo) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream inputStream = archivo.getInputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }

            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error calculando hash del archivo", e);
        }
    }

    /**
     * Validar que el archivo cumple con las restricciones
     */
    public void validarArchivo(MultipartFile archivo) {
        if (archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        if (archivo.getOriginalFilename() == null || archivo.getOriginalFilename().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del archivo es requerido");
        }

        // Validar extensión
        String extension = obtenerExtension(archivo.getOriginalFilename()).toLowerCase();
        if (!storageConfig.getAllowedExtensions().contains(extension)) {
            throw new IllegalArgumentException(
                String.format("Extensión de archivo no permitida: %s. Extensiones permitidas: %s",
                              extension, String.join(", ", storageConfig.getAllowedExtensions())));
        }

        // Validar tipo MIME
        String tipoMime = archivo.getContentType();
        if (tipoMime != null && !TIPOS_MIME_PERMITIDOS.contains(tipoMime)) {
            logger.warn("Tipo MIME no reconocido pero extensión válida: {} para archivo: {}",
                       tipoMime, archivo.getOriginalFilename());
        }

        // Validar tamaño de archivo individual (50MB por defecto)
        long maxSizeBytes = 50L * 1024 * 1024; // 50MB
        if (archivo.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException(
                String.format("El archivo excede el tamaño máximo permitido de %d MB",
                              maxSizeBytes / (1024 * 1024)));
        }
    }

    /**
     * Validar límites por trámite
     */
    public void validarLimitesPorTramite(int cantidadActual, long tamanoTotalActual, long tamanoNuevoArchivo) {
        // Validar cantidad máxima de archivos
        if (cantidadActual >= storageConfig.getMaxFilesPerTramite()) {
            throw new IllegalArgumentException(
                String.format("Se ha alcanzado el límite máximo de %d archivos por trámite",
                              storageConfig.getMaxFilesPerTramite()));
        }

        // Validar tamaño total
        long nuevoTamanoTotal = tamanoTotalActual + tamanoNuevoArchivo;
        if (nuevoTamanoTotal > storageConfig.getMaxTotalSizeBytes()) {
            throw new IllegalArgumentException(
                String.format("El trámite excedería el límite máximo de %s de almacenamiento total",
                              storageConfig.getMaxTotalSizePerTramite()));
        }
    }

    /**
     * Determinar el tipo MIME basado en la extensión
     */
    public String determinarTipoMime(String nombreArchivo) {
        String extension = obtenerExtension(nombreArchivo).toLowerCase();

        return switch (extension) {
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "zip" -> "application/zip";
            case "rar" -> "application/x-rar-compressed";
            case "dwg" -> "application/dwg";
            case "dxf" -> "application/dxf";
            default -> "application/octet-stream";
        };
    }

    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) {
            return "";
        }
        return nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1);
    }

    private String generarNombreUnico(String nombreOriginal) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nombreSinExtension = nombreOriginal;
        String extension = "";

        int ultimoPunto = nombreOriginal.lastIndexOf('.');
        if (ultimoPunto > 0) {
            nombreSinExtension = nombreOriginal.substring(0, ultimoPunto);
            extension = nombreOriginal.substring(ultimoPunto);
        }

        // Limpiar caracteres especiales del nombre
        nombreSinExtension = nombreSinExtension.replaceAll("[^a-zA-Z0-9_-]", "_");

        return nombreSinExtension + "_" + timestamp + extension;
    }

    private String generarRutaArchivo(Long tramiteId, String nombreArchivo) {
        int year = Year.now().getValue();
        int mesHash = Math.abs(tramiteId.hashCode() % 12) + 1; // Distribuir en 12 subdirectorios

        return String.format("%d/%02d/tramite_%d/%s", year, mesHash, tramiteId, nombreArchivo);
    }
}