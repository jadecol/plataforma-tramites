package com.gestion.tramites.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

@Entity
@Table(name = "consecutivos_radicacion",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"entidad_id", "tipo_entidad", "ano"},
                           name = "uk_consecutivo_entidad_ano")
       },
       indexes = {
           @Index(name = "idx_consecutivo_entidad", columnList = "entidad_id"),
           @Index(name = "idx_consecutivo_ano", columnList = "ano"),
           @Index(name = "idx_consecutivo_busqueda", columnList = "entidad_id, tipo_entidad, ano")
       })
public class ConsecutivoRadicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_consecutivo")
    private Long idConsecutivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entidad_id", nullable = false)
    @NotNull(message = "La entidad es obligatoria")
    private Entidad entidad;

    @Column(name = "codigo_dane", nullable = false, length = 5)
    @NotNull(message = "El código DANE es obligatorio")
    @Pattern(regexp = "^\\d{5}$", message = "El código DANE debe tener exactamente 5 dígitos")
    private String codigoDane;

    @Column(name = "tipo_entidad", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "El tipo de entidad es obligatorio")
    private TipoEntidadRadicacion tipoEntidad;

    @Column(name = "ano", nullable = false)
    @NotNull(message = "El año es obligatorio")
    @Positive(message = "El año debe ser positivo")
    private Integer ano;

    @Column(name = "ultimo_consecutivo", nullable = false)
    @NotNull(message = "El último consecutivo es obligatorio")
    private Integer ultimoConsecutivo = 0;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    public enum TipoEntidadRadicacion {
        SECRETARIA("0", "Secretaría de Planeación"),
        CURADURIA("CUR", "Curaduría Urbana");

        private final String codigo;
        private final String descripcion;

        TipoEntidadRadicacion(String codigo, String descripcion) {
            this.codigo = codigo;
            this.descripcion = descripcion;
        }

        public String getCodigo() { return codigo; }
        public String getDescripcion() { return descripcion; }

        public static TipoEntidadRadicacion fromCodigo(String codigo) {
            for (TipoEntidadRadicacion tipo : values()) {
                if (tipo.codigo.equals(codigo)) {
                    return tipo;
                }
            }
            throw new IllegalArgumentException("Código de tipo de entidad no válido: " + codigo);
        }
    }

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        if (this.activo == null) {
            this.activo = true;
        }
        if (this.ultimoConsecutivo == null) {
            this.ultimoConsecutivo = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Genera el siguiente número de radicación en el formato correcto
     * Secretarías: COD_DANE-0-YY-NNNN
     * Curadurías: COD_DANE-CUR-YY-NNNN
     */
    public String generarSiguienteRadicacion() {
        int siguienteNumero = this.ultimoConsecutivo + 1;
        String anoCorto = String.valueOf(this.ano % 100);
        String numeroFormateado = String.format("%04d", siguienteNumero);

        return String.format("%s-%s-%s-%s",
                this.codigoDane,
                this.tipoEntidad.getCodigo(),
                anoCorto,
                numeroFormateado);
    }

    /**
     * Incrementa el consecutivo y genera el número de radicación
     */
    public String incrementarYGenerar() {
        this.ultimoConsecutivo++;
        this.fechaActualizacion = LocalDateTime.now();
        return generarSiguienteRadicacion();
    }

    /**
     * Valida si un número de radicación corresponde a este consecutivo
     */
    public boolean correspondeAEsteConsecutivo(String numeroRadicacion) {
        if (numeroRadicacion == null || numeroRadicacion.trim().isEmpty()) {
            return false;
        }

        String patron = String.format("%s-%s-%s-\\d{4}",
                this.codigoDane,
                this.tipoEntidad.getCodigo(),
                String.valueOf(this.ano % 100));

        return numeroRadicacion.matches(patron);
    }

    /**
     * Extrae el número consecutivo de una radicación
     */
    public static Integer extraerConsecutivo(String numeroRadicacion) {
        if (numeroRadicacion == null || numeroRadicacion.trim().isEmpty()) {
            return null;
        }

        String[] partes = numeroRadicacion.split("-");
        if (partes.length != 4) {
            return null;
        }

        try {
            return Integer.parseInt(partes[3]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public ConsecutivoRadicacion() {}

    // Getters y Setters
    public Long getIdConsecutivo() { return idConsecutivo; }
    public void setIdConsecutivo(Long idConsecutivo) { this.idConsecutivo = idConsecutivo; }

    public Entidad getEntidad() { return entidad; }
    public void setEntidad(Entidad entidad) { this.entidad = entidad; }

    public String getCodigoDane() { return codigoDane; }
    public void setCodigoDane(String codigoDane) { this.codigoDane = codigoDane; }

    public TipoEntidadRadicacion getTipoEntidad() { return tipoEntidad; }
    public void setTipoEntidad(TipoEntidadRadicacion tipoEntidad) { this.tipoEntidad = tipoEntidad; }

    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }

    public Integer getUltimoConsecutivo() { return ultimoConsecutivo; }
    public void setUltimoConsecutivo(Integer ultimoConsecutivo) { this.ultimoConsecutivo = ultimoConsecutivo; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}