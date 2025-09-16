package com.gestion.tramites.tramite.dto;

import com.gestion.tramites.entidad.dto.EntidadDTO;
import com.gestion.tramites.model.Tramite.EstadoTramite;
import com.gestion.tramites.usuario.UsuarioResponseDTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TramiteResponseDTO {
    private Long idTramite;
    private UsuarioResponseDTO solicitante; // DTO completo del solicitante
    private EntidadDTO entidad;     // <-- ¡CAMBIADO EL TIPO! Ahora es EntidadDTO
    private UsuarioResponseDTO revisor;     // DTO completo del revisor (puede ser nulo)
    private String nombreTramite;
    private String descripcion;
    private EstadoTramite estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private LocalDateTime fechaFinalizacion;
    private String comentariosRevisor;
    private String numeroExpediente;
}
