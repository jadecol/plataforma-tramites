package com.gestion.tramites.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Configuration
public class DocumentStorageConfig {

    @Value("${documents.storage.path}")
    private String storagePath;

    @Value("${documents.allowed-extensions}")
    private String allowedExtensionsStr;

    @Value("${documents.max-files-per-tramite}")
    private Integer maxFilesPerTramite;

    @Value("${documents.max-total-size-per-tramite}")
    private String maxTotalSizePerTramite;

    private List<String> allowedExtensions;
    private long maxTotalSizeBytes;

    @PostConstruct
    public void init() {
        try {
            // Crear directorios de almacenamiento si no existen
            Path storageDir = Paths.get(storagePath);
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
            }

            // Crear subdirectorios por año para organización
            createYearDirectories(storageDir);

            // Procesar extensiones permitidas
            allowedExtensions = Arrays.stream(allowedExtensionsStr.split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .toList();

            // Convertir tamaño máximo a bytes
            maxTotalSizeBytes = parseSize(maxTotalSizePerTramite);

        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar el almacenamiento de documentos", e);
        }
    }

    private void createYearDirectories(Path storageDir) throws IOException {
        int currentYear = java.time.Year.now().getValue();
        for (int year = currentYear; year <= currentYear + 2; year++) {
            Path yearDir = storageDir.resolve(String.valueOf(year));
            if (!Files.exists(yearDir)) {
                Files.createDirectories(yearDir);
            }
        }
    }

    private long parseSize(String sizeStr) {
        if (sizeStr == null || sizeStr.trim().isEmpty()) {
            return 500L * 1024 * 1024; // 500MB por defecto
        }

        sizeStr = sizeStr.trim().toUpperCase();
        long multiplier = 1;

        if (sizeStr.endsWith("KB")) {
            multiplier = 1024;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        } else if (sizeStr.endsWith("MB")) {
            multiplier = 1024 * 1024;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        } else if (sizeStr.endsWith("GB")) {
            multiplier = 1024L * 1024 * 1024;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        }

        try {
            return Long.parseLong(sizeStr.trim()) * multiplier;
        } catch (NumberFormatException e) {
            return 500L * 1024 * 1024; // 500MB por defecto
        }
    }

    public String getStoragePath() {
        return storagePath;
    }

    public List<String> getAllowedExtensions() {
        return allowedExtensions;
    }

    public Integer getMaxFilesPerTramite() {
        return maxFilesPerTramite;
    }

    public long getMaxTotalSizeBytes() {
        return maxTotalSizeBytes;
    }

    public String getMaxTotalSizePerTramite() {
        return maxTotalSizePerTramite;
    }
}