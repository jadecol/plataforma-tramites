package com.gestion.tramites.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor // Mantenemos Lombok por si lo usas para getters/setters/equals/hashcode
@NoArgsConstructor
public class JwtResponse {
    private String token;
    private String tipo = "Bearer";
    private Long id;
    private String correoElectronico;
    private String rol;
    private String nombreCompleto;

    // ESTE CONSTRUCTOR MANUAL ES VITAL PARA RESOLVER EL ERROR ACTUAL.
    // Coincide EXACTAMENTE con la firma que espera AuthService (5 argumentos).
    public JwtResponse(String token, Long id, String correoElectronico, String rol, String nombreCompleto) {
        this.token = token;
        this.id = id;
        this.correoElectronico = correoElectronico;
        this.rol = rol;
        this.nombreCompleto = nombreCompleto;
    }
}
