package com.gestion.tramites.integration;

import com.gestion.tramites.model.ConsecutivoRadicacion;
import com.gestion.tramites.model.Entidad;
import com.gestion.tramites.model.TipoTramite;
import com.gestion.tramites.model.Tramite;
import com.gestion.tramites.model.Usuario;
import com.gestion.tramites.repository.ConsecutivoRadicacionRepository;
import com.gestion.tramites.repository.EntidadRepository;
import com.gestion.tramites.repository.TipoTramiteRepository;
import com.gestion.tramites.repository.TramiteRepository;
import com.gestion.tramites.repository.UsuarioRepository;
import com.gestion.tramites.service.RadicacionService;
import com.gestion.tramites.service.ValidacionRadicacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RadicacionIntegrationTest {

    @Autowired
    private RadicacionService radicacionService;

    @Autowired
    private ValidacionRadicacionService validacionService;

    @Autowired
    private EntidadRepository entidadRepository;

    @Autowired
    private TipoTramiteRepository tipoTramiteRepository;

    @Autowired
    private TramiteRepository tramiteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ConsecutivoRadicacionRepository consecutivoRepository;

    private Entidad entidadSecretaria;
    private Entidad entidadCuraduria;
    private TipoTramite tipoTramite;
    private Usuario usuarioAdmin;

    @BeforeEach
    void setUp() {
        // Limpiar datos existentes
        tramiteRepository.deleteAll();
        consecutivoRepository.deleteAll();
        usuarioRepository.deleteAll();
        tipoTramiteRepository.deleteAll();
        entidadRepository.deleteAll();

        // Crear entidades de prueba
        entidadSecretaria = crearEntidadSecretaria();
        entidadCuraduria = crearEntidadCuraduria();
        tipoTramite = crearTipoTramite();
        usuarioAdmin = crearUsuarioAdmin();

        // Configurar contexto de seguridad
        configurarContextoSeguridad();
    }

    @Test
    void deberiaGenerarNumeroRadicacionSecretaria() {
        // When
        String numeroRadicacion = radicacionService.generarSiguienteNumeroRadicacion(entidadSecretaria.getId());

        // Then
        assertThat(numeroRadicacion).isNotNull();
        assertThat(numeroRadicacion).matches("\\d{5}-0-\\d{2}-\\d{4}");
        assertThat(numeroRadicacion).startsWith(entidadSecretaria.getCodigoDane() + "-0-");

        // Verificar que se creó el consecutivo
        Optional<ConsecutivoRadicacion> consecutivo = consecutivoRepository
            .findByEntidadAndTipoEntidadAndAnoAndActivoTrue(entidadSecretaria,
                ConsecutivoRadicacion.TipoEntidadRadicacion.SECRETARIA,
                LocalDate.now().getYear());

        assertThat(consecutivo).isPresent();
        assertThat(consecutivo.get().getUltimoConsecutivo()).isEqualTo(1);
    }

    @Test
    void deberiaGenerarNumeroRadicacionCuraduria() {
        // When
        String numeroRadicacion = radicacionService.generarSiguienteNumeroRadicacion(entidadCuraduria.getId());

        // Then
        assertThat(numeroRadicacion).isNotNull();
        assertThat(numeroRadicacion).matches("\\d{5}-CUR-\\d{2}-\\d{4}");
        assertThat(numeroRadicacion).startsWith(entidadCuraduria.getCodigoDane() + "-CUR-");
    }

    @Test
    void deberiaRadicarTramiteCompleto() {
        // Given
        RadicacionService.SolicitudRadicacionTramite solicitud = new RadicacionService.SolicitudRadicacionTramite();
        solicitud.setEntidadId(entidadSecretaria.getId());
        solicitud.setTipoTramiteId(tipoTramite.getId());
        solicitud.setObjetoTramite("Construcción de casa unifamiliar");
        solicitud.setSolicitanteEmail("ciudadano@ejemplo.com");
        solicitud.setObservaciones("Trámite de prueba");

        // When
        RadicacionService.SolicitudRadicacion radicacion = radicacionService.radicarTramite(solicitud);

        // Then
        assertThat(radicacion).isNotNull();
        assertThat(radicacion.getNumeroRadicacion()).isNotNull();
        assertThat(radicacion.getIdTramite()).isNotNull();
        assertThat(radicacion.getEstado()).isEqualTo(Tramite.EstadoTramite.RADICADO);

        // Verificar que se creó el trámite en la base de datos
        Optional<Tramite> tramiteCreado = tramiteRepository.findById(radicacion.getIdTramite());
        assertThat(tramiteCreado).isPresent();
        assertThat(tramiteCreado.get().getNumeroRadicacion()).isEqualTo(radicacion.getNumeroRadicacion());
        assertThat(tramiteCreado.get().getObjetoTramite()).isEqualTo(solicitud.getObjetoTramite());
        assertThat(tramiteCreado.get().getSolicitante().getCorreoElectronico()).isEqualTo(solicitud.getSolicitanteEmail());
    }

    @Test
    void deberiaGenerarConsecutivosUnicos() {
        // Given
        int numeroGeneraciones = 10;

        // When
        List<String> numerosGenerados = java.util.stream.IntStream.range(0, numeroGeneraciones)
            .mapToObj(i -> radicacionService.generarSiguienteNumeroRadicacion(entidadSecretaria.getId()))
            .toList();

        // Then
        assertThat(numerosGenerados).hasSize(numeroGeneraciones);
        assertThat(numerosGenerados.stream().distinct().count()).isEqualTo(numeroGeneraciones);

        // Verificar secuencia correcta
        for (int i = 0; i < numeroGeneraciones; i++) {
            String numeroEsperado = String.format("%s-0-%02d-%04d",
                entidadSecretaria.getCodigoDane(),
                LocalDate.now().getYear() % 100,
                i + 1);
            assertThat(numerosGenerados.get(i)).isEqualTo(numeroEsperado);
        }
    }

    @Test
    void deberiaValidarFormatoCorrectamente() {
        // Given
        String numeroValido = entidadSecretaria.getCodigoDane() + "-0-25-0001";
        String numeroInvalido = "FORMATO-MALO";

        // When
        ValidacionRadicacionService.ResultadoValidacionRadicacion resultadoValido =
            validacionService.validarFormato(numeroValido);
        ValidacionRadicacionService.ResultadoValidacionRadicacion resultadoInvalido =
            validacionService.validarFormato(numeroInvalido);

        // Then
        assertThat(resultadoValido.isValido()).isTrue();
        assertThat(resultadoInvalido.isValido()).isFalse();
        assertThat(resultadoInvalido.getMensaje()).contains("Formato inválido");
    }

    @Test
    void deberiaDetectarDuplicados() {
        // Given
        String numeroRadicacion = radicacionService.generarSiguienteNumeroRadicacion(entidadSecretaria.getId());

        // Crear trámite con este número
        Tramite tramite = new Tramite();
        tramite.setNumeroRadicacion(numeroRadicacion);
        tramite.setObjetoTramite("Trámite de prueba");
        tramite.setFechaRadicacion(LocalDate.now());
        tramite.setEstadoActual(Tramite.EstadoTramite.RADICADO);
        tramite.setEntidad(entidadSecretaria);
        tramite.setTipoTramite(tipoTramite);
        tramite.setSolicitante(usuarioAdmin);
        tramiteRepository.save(tramite);

        // When
        ValidacionRadicacionService.ResultadoValidacionRadicacion resultado =
            validacionService.validarUnicidad(numeroRadicacion);

        // Then
        assertThat(resultado.isValido()).isFalse();
        assertThat(resultado.getMensaje()).contains("ya existe");
    }

    @Test
    void deberiaObtenerEstadisticasCorrectas() {
        // Given
        int numeroRadicaciones = 5;
        for (int i = 0; i < numeroRadicaciones; i++) {
            radicacionService.generarSiguienteNumeroRadicacion(entidadSecretaria.getId());
        }

        // When
        RadicacionService.EstadisticasRadicacion estadisticas =
            radicacionService.obtenerEstadisticasRadicacion(entidadSecretaria.getId(), LocalDate.now().getYear());

        // Then
        assertThat(estadisticas.getEntidadId()).isEqualTo(entidadSecretaria.getId());
        assertThat(estadisticas.getRadicacionesSecretaria()).isEqualTo(numeroRadicaciones);
        assertThat(estadisticas.getRadicacionesCuraduria()).isEqualTo(0);
        assertThat(estadisticas.getTotalRadicaciones()).isEqualTo(numeroRadicaciones);
    }

    @Test
    void deberiaDeterminarTipoEntidadCorrectamente() {
        // When
        ConsecutivoRadicacion.TipoEntidadRadicacion tipoSecretaria =
            validacionService.determinarTipoEntidad(entidadSecretaria);
        ConsecutivoRadicacion.TipoEntidadRadicacion tipoCuraduria =
            validacionService.determinarTipoEntidad(entidadCuraduria);

        // Then
        assertThat(tipoSecretaria).isEqualTo(ConsecutivoRadicacion.TipoEntidadRadicacion.SECRETARIA);
        assertThat(tipoCuraduria).isEqualTo(ConsecutivoRadicacion.TipoEntidadRadicacion.CURADURIA);
    }

    @Test
    void deberiaCrearSolicitanteAutomaticamente() {
        // Given
        String emailNuevo = "nuevo.solicitante@ejemplo.com";
        RadicacionService.SolicitudRadicacionTramite solicitud = new RadicacionService.SolicitudRadicacionTramite();
        solicitud.setEntidadId(entidadSecretaria.getId());
        solicitud.setTipoTramiteId(tipoTramite.getId());
        solicitud.setObjetoTramite("Nuevo trámite");
        solicitud.setSolicitanteEmail(emailNuevo);

        // When
        RadicacionService.SolicitudRadicacion radicacion = radicacionService.radicarTramite(solicitud);

        // Then
        Optional<Usuario> solicitante = usuarioRepository.findByCorreoElectronico(emailNuevo);
        assertThat(solicitante).isPresent();
        assertThat(solicitante.get().getRol()).isEqualTo(Usuario.Rol.SOLICITANTE);
        assertThat(solicitante.get().getEntidad()).isEqualTo(entidadSecretaria);

        // Verificar que el trámite está asociado al solicitante
        Optional<Tramite> tramite = tramiteRepository.findById(radicacion.getIdTramite());
        assertThat(tramite).isPresent();
        assertThat(tramite.get().getSolicitante().getCorreoElectronico()).isEqualTo(emailNuevo);
    }

    private Entidad crearEntidadSecretaria() {
        Entidad entidad = new Entidad();
        entidad.setNombre("Secretaría de Planeación de Prueba");
        entidad.setNit("900123456-1");
        entidad.setCodigoDane("11001");
        entidad.setDireccion("Calle 100 # 15-20");
        entidad.setTelefono("601-3216540");
        entidad.setEmail("planeacion@prueba.gov.co");
        entidad.setActivo(true);
        return entidadRepository.save(entidad);
    }

    private Entidad crearEntidadCuraduria() {
        Entidad entidad = new Entidad();
        entidad.setNombre("Curaduría Primera de Prueba");
        entidad.setNit("800987654-3");
        entidad.setCodigoDane("11001");
        entidad.setDireccion("Carrera 7 # 80-45");
        entidad.setTelefono("601-2109876");
        entidad.setEmail("curaduria1@prueba.com");
        entidad.setActivo(true);
        return entidadRepository.save(entidad);
    }

    private TipoTramite crearTipoTramite() {
        TipoTramite tipo = new TipoTramite();
        tipo.setNombre("Licencia de Construcción");
        tipo.setDescripcion("Licencia para construcción de obra nueva");
        tipo.setActivo(true);
        return tipoTramiteRepository.save(tipo);
    }

    private Usuario crearUsuarioAdmin() {
        Usuario usuario = new Usuario();
        usuario.setNombreCompleto("Administrador de Prueba");
        usuario.setCorreoElectronico("admin@prueba.gov.co");
        usuario.setContrasena("password123");
        usuario.setRol(Usuario.Rol.ADMIN_ENTIDAD);
        usuario.setEntidad(entidadSecretaria);
        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }

    private void configurarContextoSeguridad() {
        com.gestion.tramites.service.CustomUserDetails userDetails =
            new com.gestion.tramites.service.CustomUserDetails(usuarioAdmin);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            userDetails, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN_ENTIDAD")));

        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}