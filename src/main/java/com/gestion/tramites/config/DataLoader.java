package com.gestion.tramites.config;

import com.gestion.tramites.model.Usuario;
import com.gestion.tramites.repository.UsuarioRepository;
import com.gestion.tramites.util.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import com.gestion.tramites.repository.EntidadRepository; // <-- Descomenta este import
import com.gestion.tramites.model.Entidad; // <-- Descomenta este import

@Component
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordGenerator passwordGenerator;

    @Autowired
    private EntidadRepository entidadRepository; // <-- Descomenta e inyecta EntidadRepository

    @Autowired
    public DataLoader(UsuarioRepository usuarioRepository, PasswordGenerator passwordGenerator) {
        this.usuarioRepository = usuarioRepository;
        this.passwordGenerator = passwordGenerator;
    }

    @Override
    public void run(String... args) throws Exception {
        // Crear un ADMIN_GLOBAL si no existe
        Optional<Usuario> existingGlobalAdmin = usuarioRepository.findByCorreoElectronico("admin.global@gestion.com");

        if (existingGlobalAdmin.isEmpty()) {
            Usuario adminGlobal = new Usuario();
            adminGlobal.setCorreoElectronico("admin.global@gestion.com");
            adminGlobal.setContrasenaHash(passwordGenerator.encode("AdminGlobalPass123!"));
            adminGlobal.setNombreCompleto("Administrador Global");
            adminGlobal.setTipoDocumento("CC");
            adminGlobal.setNumeroDocumento("000000000");
            adminGlobal.setTelefono("3001234567");
            adminGlobal.setEstaActivo(true);
            adminGlobal.setFechaCreacion(LocalDateTime.now());
            adminGlobal.setRol("ADMIN_GLOBAL");
            // Para el ADMIN_GLOBAL, la entidad es nula porque no pertenece a una curaduría específica
            adminGlobal.setEntidad(null); // <-- Asegúrate de establecer esto explícitamente a null
            usuarioRepository.save(adminGlobal);
            System.out.println("Usuario 'admin.global@gestion.com' (ADMIN_GLOBAL) creado al inicio de la aplicación.");
        } else {
            System.out.println("Usuario 'admin.global@gestion.com' (ADMIN_GLOBAL) ya existe, no se creó de nuevo.");
        }

        // AÑADIR AQUI LA CREACIÓN DE UNA ENTIDAD DE PRUEBA Y UN ADMIN_ENTIDAD PARA ESA ENTIDAD
        // ESTO SE HARA UNA VEZ LA CLASE Entidad ESTÉ FUNCIONANDO CORRECTAMENTE
        // y una vez la clase Usuario haya sido actualizada con la relación a Entidad y Roles.

        // DESCOMENTAR ESTA SECCIÓN:
        if (entidadRepository != null) { // Para evitar NullPointerException si no se ha autowireado
            Optional<Entidad> existingEntidad = entidadRepository.findByNit("900123456");
            if (existingEntidad.isEmpty()) {
                Entidad entidadPrueba = new Entidad("Curaduría Urbana No. 1", "900123456", "Calle 100 #20-30", "6011234567", "curaduria1@example.com", "www.curaduria1.com");
                entidadRepository.save(entidadPrueba);
                System.out.println("Entidad 'Curaduría Urbana No. 1' creada.");

                // Crear ADMIN_ENTIDAD para esta Curaduría
                Optional<Usuario> existingAdminEntidad = usuarioRepository.findByCorreoElectronico("admin.curaduria1@gestion.com");
                if (existingAdminEntidad.isEmpty()) {
                    Usuario adminEntidad = new Usuario();
                    adminEntidad.setCorreoElectronico("admin.curaduria1@gestion.com");
                    adminEntidad.setContrasenaHash(passwordGenerator.encode("AdminCuraduriaPass123!"));
                    adminEntidad.setNombreCompleto("Administrador Curaduría 1");
                    adminEntidad.setTipoDocumento("CC");
                    adminEntidad.setNumeroDocumento("111111111");
                    adminEntidad.setTelefono("3007654321");
                    adminEntidad.setEstaActivo(true);
                    adminEntidad.setFechaCreacion(LocalDateTime.now());
                    adminEntidad.setRol("ADMIN_ENTIDAD"); // Asignar el rol
                    adminEntidad.setEntidad(entidadPrueba); // ¡IMPORTANTE! Vincula al ADMIN_ENTIDAD con la entidad

                    usuarioRepository.save(adminEntidad);
                    System.out.println("Usuario 'admin.curaduria1@gestion.com' (ADMIN_ENTIDAD) creado.");
                } else {
                    System.out.println("Usuario 'admin.curaduria1@gestion.com' ya existe, no se creó.");
                }
            } else {
                System.out.println("Entidad 'Curaduría Urbana No. 1' ya existe, no se creó.");
            }
        }
    }
}
