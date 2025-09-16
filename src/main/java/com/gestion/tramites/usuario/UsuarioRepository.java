package com.gestion.tramites.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // Importar Optional

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Añadir estos métodos para resolver los errores:
    Optional<Usuario> findByNumeroDocumento(String numeroDocumento);
    Optional<Usuario> findByCorreoElectronico(String correoElectronico);

    // Puedes añadir otros métodos personalizados si los necesitas
}