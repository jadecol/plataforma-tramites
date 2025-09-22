package com.gestion.tramites.service;

import com.gestion.tramites.model.ConsecutivoRadicacion;
import com.gestion.tramites.model.Entidad;
import com.gestion.tramites.model.Tramite;
import com.gestion.tramites.repository.ConsecutivoRadicacionRepository;
import com.gestion.tramites.repository.TramiteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
public class ValidacionRadicacionService {

    private static final Logger logger = LoggerFactory.getLogger(ValidacionRadicacionService.class);

    @Autowired
    private ConsecutivoRadicacionRepository consecutivoRepository;

    @Autowired
    private TramiteRepository tramiteRepository;

    // Patrones de validación para números de radicación
    private static final Pattern PATRON_SECRETARIA = Pattern.compile("^\\d{5}-0-\\d{2}-\\d{4}$");
    private static final Pattern PATRON_CURADURIA = Pattern.compile("^\\d{5}-CUR-\\d{2}-\\d{4}$");
    private static final Pattern PATRON_GENERAL = Pattern.compile("^\\d{5}-(0|CUR)-\\d{2}-\\d{4}$");

    /**
     * Valida completamente un número de radicación
     */
    public ResultadoValidacionRadicacion validarNumeroRadicacion(String numeroRadicacion, Entidad entidad) {
        logger.debug("Validando número de radicación: {} para entidad: {}", numeroRadicacion, entidad.getId());

        // 1. Validación de formato
        ResultadoValidacionRadicacion validacionFormato = validarFormato(numeroRadicacion);
        if (!validacionFormato.isValido()) {
            return validacionFormato;
        }

        // 2. Validación de componentes
        ResultadoValidacionRadicacion validacionComponentes = validarComponentes(numeroRadicacion, entidad);
        if (!validacionComponentes.isValido()) {
            return validacionComponentes;
        }

        // 3. Validación de unicidad
        ResultadoValidacionRadicacion validacionUnicidad = validarUnicidad(numeroRadicacion);
        if (!validacionUnicidad.isValido()) {
            return validacionUnicidad;
        }

        // 4. Validación de secuencia consecutiva
        ResultadoValidacionRadicacion validacionSecuencia = validarSecuenciaConsecutiva(numeroRadicacion, entidad);
        if (!validacionSecuencia.isValido()) {
            return validacionSecuencia;
        }

        logger.info("Número de radicación válido: {}", numeroRadicacion);
        return new ResultadoValidacionRadicacion(true, "Número de radicación válido", TipoValidacionRadicacion.COMPLETA);
    }

    /**
     * Valida el formato del número de radicación
     */
    public ResultadoValidacionRadicacion validarFormato(String numeroRadicacion) {
        if (numeroRadicacion == null || numeroRadicacion.trim().isEmpty()) {
            return new ResultadoValidacionRadicacion(false, "El número de radicación no puede estar vacío", TipoValidacionRadicacion.FORMATO);
        }

        String numero = numeroRadicacion.trim().toUpperCase();

        if (!PATRON_GENERAL.matcher(numero).matches()) {
            return new ResultadoValidacionRadicacion(false,
                "Formato inválido. Debe ser: DANE-0-YY-NNNN (Secretaría) o DANE-CUR-YY-NNNN (Curaduría)",
                TipoValidacionRadicacion.FORMATO);
        }

        return new ResultadoValidacionRadicacion(true, "Formato válido", TipoValidacionRadicacion.FORMATO);
    }

    /**
     * Valida los componentes del número de radicación
     */
    public ResultadoValidacionRadicacion validarComponentes(String numeroRadicacion, Entidad entidad) {
        String[] partes = numeroRadicacion.split("-");

        // Validar código DANE
        String codigoDane = partes[0];
        if (!codigoDane.equals(entidad.getCodigoDane())) {
            return new ResultadoValidacionRadicacion(false,
                String.format("Código DANE no coincide. Esperado: %s, Recibido: %s",
                    entidad.getCodigoDane(), codigoDane),
                TipoValidacionRadicacion.COMPONENTES);
        }

        // Validar tipo de entidad
        String tipoEntidad = partes[1];
        ConsecutivoRadicacion.TipoEntidadRadicacion tipoEsperado = determinarTipoEntidad(entidad);
        if (!tipoEntidad.equals(tipoEsperado.getCodigo())) {
            return new ResultadoValidacionRadicacion(false,
                String.format("Tipo de entidad no válido. Esperado: %s, Recibido: %s",
                    tipoEsperado.getCodigo(), tipoEntidad),
                TipoValidacionRadicacion.COMPONENTES);
        }

        // Validar año
        String anoStr = partes[2];
        int anoActual = LocalDate.now().getYear() % 100;
        try {
            int ano = Integer.parseInt(anoStr);
            if (ano != anoActual) {
                return new ResultadoValidacionRadicacion(false,
                    String.format("Año no válido. Esperado: %02d, Recibido: %s", anoActual, anoStr),
                    TipoValidacionRadicacion.COMPONENTES);
            }
        } catch (NumberFormatException e) {
            return new ResultadoValidacionRadicacion(false, "Año debe ser numérico", TipoValidacionRadicacion.COMPONENTES);
        }

        // Validar número consecutivo
        String consecutivoStr = partes[3];
        try {
            int consecutivo = Integer.parseInt(consecutivoStr);
            if (consecutivo <= 0 || consecutivo > 9999) {
                return new ResultadoValidacionRadicacion(false,
                    "Número consecutivo debe estar entre 0001 y 9999",
                    TipoValidacionRadicacion.COMPONENTES);
            }
        } catch (NumberFormatException e) {
            return new ResultadoValidacionRadicacion(false,
                "Número consecutivo debe ser numérico",
                TipoValidacionRadicacion.COMPONENTES);
        }

        return new ResultadoValidacionRadicacion(true, "Componentes válidos", TipoValidacionRadicacion.COMPONENTES);
    }

    /**
     * Valida que el número de radicación sea único
     */
    public ResultadoValidacionRadicacion validarUnicidad(String numeroRadicacion) {
        Optional<Tramite> tramiteExistente = tramiteRepository.findByNumeroRadicacion(numeroRadicacion);

        if (tramiteExistente.isPresent()) {
            return new ResultadoValidacionRadicacion(false,
                String.format("El número de radicación %s ya existe para el trámite ID: %d",
                    numeroRadicacion, tramiteExistente.get().getIdTramite()),
                TipoValidacionRadicacion.UNICIDAD);
        }

        return new ResultadoValidacionRadicacion(true, "Número único", TipoValidacionRadicacion.UNICIDAD);
    }

    /**
     * Valida que el número consecutivo mantenga la secuencia
     */
    public ResultadoValidacionRadicacion validarSecuenciaConsecutiva(String numeroRadicacion, Entidad entidad) {
        Integer consecutivo = ConsecutivoRadicacion.extraerConsecutivo(numeroRadicacion);
        if (consecutivo == null) {
            return new ResultadoValidacionRadicacion(false,
                "No se pudo extraer el número consecutivo",
                TipoValidacionRadicacion.SECUENCIA);
        }

        ConsecutivoRadicacion.TipoEntidadRadicacion tipoEntidad = determinarTipoEntidad(entidad);
        int anoActual = LocalDate.now().getYear();

        Optional<ConsecutivoRadicacion> consecutivoActual =
            consecutivoRepository.findByEntidadAndTipoEntidadAndAnoAndActivoTrue(entidad, tipoEntidad, anoActual);

        if (consecutivoActual.isPresent()) {
            int ultimoConsecutivo = consecutivoActual.get().getUltimoConsecutivo();
            int siguienteEsperado = ultimoConsecutivo + 1;

            if (consecutivo != siguienteEsperado) {
                return new ResultadoValidacionRadicacion(false,
                    String.format("Número consecutivo fuera de secuencia. Esperado: %04d, Recibido: %04d",
                        siguienteEsperado, consecutivo),
                    TipoValidacionRadicacion.SECUENCIA);
            }
        } else {
            // Primera radicación del año, debe ser 0001
            if (consecutivo != 1) {
                return new ResultadoValidacionRadicacion(false,
                    "Primera radicación del año debe ser 0001",
                    TipoValidacionRadicacion.SECUENCIA);
            }
        }

        return new ResultadoValidacionRadicacion(true, "Secuencia consecutiva válida", TipoValidacionRadicacion.SECUENCIA);
    }

    /**
     * Valida múltiples números de radicación en lote
     */
    @Transactional(readOnly = true)
    public List<ResultadoValidacionRadicacion> validarLoteRadicaciones(List<String> numerosRadicacion, Entidad entidad) {
        logger.info("Validando lote de {} radicaciones para entidad: {}", numerosRadicacion.size(), entidad.getId());

        return numerosRadicacion.stream()
            .map(numero -> validarNumeroRadicacion(numero, entidad))
            .toList();
    }

    /**
     * Detecta posibles problemas en la secuencia de consecutivos
     */
    @Transactional(readOnly = true)
    public List<ProblemaConsecutivo> detectarProblemasSecuencia(Entidad entidad, int ano) {
        List<ProblemaConsecutivo> problemas = new java.util.ArrayList<>();

        ConsecutivoRadicacion.TipoEntidadRadicacion[] tipos = ConsecutivoRadicacion.TipoEntidadRadicacion.values();

        for (ConsecutivoRadicacion.TipoEntidadRadicacion tipo : tipos) {
            Optional<ConsecutivoRadicacion> consecutivo =
                consecutivoRepository.findByEntidadAndTipoEntidadAndAnoAndActivoTrue(entidad, tipo, ano);

            if (consecutivo.isPresent()) {
                // Verificar saltos en la secuencia
                String anoCorto = String.format("%02d", ano % 100);
                List<Integer> consecutivosUsados = tramiteRepository
                    .findConsecutivosUsadosByEntidadAndTipoAndAno(entidad, tipo.getCodigo(), anoCorto);

                for (int i = 1; i <= consecutivo.get().getUltimoConsecutivo(); i++) {
                    if (!consecutivosUsados.contains(i)) {
                        problemas.add(new ProblemaConsecutivo(
                            tipo,
                            ano,
                            i,
                            "Salto en secuencia - consecutivo no utilizado",
                            TipoProblema.SALTO_SECUENCIA
                        ));
                    }
                }

                // Verificar duplicados
                List<Integer> duplicados = tramiteRepository
                    .findConsecutivosDuplicadosByEntidadAndTipoAndAno(entidad, tipo.getCodigo(), anoCorto);

                for (Integer duplicado : duplicados) {
                    problemas.add(new ProblemaConsecutivo(
                        tipo,
                        ano,
                        duplicado,
                        "Consecutivo duplicado detectado",
                        TipoProblema.DUPLICADO
                    ));
                }
            }
        }

        if (!problemas.isEmpty()) {
            logger.warn("Detectados {} problemas de secuencia para entidad {} en año {}",
                problemas.size(), entidad.getId(), ano);
        }

        return problemas;
    }

    /**
     * Determina el tipo de entidad para radicación basado en las características de la entidad
     */
    public ConsecutivoRadicacion.TipoEntidadRadicacion determinarTipoEntidad(Entidad entidad) {
        String nombre = entidad.getNombre().toLowerCase();

        if (nombre.contains("curadur") || nombre.contains("curador")) {
            return ConsecutivoRadicacion.TipoEntidadRadicacion.CURADURIA;
        } else {
            return ConsecutivoRadicacion.TipoEntidadRadicacion.SECRETARIA;
        }
    }

    /**
     * Genera un reporte de validación para auditoría
     */
    @Transactional(readOnly = true)
    public ReporteValidacionRadicacion generarReporteValidacion(Entidad entidad, int ano) {
        List<ProblemaConsecutivo> problemas = detectarProblemasSecuencia(entidad, ano);

        long totalRadicaciones = tramiteRepository.countByEntidadAndAno(entidad, ano);
        long radicacionesValidas = totalRadicaciones - problemas.size();

        return new ReporteValidacionRadicacion(
            entidad.getId(),
            entidad.getNombre(),
            ano,
            totalRadicaciones,
            radicacionesValidas,
            problemas.size(),
            problemas
        );
    }

    // DTOs para resultados de validación
    public static class ResultadoValidacionRadicacion {
        private final boolean valido;
        private final String mensaje;
        private final TipoValidacionRadicacion tipoValidacion;

        public ResultadoValidacionRadicacion(boolean valido, String mensaje, TipoValidacionRadicacion tipoValidacion) {
            this.valido = valido;
            this.mensaje = mensaje;
            this.tipoValidacion = tipoValidacion;
        }

        public boolean isValido() { return valido; }
        public String getMensaje() { return mensaje; }
        public TipoValidacionRadicacion getTipoValidacion() { return tipoValidacion; }
    }

    public enum TipoValidacionRadicacion {
        FORMATO, COMPONENTES, UNICIDAD, SECUENCIA, COMPLETA
    }

    public static class ProblemaConsecutivo {
        private final ConsecutivoRadicacion.TipoEntidadRadicacion tipoEntidad;
        private final int ano;
        private final int consecutivo;
        private final String descripcion;
        private final TipoProblema tipoProblema;

        public ProblemaConsecutivo(ConsecutivoRadicacion.TipoEntidadRadicacion tipoEntidad, int ano, int consecutivo,
                                 String descripcion, TipoProblema tipoProblema) {
            this.tipoEntidad = tipoEntidad;
            this.ano = ano;
            this.consecutivo = consecutivo;
            this.descripcion = descripcion;
            this.tipoProblema = tipoProblema;
        }

        public ConsecutivoRadicacion.TipoEntidadRadicacion getTipoEntidad() { return tipoEntidad; }
        public int getAno() { return ano; }
        public int getConsecutivo() { return consecutivo; }
        public String getDescripcion() { return descripcion; }
        public TipoProblema getTipoProblema() { return tipoProblema; }
    }

    public enum TipoProblema {
        SALTO_SECUENCIA, DUPLICADO, FORMATO_INVALIDO
    }

    public static class ReporteValidacionRadicacion {
        private final Long entidadId;
        private final String nombreEntidad;
        private final int ano;
        private final long totalRadicaciones;
        private final long radicacionesValidas;
        private final long problemasDetectados;
        private final List<ProblemaConsecutivo> problemas;

        public ReporteValidacionRadicacion(Long entidadId, String nombreEntidad, int ano,
                                         long totalRadicaciones, long radicacionesValidas,
                                         long problemasDetectados, List<ProblemaConsecutivo> problemas) {
            this.entidadId = entidadId;
            this.nombreEntidad = nombreEntidad;
            this.ano = ano;
            this.totalRadicaciones = totalRadicaciones;
            this.radicacionesValidas = radicacionesValidas;
            this.problemasDetectados = problemasDetectados;
            this.problemas = problemas;
        }

        // Getters
        public Long getEntidadId() { return entidadId; }
        public String getNombreEntidad() { return nombreEntidad; }
        public int getAno() { return ano; }
        public long getTotalRadicaciones() { return totalRadicaciones; }
        public long getRadicacionesValidas() { return radicacionesValidas; }
        public long getProblemasDetectados() { return problemasDetectados; }
        public List<ProblemaConsecutivo> getProblemas() { return problemas; }
        public double getPorcentajeValidez() {
            return totalRadicaciones > 0 ? (double) radicacionesValidas / totalRadicaciones * 100 : 100.0;
        }
    }
}