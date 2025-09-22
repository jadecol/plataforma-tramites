package com.gestion.tramites.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class ValidacionDominiosGubernamentalesService {

    private static final Logger logger = LoggerFactory.getLogger(ValidacionDominiosGubernamentalesService.class);

    @Value("${validacion.dominios.timeout:5000}")
    private int timeoutMs;

    @Value("${validacion.dominios.habilitada:true}")
    private boolean validacionHabilitada;

    // Lista blanca de dominios gubernamentales oficiales verificados
    private static final Set<String> DOMINIOS_OFICIALES_ALCALDIAS = Set.of(
        // Alcaldías principales
        "www.bogota.gov.co",
        "www.medellin.gov.co",
        "www.cali.gov.co",
        "www.barranquilla.gov.co",
        "www.cartagena.gov.co",
        "www.bucaramanga.gov.co",
        "www.pereira.gov.co",
        "www.manizales.gov.co",
        "www.ibague.gov.co",
        "www.cucuta.gov.co",
        "www.villavicencio.gov.co",
        "www.monteria.gov.co",
        "www.neiva.gov.co",
        "www.pasto.gov.co",
        "www.armenia.gov.co",
        "www.valledupar.gov.co",
        "www.popayan.gov.co",
        "www.sincelejo.gov.co",
        "www.florencia.gov.co",
        "www.riohacha.gov.co",
        "www.yopal.gov.co",
        "www.tunja.gov.co",
        "www.mocoa.gov.co",
        "www.quibdo.gov.co",
        "www.arauca.gov.co",
        "www.mitupuerto.gov.co",
        "www.sanjosedelguaviare.gov.co",
        "www.leticia.gov.co",

        // Alcaldías metropolitanas y municipios importantes
        "www.itagui.gov.co",
        "www.bello.gov.co",
        "www.envigado.gov.co",
        "www.copacabana.gov.co",
        "www.sabaneta.gov.co",
        "www.girardota.gov.co",
        "www.caldas.gov.co",
        "www.laestrellaantioquia.gov.co",

        // Cundinamarca
        "www.susa-cundinamarca.gov.co",
        "www.zipaquira.gov.co",
        "www.facatativa.gov.co",
        "www.chia.gov.co",
        "www.cajica.gov.co",
        "www.funza.gov.co",
        "www.madrid.gov.co",
        "www.mosquera.gov.co",
        "www.soacha.gov.co",
        "www.fusagasuga.gov.co",

        // Valle del Cauca
        "www.palmira.gov.co",
        "www.buenaventura.gov.co",
        "www.tulua.gov.co",
        "www.cartago.gov.co",
        "www.buga.gov.co",

        // Santander
        "www.floridablanca.gov.co",
        "www.giron.gov.co",
        "www.piedecuesta.gov.co",

        // Otros municipios importantes
        "www.dosquebradas.gov.co",
        "www.rionegro.gov.co"
    );

    private static final Set<String> DOMINIOS_OFICIALES_CURADURIAS = Set.of(
        // Curadurías principales - patrones verificados
        "curaduria1bogota.com",
        "curaduria2bogota.com",
        "curaduria3bogota.com",
        "curaduria4bogota.com",
        "curaduria5bogota.com",
        "curaduria1medellin.com",
        "curaduria2medellin.com",
        "curaduria1cali.com",
        "curaduria2cali.com",
        "curaduria3cali.com",
        "curaduria1barranquilla.com",
        "curaduria2barranquilla.com",
        "curaduria1bucaramanga.com",
        "curaduria2bucaramanga.com",
        "curaduria1cartagena.com",
        "curaduria1pereira.com",
        "curaduria1manizales.com",
        "curaduria1armenia.com",
        "curaduria1ibague.com",
        "curaduria1neiva.com",
        "curaduria1pasto.com",
        "curaduria1villavicencio.com",
        "curaduria1monteria.com",
        "curaduria1valledupar.com",
        "curaduria1sincelejo.com",
        "curaduria1popayan.com",
        "curaduria1tunja.com",
        "curaduria1florencia.com",
        "curaduria1yopal.com",
        "curaduria1riohacha.com",
        "curaduria1arauca.com",
        "curaduria1mocoa.com",
        "curaduria1quibdo.com",
        "curaduria1leticia.com",
        "curaduria1mitupuerto.com",
        "curaduria1sanjose.com"
    );

    private static final Set<String> DOMINIOS_INSTITUCIONALES_OFICIALES = Set.of(
        // Entidades nacionales
        "www.minvivienda.gov.co",
        "www.mvct.gov.co",
        "www.dnp.gov.co",
        "www.dapre.gov.co",
        "www.presidencia.gov.co",

        // Gobernaciones
        "www.antioquia.gov.co",
        "www.cundinamarca.gov.co",
        "www.valledelcauca.gov.co",
        "www.santander.gov.co",
        "www.atlantico.gov.co",
        "www.bolivar.gov.co",
        "www.risaralda.gov.co",
        "www.caldas.gov.co",
        "www.quindio.gov.co",
        "www.tolima.gov.co",
        "www.huila.gov.co",
        "www.narino.gov.co",
        "www.meta.gov.co",
        "www.cordoba.gov.co",
        "www.cesar.gov.co",
        "www.sucre.gov.co",
        "www.cauca.gov.co",
        "www.boyaca.gov.co",
        "www.casanare.gov.co",
        "www.caqueta.gov.co",
        "www.laguajira.gov.co",
        "www.arauca.gov.co",
        "www.putumayo.gov.co",
        "www.choco.gov.co",
        "www.amazonas.gov.co",
        "www.guainia.gov.co",
        "www.guaviare.gov.co",
        "www.vaupes.gov.co",
        "www.vichada.gov.co"
    );

    // Patrones de dominios válidos para entidades gubernamentales
    private static final List<Pattern> PATRONES_DOMINIOS_VALIDOS = List.of(
        // Dominios .gov.co oficiales
        Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9-]*\\.gov\\.co$"),

        // Alcaldías con patrón estándar
        Pattern.compile("^www\\.[a-zA-Z][a-zA-Z0-9-]*\\.gov\\.co$"),

        // Curadurías con patrón verificado
        Pattern.compile("^curaduria[1-9]\\d*[a-zA-Z]+\\.(com|org|gov\\.co)$"),

        // Instituciones educativas oficiales
        Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9-]*\\.edu\\.co$")
    );

    /**
     * Valida si un dominio pertenece a una entidad gubernamental legítima
     */
    public ResultadoValidacionDominio validarDominioGubernamental(String dominio, String tipoEntidad) {
        if (!validacionHabilitada) {
            logger.debug("Validación de dominios deshabilitada");
            return new ResultadoValidacionDominio(true, "Validación deshabilitada", TipoValidacion.OMITIDA);
        }

        if (dominio == null || dominio.trim().isEmpty()) {
            return new ResultadoValidacionDominio(false, "Dominio no puede ser nulo o vacío", TipoValidacion.ERROR);
        }

        // Normalizar dominio
        String dominioNormalizado = normalizarDominio(dominio);

        // 1. Verificar si está en lista blanca
        if (estaEnListaBlanca(dominioNormalizado, tipoEntidad)) {
            return new ResultadoValidacionDominio(true, "Dominio verificado en lista oficial", TipoValidacion.LISTA_BLANCA);
        }

        // 2. Verificar patrones válidos
        if (!cumplePatronValido(dominioNormalizado)) {
            return new ResultadoValidacionDominio(false,
                "Dominio no cumple con patrones oficiales gubernamentales", TipoValidacion.PATRON_INVALIDO);
        }

        // 3. Verificar accesibilidad del dominio
        ResultadoValidacionDominio resultadoAccesibilidad = verificarAccesibilidadDominio(dominioNormalizado);
        if (!resultadoAccesibilidad.isValido()) {
            return resultadoAccesibilidad;
        }

        // 4. Validación específica por tipo de entidad
        return validarSegunTipoEntidad(dominioNormalizado, tipoEntidad);
    }

    /**
     * Verifica si el dominio está en la lista blanca oficial
     */
    private boolean estaEnListaBlanca(String dominio, String tipoEntidad) {
        switch (tipoEntidad.toUpperCase()) {
            case "ALCALDIA":
                return DOMINIOS_OFICIALES_ALCALDIAS.contains(dominio);
            case "CURADURIA_URBANA":
                return DOMINIOS_OFICIALES_CURADURIAS.contains(dominio);
            case "GOBERNACION":
            case "SECRETARIA_PLANEACION":
            case "INSTITUTO_DESARROLLO_URBANO":
                return DOMINIOS_INSTITUCIONALES_OFICIALES.contains(dominio);
            default:
                return DOMINIOS_OFICIALES_ALCALDIAS.contains(dominio) ||
                       DOMINIOS_OFICIALES_CURADURIAS.contains(dominio) ||
                       DOMINIOS_INSTITUCIONALES_OFICIALES.contains(dominio);
        }
    }

    /**
     * Verifica si el dominio cumple con patrones válidos
     */
    private boolean cumplePatronValido(String dominio) {
        return PATRONES_DOMINIOS_VALIDOS.stream()
                .anyMatch(patron -> patron.matcher(dominio).matches());
    }

    /**
     * Verifica que el dominio sea accesible y responda
     */
    private ResultadoValidacionDominio verificarAccesibilidadDominio(String dominio) {
        try {
            String urlCompleta = dominio.startsWith("http") ? dominio : "https://" + dominio;
            URL url = new URL(urlCompleta);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(timeoutMs);
            connection.setReadTimeout(timeoutMs);
            connection.setRequestProperty("User-Agent", "PlataformaTramites/1.0 DomainValidator");

            int responseCode = connection.getResponseCode();

            if (responseCode >= 200 && responseCode < 400) {
                return new ResultadoValidacionDominio(true,
                    "Dominio accesible (HTTP " + responseCode + ")", TipoValidacion.ACCESIBILIDAD);
            } else {
                return new ResultadoValidacionDominio(false,
                    "Dominio no accesible (HTTP " + responseCode + ")", TipoValidacion.ACCESIBILIDAD);
            }

        } catch (IOException e) {
            logger.warn("Error verificando accesibilidad del dominio {}: {}", dominio, e.getMessage());
            return new ResultadoValidacionDominio(false,
                "Dominio no accesible: " + e.getMessage(), TipoValidacion.ACCESIBILIDAD);
        }
    }

    /**
     * Validación específica según el tipo de entidad
     */
    private ResultadoValidacionDominio validarSegunTipoEntidad(String dominio, String tipoEntidad) {
        switch (tipoEntidad.toUpperCase()) {
            case "ALCALDIA":
                return validarDominioAlcaldia(dominio);
            case "CURADURIA_URBANA":
                return validarDominioCuraduria(dominio);
            case "GOBERNACION":
                return validarDominioGobernacion(dominio);
            default:
                if (dominio.endsWith(".gov.co")) {
                    return new ResultadoValidacionDominio(true,
                        "Dominio .gov.co válido para entidad gubernamental", TipoValidacion.TIPO_ESPECIFICO);
                } else {
                    return new ResultadoValidacionDominio(false,
                        "Tipo de entidad requiere dominio .gov.co", TipoValidacion.TIPO_ESPECIFICO);
                }
        }
    }

    private ResultadoValidacionDominio validarDominioAlcaldia(String dominio) {
        if (dominio.endsWith(".gov.co")) {
            return new ResultadoValidacionDominio(true,
                "Dominio .gov.co válido para alcaldía", TipoValidacion.TIPO_ESPECIFICO);
        }
        return new ResultadoValidacionDominio(false,
            "Las alcaldías deben usar dominios .gov.co oficiales", TipoValidacion.TIPO_ESPECIFICO);
    }

    private ResultadoValidacionDominio validarDominioCuraduria(String dominio) {
        // Las curadurías pueden usar dominios .com verificados o .gov.co
        if (dominio.endsWith(".gov.co") ||
            (dominio.contains("curaduria") && (dominio.endsWith(".com") || dominio.endsWith(".org")))) {
            return new ResultadoValidacionDominio(true,
                "Dominio válido para curaduría urbana", TipoValidacion.TIPO_ESPECIFICO);
        }
        return new ResultadoValidacionDominio(false,
            "Las curadurías deben usar dominios oficiales verificados", TipoValidacion.TIPO_ESPECIFICO);
    }

    private ResultadoValidacionDominio validarDominioGobernacion(String dominio) {
        if (dominio.endsWith(".gov.co")) {
            return new ResultadoValidacionDominio(true,
                "Dominio .gov.co válido para gobernación", TipoValidacion.TIPO_ESPECIFICO);
        }
        return new ResultadoValidacionDominio(false,
            "Las gobernaciones deben usar dominios .gov.co oficiales", TipoValidacion.TIPO_ESPECIFICO);
    }

    private String normalizarDominio(String dominio) {
        if (dominio == null) return "";

        // Remover protocolo si está presente
        dominio = dominio.replaceFirst("^https?://", "");

        // Remover path si está presente
        dominio = dominio.split("/")[0];

        // Convertir a minúsculas
        dominio = dominio.toLowerCase().trim();

        return dominio;
    }

    /**
     * Obtiene todos los dominios oficiales verificados
     */
    public Set<String> obtenerDominiosOficialesVerificados() {
        Set<String> todosLosDominios = new HashSet<>();
        todosLosDominios.addAll(DOMINIOS_OFICIALES_ALCALDIAS);
        todosLosDominios.addAll(DOMINIOS_OFICIALES_CURADURIAS);
        todosLosDominios.addAll(DOMINIOS_INSTITUCIONALES_OFICIALES);
        return Collections.unmodifiableSet(todosLosDominios);
    }

    public static class ResultadoValidacionDominio {
        private final boolean valido;
        private final String mensaje;
        private final TipoValidacion tipoValidacion;

        public ResultadoValidacionDominio(boolean valido, String mensaje, TipoValidacion tipoValidacion) {
            this.valido = valido;
            this.mensaje = mensaje;
            this.tipoValidacion = tipoValidacion;
        }

        public boolean isValido() { return valido; }
        public String getMensaje() { return mensaje; }
        public TipoValidacion getTipoValidacion() { return tipoValidacion; }
    }

    public enum TipoValidacion {
        LISTA_BLANCA,
        PATRON_INVALIDO,
        ACCESIBILIDAD,
        TIPO_ESPECIFICO,
        ERROR,
        OMITIDA
    }
}