//package com.gestion.tramites.dto.tramite;
//
//import com.gestion.tramites.dto.EntidadResponseDTO; // Reutiliza este DTO
//import com.gestion.tramites.dto.usuario.UsuarioResponseDTO; // Reutiliza este DTO
//import com.gestion.tramites.entidad.Tramite.EstadoTramite;
//import lombok.Data;
//
//import java.time.LocalDateTime;
//
//@Data
//public class TramiteResponseDTO {
//    private Long idTramite;
//    private UsuarioResponseDTO solicitante; // DTO completo del solicitante
//    private EntidadResponseDTO entidad;     // DTO completo de la entidad
//    private UsuarioResponseDTO revisor;     // DTO completo del revisor (puede ser nulo)
//    private String nombreTramite;
//    private String descripcion;
//    private EstadoTramite estado;
//    private LocalDateTime fechaCreacion;
//    private LocalDateTime fechaActualizacion;
//    private LocalDateTime fechaFinalizacion;
//    private String comentariosRevisor;
//    private String numeroExpediente;
//}
package com.gestion.tramites.dto.tramite;

// AHORA SÍ, ESTOS SON LOS IMPORTS CORRECTOS PARA TUS DTOS
import com.gestion.tramites.dto.EntidadDTO; // <-- ¡CAMBIADO! Usas EntidadDTO, no EntidadResponseDTO
import com.gestion.tramites.dto.UsuarioResponseDTO; // <-- ¡CAMBIADO! UsuarioResponseDTO está directamente en 'dto', no en 'dto.usuario'

import com.gestion.tramites.entidad.Tramite.EstadoTramite;
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
