package com.gestion.tramites.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestion.tramites.model.Entidad;
import com.gestion.tramites.model.TipoTramite;
import com.gestion.tramites.model.Tramite;
import com.gestion.tramites.model.Usuario;
import com.gestion.tramites.repository.EntidadRepository;
import com.gestion.tramites.repository.TipoTramiteRepository;
import com.gestion.tramites.repository.TramiteRepository;
import com.gestion.tramites.repository.UsuarioRepository;
import com.gestion.tramites.security.test.WithMockCustomUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Asegura que se use el perfil de test, útil para bases de datos en memoria
@Transactional // Cada test se ejecuta en una transacción y se hace rollback al finalizar
public class TramiteControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // Para convertir objetos a JSON

    @Autowired
    private EntidadRepository entidadRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TipoTramiteRepository tipoTramiteRepository;

    @Autowired
    private TramiteRepository tramiteRepository;

    private Entidad entidadA;
    private Entidad entidadB;
    private Usuario userA;
    private Usuario userB;
    private TipoTramite tipoTramite;
    private Tramite tramiteA1;
    private Tramite tramiteA2;
    private Tramite tramiteB1;

    @BeforeEach
    void setUp() {
        // Limpiar y configurar datos para cada test
        tramiteRepository.deleteAll();
        usuarioRepository.deleteAll();
        tipoTramiteRepository.deleteAll();
        entidadRepository.deleteAll();

        // 1. Crear Entidades
        entidadA = new Entidad();
        entidadA.setNombre("Entidad A");
        entidadA = entidadRepository.save(entidadA);

        entidadB = new Entidad();
        entidadB.setNombre("Entidad B");
        entidadB = entidadRepository.save(entidadB);

        // 2. Crear Usuarios
        userA = new Usuario();
        userA.setUsername("userA");
        userA.setPassword("password"); // La contraseña no se usa en @WithMockCustomUser, pero es
                                       // buena práctica
        userA.setRol("USER");
        userA.setEntidad(entidadA);
        userA = usuarioRepository.save(userA);

        userB = new Usuario();
        userB.setUsername("userB");
        userB.setPassword("password");
        userB.setRol("USER");
        userB.setEntidad(entidadB);
        userB = usuarioRepository.save(userB);

        // 3. Crear Tipo de Trámite
        tipoTramite = new TipoTramite();
        tipoTramite.setNombre("Licencia");
        tipoTramite.setEntidad(entidadA); // Asignar a una entidad, aunque no es relevante para este
                                          // test
        tipoTramite = tipoTramiteRepository.save(tipoTramite);

        // 4. Crear Trámites
        tramiteA1 = new Tramite();
        tramiteA1.setNumeroRadicacion("TRA-A-001");
        tramiteA1.setEstadoActual(Tramite.EstadoTramite.RADICADO);
        tramiteA1.setFechaCreacion(LocalDateTime.now());
        tramiteA1.setEntidad(entidadA);
        tramiteA1.setSolicitante(userA);
        tramiteA1.setTipoTramite(tipoTramite);
        tramiteA1 = tramiteRepository.save(tramiteA1);

        tramiteA2 = new Tramite();
        tramiteA2.setNumeroRadicacion("TRA-A-002");
        tramiteA2.setEstadoActual(Tramite.EstadoTramite.EN_REVISION);
        tramiteA2.setFechaCreacion(LocalDateTime.now());
        tramiteA2.setEntidad(entidadA);
        tramiteA2.setSolicitante(userA);
        tramiteA2.setTipoTramite(tipoTramite);
        tramiteA2 = tramiteRepository.save(tramiteA2);

        tramiteB1 = new Tramite();
        tramiteB1.setNumeroRadicacion("TRA-B-001");
        tramiteB1.setEstadoActual(Tramite.EstadoTramite.RADICADO);
        tramiteB1.setFechaCreacion(LocalDateTime.now());
        tramiteB1.setEntidad(entidadB);
        tramiteB1.setSolicitante(userB);
        tramiteB1.setTipoTramite(tipoTramite);
        tramiteB1 = tramiteRepository.save(tramiteB1);
    }

    /**
     * Escenario: Un usuario de la Entidad A consulta sus propios trámites. Se espera que la
     * respuesta sea 200 OK y contenga solo los trámites de la Entidad A.
     */
    @Test
    @WithMockCustomUser(username = "userA", entityId = 1L, userId = 1L, roles = "USER")
    void cuandoUsuarioDeEntidadAConsultaSusTramites_debeRetornarSoloLosDeSuEntidad()
            throws Exception {
        mockMvc.perform(get("/api/tramites").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2))) // Espera 2
                                                                                 // trámites para la
                                                                                 // Entidad A
                .andExpect(jsonPath("$[0].numeroRadicacion", is(tramiteA1.getNumeroRadicacion())))
                .andExpect(jsonPath("$[1].numeroRadicacion", is(tramiteA2.getNumeroRadicacion())));
    }

    /**
     * Escenario: Un usuario de la Entidad A intenta acceder a un trámite específico de la Entidad
     * B. Se espera que la respuesta sea 404 Not Found, confirmando el aislamiento multi-tenant.
     */
    @Test
    @WithMockCustomUser(username = "userA", entityId = 1L, userId = 1L, roles = "USER")
    void cuandoUsuarioDeEntidadAIntentaAccederATramiteDeEntidadB_debeRetornar404NotFound()
            throws Exception {
        mockMvc.perform(get("/api/tramites/{id}", tramiteB1.getIdTramite())
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound()); // El
                                                                                            // filtro
                                                                                            // multi-tenant
                                                                                            // debería
                                                                                            // ocultarlo
    }

    /**
     * Escenario: Un usuario de la Entidad A intenta acceder a un trámite específico de su propia
     * entidad. Se espera que la respuesta sea 200 OK.
     */
    @Test
    @WithMockCustomUser(username = "userA", entityId = 1L, userId = 1L, roles = "USER")
    void cuandoUsuarioDeEntidadAIntentaAccederATramiteDeSuEntidad_debeRetornar200OK()
            throws Exception {
        mockMvc.perform(get("/api/tramites/{id}", tramiteA1.getIdTramite())
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroRadicacion", is(tramiteA1.getNumeroRadicacion())));
    }
}
