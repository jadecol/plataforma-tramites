package com.gestion.tramites;

import com.gestion.tramites.model.Entidad;
import com.gestion.tramites.model.Tramite;
import com.gestion.tramites.model.Usuario;
import com.gestion.tramites.repository.EntidadRepository;
import com.gestion.tramites.repository.TramiteRepository;
import com.gestion.tramites.repository.UsuarioRepository;
import com.gestion.tramites.security.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ContextConfiguration(initializers = {MultiTenantSecurityIT.Initializer.class})
class MultiTenantSecurityIT {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine");

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                "spring.datasource.password=" + postgreSQLContainer.getPassword(),
                "spring.flyway.clean-disabled=false" // Permitir limpieza para pruebas
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EntidadRepository entidadRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private TramiteRepository tramiteRepository;

    private Entidad entidadA;
    private Entidad entidadB;
    private Usuario usuarioA;
    private Tramite tramiteA;
    private Tramite tramiteB;

    @BeforeEach
    void setUp() {
        // Limpiar la base de datos antes de cada prueba para asegurar el aislamiento
        tramiteRepository.deleteAll();
        usuarioRepository.deleteAll();
        entidadRepository.deleteAll();

        // Crear datos para Entidad A
        entidadA = new Entidad();
        entidadA.setNombre("Curaduría A");
        entidadRepository.save(entidadA);

        usuarioA = new Usuario();
        usuarioA.setCorreoElectronico("userA@curaduria_a.com");
        usuarioA.setEntidad(entidadA);
        // ... otros campos requeridos
        usuarioRepository.save(usuarioA);

        tramiteA = new Tramite();
        tramiteA.setNumeroRadicacion("RAD-A-001");
        tramiteA.setEntidad(entidadA);
        tramiteRepository.save(tramiteA);

        // Crear datos para Entidad B
        entidadB = new Entidad();
        entidadB.setNombre("Curaduría B");
        entidadRepository.save(entidadB);

        Usuario usuarioB = new Usuario();
        usuarioB.setCorreoElectronico("userB@curaduria_b.com");
        usuarioB.setEntidad(entidadB);
        usuarioRepository.save(usuarioB);

        tramiteB = new Tramite();
        tramiteB.setNumeroRadicacion("RAD-B-001");
        tramiteB.setEntidad(entidadB);
        tramiteRepository.save(tramiteB);
    }

    private String generateTokenForUser(Usuario user) {
        UserDetails userDetails = new User(user.getCorreoElectronico(), "", new ArrayList<>());
        return jwtUtil.generateToken(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
    }

    @Test
    void testAccessoPermitido_UsuarioDebeVerSoloSusPropiosTramites() throws Exception {
        // GIVEN: Un token para un usuario de la Entidad A
        String tokenUsuarioA = generateTokenForUser(usuarioA);

        // WHEN: El usuario A solicita la lista de todos los trámites
        mockMvc.perform(get("/api/tramites") // Asumiendo que este es el endpoint
                .header("Authorization", "Bearer " + tokenUsuarioA))
                // THEN: La solicitud es exitosa y solo recibe los trámites de su entidad
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))) // Debe recibir solo 1 trámite
                .andExpect(jsonPath("$[0].numeroRadicacion", is(tramiteA.getNumeroRadicacion())));
    }

    @Test
    void testAccesoDenegado_UsuarioNoDebeVerTramitesDeOtrasEntidades() throws Exception {
        // GIVEN: Un token para un usuario de la Entidad A y el ID de un trámite de la Entidad B
        String tokenUsuarioA = generateTokenForUser(usuarioA);
        Long idTramiteEntidadB = tramiteB.getIdTramite();

        // WHEN: El usuario A intenta acceder directamente al trámite de la Entidad B
        mockMvc.perform(get("/api/tramites/" + idTramiteEntidadB) // Asumiendo este endpoint
                .header("Authorization", "Bearer " + tokenUsuarioA))
                // THEN: El sistema debe responder con 404 Not Found
                // Esto confirma que, para el usuario A, el recurso de la entidad B no existe.
                .andExpect(status().isNotFound());
    }
}
