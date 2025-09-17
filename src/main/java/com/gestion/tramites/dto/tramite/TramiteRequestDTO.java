package com.gestion.tramites.dto.tramite;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TramiteRequestDTO {

    @NotNull(message = "El solicitante es obligatorio")
    private Long idSolicitante;

    @NotNull(message = "El tipo de trámite es obligatorio")
    private Long idTipoTramite;

    private Long idModalidadTramite;
    private Long idSubtipoTramite;
    private Long idEntidad; // Solo para ADMIN_GLOBAL

    @NotBlank(message = "El objeto del trámite es obligatorio")
    @Size(max = 500, message = "El objeto del trámite no puede exceder 500 caracteres")
    private String objetoTramite;

    @Size(max = 1000, message = "La descripción del proyecto no puede exceder 1000 caracteres")
    private String descripcionProyecto;

    @Size(max = 255, message = "La dirección del inmueble no puede exceder 255 caracteres")
    private String direccionInmueble;

    // Constructores
    public TramiteRequestDTO() {}

    // Getters y Setters
    public Long getIdSolicitante() { return idSolicitante; }
    public void setIdSolicitante(Long idSolicitante) { this.idSolicitante = idSolicitante; }

    public Long getIdTipoTramite() { return idTipoTramite; }
    public void setIdTipoTramite(Long idTipoTramite) { this.idTipoTramite = idTipoTramite; }

    public Long getIdModalidadTramite() { return idModalidadTramite; }
    public void setIdModalidadTramite(Long idModalidadTramite) { this.idModalidadTramite = idModalidadTramite; }

    public Long getIdSubtipoTramite() { return idSubtipoTramite; }
    public void setIdSubtipoTramite(Long idSubtipoTramite) { this.idSubtipoTramite = idSubtipoTramite; }

    public Long getIdEntidad() { return idEntidad; }
    public void setIdEntidad(Long idEntidad) { this.idEntidad = idEntidad; }

    public String getObjetoTramite() { return objetoTramite; }
    public void setObjetoTramite(String objetoTramite) { this.objetoTramite = objetoTramite; }

    public String getDescripcionProyecto() { return descripcionProyecto; }
    public void setDescripcionProyecto(String descripcionProyecto) { this.descripcionProyecto = descripcionProyecto; }

    public String getDireccionInmueble() { return direccionInmueble; }
    public void setDireccionInmueble(String direccionInmueble) { this.direccionInmueble = direccionInmueble; }
}