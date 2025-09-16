package com.gestion.tramites.config;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.gestion.tramites.entidad.EntidadRepository;
import com.gestion.tramites.model.Entidad; // <-- Descomenta este import
import com.gestion.tramites.usuario.Usuario;
import com.gestion.tramites.usuario.UsuarioRepository;
import com.gestion.tramites.util.PasswordGenerator;

@Component
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordGenerator passwordGenerator;
    private final EntidadRepository entidadRepository;

    @Autowired
    public DataLoader(UsuarioRepository usuarioRepository, PasswordGenerator passwordGenerator, EntidadRepository entidadRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordGenerator = passwordGenerator;
        this.entidadRepository = entidadRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        createGlobalAdmin();
        createEntidadAndAdmin();
    }

    private void createGlobalAdmin() {
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
            adminGlobal.setEntidad(null);

            usuarioRepository.save(adminGlobal);
            System.out.println("Usuario 'admin.global@gestion.com' (ADMIN_GLOBAL) creado al inicio de la aplicación.");
        } else {
            System.out.println("Usuario 'admin.global@gestion.com' (ADMIN_GLOBAL) ya existe, no se creó de nuevo.");
        }
    }

    private void createEntidadAndAdmin() {
        Optional<Entidad> existingEntidad = entidadRepository.findByNit("900123456");

        if (existingEntidad.isEmpty()) {
            Entidad entidadPrueba = new Entidad("Curaduría Urbana No. 1", "900123456", "Calle 100 #20-30", "6011234567", "curaduria1@example.com", "www.curaduria1.com");
            entidadRepository.save(entidadPrueba);
            System.out.println("Entidad 'Curaduría Urbana No. 1' creada.");
            
            // Llama a un método para crear el admin de la entidad
            createEntidadAdmin(entidadPrueba);

        } else {
            System.out.println("Entidad 'Curaduría Urbana No. 1' ya existe, no se creó.");
            // Si la entidad ya existe, asegúrate de crear su admin si no existe.
            createEntidadAdmin(existingEntidad.get());
        }
    }
    
    private void createEntidadAdmin(Entidad entidad) {
        String correoAdminEntidad = "admin.curaduria1@gestion.com";
        Optional<Usuario> existingAdminEntidad = usuarioRepository.findByCorreoElectronico(correoAdminEntidad);

        if (existingAdminEntidad.isEmpty()) {
            Usuario adminEntidad = new Usuario();
            adminEntidad.setCorreoElectronico(correoAdminEntidad);
            adminEntidad.setContrasenaHash(passwordGenerator.encode("AdminCuraduriaPass123!"));
            adminEntidad.setNombreCompleto("Administrador Curaduría 1");
            adminEntidad.setTipoDocumento("CC");
            adminEntidad.setNumeroDocumento("111111111");
            adminEntidad.setTelefono("3007654321");
            adminEntidad.setEstaActivo(true);
            adminEntidad.setFechaCreacion(LocalDateTime.now());
            adminEntidad.setRol("ADMIN_ENTIDAD");
            adminEntidad.setEntidad(entidad);

            usuarioRepository.save(adminEntidad);
            System.out.println("Usuario '" + correoAdminEntidad + "' (ADMIN_ENTIDAD) creado.");
        } else {
            System.out.println("Usuario '" + correoAdminEntidad + "' ya existe, no se creó.");
        }
    }
}
