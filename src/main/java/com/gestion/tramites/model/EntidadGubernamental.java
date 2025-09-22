package com.gestion.tramites.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "entidades_gubernamentales")
public class EntidadGubernamental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entidad_gubernamental")
    private Long idEntidadGubernamental;

    @Column(name = "nombre", nullable = false, unique = true)
    @NotBlank(message = "El nombre de la entidad es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    private String nombre;

    @Column(name = "codigo_dane", nullable = false, unique = true)
    @NotBlank(message = "El código DANE es obligatorio")
    @Pattern(regexp = "^\\d{5}$", message = "El código DANE debe tener exactamente 5 dígitos")
    private String codigoDane;

    @Column(name = "nit", nullable = false, unique = true)
    @NotBlank(message = "El NIT es obligatorio")
    @Pattern(regexp = "^\\d{9}-\\d{1}$", message = "El NIT debe tener el formato 123456789-0")
    private String nit;

    @Column(name = "tipo_entidad", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "El tipo de entidad es obligatorio")
    private TipoEntidadGubernamental tipoEntidad;

    @Column(name = "dominio_oficial", nullable = false, unique = true)
    @NotBlank(message = "El dominio oficial es obligatorio")
    private String dominioOficial;

    @Column(name = "sitio_web_oficial", nullable = false)
    @NotBlank(message = "El sitio web oficial es obligatorio")
    private String sitioWebOficial;

    @Column(name = "email_oficial", nullable = false)
    @NotBlank(message = "El email oficial es obligatorio")
    @Pattern(regexp = "^[\\w.-]+@[\\w.-]+\\.(gov\\.co|edu\\.co)$",
             message = "El email debe pertenecer a un dominio oficial (.gov.co o .edu.co)")
    private String emailOficial;

    @Column(name = "telefono_oficial")
    @Pattern(regexp = "^\\+?57\\s?\\d{7,10}$",
             message = "El teléfono debe tener formato colombiano válido")
    private String telefonoOficial;

    @Column(name = "direccion_fisica", nullable = false)
    @NotBlank(message = "La dirección física es obligatoria")
    @Size(max = 500, message = "La dirección no puede exceder 500 caracteres")
    private String direccionFisica;

    @Column(name = "departamento", nullable = false)
    @NotBlank(message = "El departamento es obligatorio")
    @Size(max = 100, message = "El departamento no puede exceder 100 caracteres")
    private String departamento;

    @Column(name = "municipio", nullable = false)
    @NotBlank(message = "El municipio es obligatorio")
    @Size(max = 100, message = "El municipio no puede exceder 100 caracteres")
    private String municipio;

    @Column(name = "estado_verificacion", nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoVerificacion estadoVerificacion = EstadoVerificacion.PENDIENTE;

    @Column(name = "fecha_verificacion")
    private LocalDateTime fechaVerificacion;

    @Column(name = "verificado_por")
    private String verificadoPor;

    @Column(name = "observaciones_verificacion", length = 1000)
    private String observacionesVerificacion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Relación con la entidad original del sistema
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_entidad_sistema", referencedColumnName = "id")
    private Entidad entidadSistema;

    public enum TipoEntidadGubernamental {
        ALCALDIA("Alcaldía Municipal", "Entidad territorial municipal"),
        GOBERNACION("Gobernación Departamental", "Entidad territorial departamental"),
        CURADURIA_URBANA("Curaduría Urbana", "Entidad privada con funciones públicas"),
        SECRETARIA_PLANEACION("Secretaría de Planeación", "Dependencia municipal/departamental"),
        INSTITUTO_DESARROLLO_URBANO("Instituto de Desarrollo Urbano", "Entidad descentralizada"),
        EMPRESA_DESARROLLO_URBANO("Empresa de Desarrollo Urbano", "Empresa pública"),
        AREA_METROPOLITANA("Área Metropolitana", "Asociación de municipios"),
        CORPORACION_AUTONOMA("Corporación Autónoma Regional", "Entidad ambiental"),
        OTRO("Otro tipo de entidad", "Otra entidad gubernamental verificada");

        private final String descripcion;
        private final String detalle;

        TipoEntidadGubernamental(String descripcion, String detalle) {
            this.descripcion = descripcion;
            this.detalle = detalle;
        }

        public String getDescripcion() { return descripcion; }
        public String getDetalle() { return detalle; }
    }

    public enum EstadoVerificacion {
        PENDIENTE("Pendiente de verificación", "La entidad está en proceso de verificación"),
        VERIFICADA("Verificada", "La entidad ha sido verificada como legítima"),
        RECHAZADA("Rechazada", "La entidad no pudo ser verificada"),
        SUSPENDIDA("Suspendida", "La entidad ha sido suspendida temporalmente"),
        REVOCADA("Revocada", "La verificación ha sido revocada permanentemente");

        private final String descripcion;
        private final String detalle;

        EstadoVerificacion(String descripcion, String detalle) {
            this.descripcion = descripcion;
            this.detalle = detalle;
        }

        public String getDescripcion() { return descripcion; }
        public String getDetalle() { return detalle; }
    }

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        if (this.activo == null) {
            this.activo = true;
        }
        if (this.estadoVerificacion == null) {
            this.estadoVerificacion = EstadoVerificacion.PENDIENTE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    public boolean estaVerificada() {
        return this.estadoVerificacion == EstadoVerificacion.VERIFICADA;
    }

    public boolean estaActiva() {
        return Boolean.TRUE.equals(this.activo) && estaVerificada();
    }

    public void verificar(String verificadoPor, String observaciones) {
        this.estadoVerificacion = EstadoVerificacion.VERIFICADA;
        this.fechaVerificacion = LocalDateTime.now();
        this.verificadoPor = verificadoPor;
        this.observacionesVerificacion = observaciones;
    }

    public void rechazar(String verificadoPor, String observaciones) {
        this.estadoVerificacion = EstadoVerificacion.RECHAZADA;
        this.fechaVerificacion = LocalDateTime.now();
        this.verificadoPor = verificadoPor;
        this.observacionesVerificacion = observaciones;
        this.activo = false;
    }

    public void suspender(String verificadoPor, String observaciones) {
        this.estadoVerificacion = EstadoVerificacion.SUSPENDIDA;
        this.fechaVerificacion = LocalDateTime.now();
        this.verificadoPor = verificadoPor;
        this.observacionesVerificacion = observaciones;
        this.activo = false;
    }

    public String getNombreCompleto() {
        return String.format("%s - %s", this.tipoEntidad.getDescripcion(), this.nombre);
    }

    public EntidadGubernamental() {}

    // Getters y Setters
    public Long getIdEntidadGubernamental() { return idEntidadGubernamental; }
    public void setIdEntidadGubernamental(Long idEntidadGubernamental) { this.idEntidadGubernamental = idEntidadGubernamental; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCodigoDane() { return codigoDane; }
    public void setCodigoDane(String codigoDane) { this.codigoDane = codigoDane; }

    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }

    public TipoEntidadGubernamental getTipoEntidad() { return tipoEntidad; }
    public void setTipoEntidad(TipoEntidadGubernamental tipoEntidad) { this.tipoEntidad = tipoEntidad; }

    public String getDominioOficial() { return dominioOficial; }
    public void setDominioOficial(String dominioOficial) { this.dominioOficial = dominioOficial; }

    public String getSitioWebOficial() { return sitioWebOficial; }
    public void setSitioWebOficial(String sitioWebOficial) { this.sitioWebOficial = sitioWebOficial; }

    public String getEmailOficial() { return emailOficial; }
    public void setEmailOficial(String emailOficial) { this.emailOficial = emailOficial; }

    public String getTelefonoOficial() { return telefonoOficial; }
    public void setTelefonoOficial(String telefonoOficial) { this.telefonoOficial = telefonoOficial; }

    public String getDireccionFisica() { return direccionFisica; }
    public void setDireccionFisica(String direccionFisica) { this.direccionFisica = direccionFisica; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public EstadoVerificacion getEstadoVerificacion() { return estadoVerificacion; }
    public void setEstadoVerificacion(EstadoVerificacion estadoVerificacion) { this.estadoVerificacion = estadoVerificacion; }

    public LocalDateTime getFechaVerificacion() { return fechaVerificacion; }
    public void setFechaVerificacion(LocalDateTime fechaVerificacion) { this.fechaVerificacion = fechaVerificacion; }

    public String getVerificadoPor() { return verificadoPor; }
    public void setVerificadoPor(String verificadoPor) { this.verificadoPor = verificadoPor; }

    public String getObservacionesVerificacion() { return observacionesVerificacion; }
    public void setObservacionesVerificacion(String observacionesVerificacion) { this.observacionesVerificacion = observacionesVerificacion; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public Entidad getEntidadSistema() { return entidadSistema; }
    public void setEntidadSistema(Entidad entidadSistema) { this.entidadSistema = entidadSistema; }
}