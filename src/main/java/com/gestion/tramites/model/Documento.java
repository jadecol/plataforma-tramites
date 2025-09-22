package com.gestion.tramites.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "documentos")
public class Documento extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_documento")
    private Long idDocumento;

    @Column(name = "nombre_archivo", nullable = false)
    @NotBlank(message = "El nombre del archivo es obligatorio")
    @Size(max = 255, message = "El nombre del archivo no puede exceder 255 caracteres")
    private String nombreArchivo;

    @Column(name = "nombre_original", nullable = false)
    @NotBlank(message = "El nombre original es obligatorio")
    @Size(max = 255, message = "El nombre original no puede exceder 255 caracteres")
    private String nombreOriginal;

    @Column(name = "tipo_mime", nullable = false)
    @NotBlank(message = "El tipo MIME es obligatorio")
    @Size(max = 100, message = "El tipo MIME no puede exceder 100 caracteres")
    private String tipoMime;

    @Column(name = "extension", nullable = false)
    @NotBlank(message = "La extensión es obligatoria")
    @Size(max = 10, message = "La extensión no puede exceder 10 caracteres")
    private String extension;

    @Column(name = "tamano_bytes", nullable = false)
    @NotNull(message = "El tamaño es obligatorio")
    private Long tamanoBytes;

    @Column(name = "ruta_archivo", nullable = false)
    @NotBlank(message = "La ruta del archivo es obligatoria")
    @Size(max = 500, message = "La ruta no puede exceder 500 caracteres")
    private String rutaArchivo;

    @Column(name = "hash_archivo", nullable = false)
    @NotBlank(message = "El hash del archivo es obligatorio")
    @Size(max = 64, message = "El hash no puede exceder 64 caracteres")
    private String hashArchivo;

    @Column(name = "tipo_documento", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "El tipo de documento es obligatorio")
    private TipoDocumento tipoDocumento;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "version", nullable = false)
    @NotNull(message = "La versión es obligatoria")
    private Integer version = 1;

    @Column(name = "es_version_actual", nullable = false)
    private Boolean esVersionActual = true;

    @Column(name = "fecha_subida", nullable = false)
    private LocalDateTime fechaSubida;

    @Column(name = "fecha_ultimo_acceso")
    private LocalDateTime fechaUltimoAcceso;

    @Column(name = "estado", nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoDocumento estado = EstadoDocumento.ACTIVO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tramite", nullable = false)
    @NotNull(message = "El trámite es obligatorio")
    private Tramite tramite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_subida", nullable = false)
    @NotNull(message = "El usuario que subió el documento es obligatorio")
    private Usuario usuarioSubida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_documento_padre")
    private Documento documentoPadre;

    public enum TipoDocumento {
        CEDULA_CIUDADANIA("Cédula de Ciudadanía", "Documento de identificación personal"),
        CERTIFICADO_LIBERTAD_TRADICION("Certificado de Libertad y Tradición", "Documento que acredita la propiedad del inmueble"),
        PLANOS_ARQUITECTONICOS("Planos Arquitectónicos", "Diseños técnicos de la construcción"),
        PLANOS_ESTRUCTURALES("Planos Estructurales", "Diseños de la estructura de la construcción"),
        ESTUDIO_SUELOS("Estudio de Suelos", "Análisis geotécnico del terreno"),
        LICENCIA_AMBIENTAL("Licencia Ambiental", "Permiso para actividades que afecten el medio ambiente"),
        CONCEPTO_BOMBEROS("Concepto de Bomberos", "Aprobación de medidas contra incendios"),
        CONCEPTO_ESPACIO_PUBLICO("Concepto de Espacio Público", "Autorización para afectación del espacio público"),
        PAZ_Y_SALVO_PREDIAL("Paz y Salvo Predial", "Certificado de estar al día con impuestos prediales"),
        FORMATO_UNICO_SOLICITUD("Formato Único de Solicitud", "Formulario oficial de solicitud del trámite"),
        PODER_AUTENTICADO("Poder Autenticado", "Documento que otorga representación legal"),
        ACTA_VECINDAD("Acta de Vecindad", "Documento que certifica el estado de predios colindantes"),
        FOTOGRAFIA_PREDIO("Fotografía del Predio", "Registro fotográfico del inmueble"),
        CONCEPTO_TECNICO("Concepto Técnico", "Evaluación técnica especializada"),
        DOCUMENTO_RESPUESTA("Documento de Respuesta", "Respuesta a requerimientos o solicitudes"),
        OTRO("Otro", "Otro tipo de documento no especificado");

        private final String descripcion;
        private final String detalle;

        TipoDocumento(String descripcion, String detalle) {
            this.descripcion = descripcion;
            this.detalle = detalle;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public String getDetalle() {
            return detalle;
        }
    }

    public enum EstadoDocumento {
        ACTIVO("Activo", "Documento disponible para consulta"),
        ELIMINADO("Eliminado", "Documento marcado como eliminado"),
        ARCHIVADO("Archivado", "Documento archivado por nueva versión"),
        BLOQUEADO("Bloqueado", "Documento bloqueado por seguridad");

        private final String descripcion;
        private final String detalle;

        EstadoDocumento(String descripcion, String detalle) {
            this.descripcion = descripcion;
            this.detalle = detalle;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public String getDetalle() {
            return detalle;
        }
    }

    @PrePersist
    protected void onCreate() {
        this.fechaSubida = LocalDateTime.now();
        this.fechaUltimoAcceso = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoDocumento.ACTIVO;
        }
        if (this.version == null) {
            this.version = 1;
        }
        if (this.esVersionActual == null) {
            this.esVersionActual = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaUltimoAcceso = LocalDateTime.now();
    }

    public void marcarComoEliminado() {
        this.estado = EstadoDocumento.ELIMINADO;
        this.esVersionActual = false;
    }

    public void archivarPorNuevaVersion() {
        this.estado = EstadoDocumento.ARCHIVADO;
        this.esVersionActual = false;
    }

    public boolean esActivo() {
        return this.estado == EstadoDocumento.ACTIVO;
    }

    public boolean esVersionActual() {
        return Boolean.TRUE.equals(this.esVersionActual);
    }

    public String getNombreCompletoConVersion() {
        return String.format("%s (v%d)", this.nombreOriginal, this.version);
    }

    public String getTamanoLegible() {
        if (tamanoBytes == null) return "0 B";

        long bytes = tamanoBytes;
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    public Documento() {}

    public Documento(String nombreOriginal, String tipoMime, String extension, Long tamanoBytes,
                    String rutaArchivo, String hashArchivo, TipoDocumento tipoDocumento,
                    Tramite tramite, Usuario usuarioSubida) {
        this.nombreOriginal = nombreOriginal;
        this.nombreArchivo = generarNombreUnico(nombreOriginal);
        this.tipoMime = tipoMime;
        this.extension = extension;
        this.tamanoBytes = tamanoBytes;
        this.rutaArchivo = rutaArchivo;
        this.hashArchivo = hashArchivo;
        this.tipoDocumento = tipoDocumento;
        this.tramite = tramite;
        this.usuarioSubida = usuarioSubida;
        this.setEntidad(tramite.getEntidad());
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

        return nombreSinExtension + "_" + timestamp + extension;
    }

    // Getters y Setters
    public Long getIdDocumento() { return idDocumento; }
    public void setIdDocumento(Long idDocumento) { this.idDocumento = idDocumento; }

    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }

    public String getNombreOriginal() { return nombreOriginal; }
    public void setNombreOriginal(String nombreOriginal) { this.nombreOriginal = nombreOriginal; }

    public String getTipoMime() { return tipoMime; }
    public void setTipoMime(String tipoMime) { this.tipoMime = tipoMime; }

    public String getExtension() { return extension; }
    public void setExtension(String extension) { this.extension = extension; }

    public Long getTamanoBytes() { return tamanoBytes; }
    public void setTamanoBytes(Long tamanoBytes) { this.tamanoBytes = tamanoBytes; }

    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }

    public String getHashArchivo() { return hashArchivo; }
    public void setHashArchivo(String hashArchivo) { this.hashArchivo = hashArchivo; }

    public TipoDocumento getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(TipoDocumento tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public Boolean getEsVersionActual() { return esVersionActual; }
    public void setEsVersionActual(Boolean esVersionActual) { this.esVersionActual = esVersionActual; }

    public LocalDateTime getFechaSubida() { return fechaSubida; }
    public void setFechaSubida(LocalDateTime fechaSubida) { this.fechaSubida = fechaSubida; }

    public LocalDateTime getFechaUltimoAcceso() { return fechaUltimoAcceso; }
    public void setFechaUltimoAcceso(LocalDateTime fechaUltimoAcceso) { this.fechaUltimoAcceso = fechaUltimoAcceso; }

    public EstadoDocumento getEstado() { return estado; }
    public void setEstado(EstadoDocumento estado) { this.estado = estado; }

    public Tramite getTramite() { return tramite; }
    public void setTramite(Tramite tramite) { this.tramite = tramite; }

    public Usuario getUsuarioSubida() { return usuarioSubida; }
    public void setUsuarioSubida(Usuario usuarioSubida) { this.usuarioSubida = usuarioSubida; }

    public Documento getDocumentoPadre() { return documentoPadre; }
    public void setDocumentoPadre(Documento documentoPadre) { this.documentoPadre = documentoPadre; }
}