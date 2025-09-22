package com.gestion.tramites.service;

import com.gestion.tramites.dto.reportes.DashboardMetricasDTO;
import com.gestion.tramites.model.Tramite;
import com.gestion.tramites.repository.TramiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReporteService {

    @Autowired
    private TramiteRepository tramiteRepository;

    public DashboardMetricasDTO generarDashboardMetricas() {
        CustomUserDetails currentUser = getCurrentUser();

        List<Tramite> tramites;
        if (currentUser.isAdminGlobal()) {
            tramites = tramiteRepository.findAll();
        } else {
            tramites = tramiteRepository.findAll();
        }

        DashboardMetricasDTO metricas = new DashboardMetricasDTO();

        if (!currentUser.isAdminGlobal() && currentUser.tieneEntidad()) {
            metricas.setNombreEntidad(currentUser.getNombreEntidad());
        }

        calcularMetricasGenerales(tramites, metricas);
        calcularDistribucionEstados(tramites, metricas);
        calcularTiempoPromedioProcesamiento(tramites, metricas);
        calcularCargaRevisores(tramites, metricas);
        calcularTramitesPorTipo(tramites, metricas);
        calcularTasaAprobacion(tramites, metricas);
        calcularTramitesVencidos(tramites, metricas);

        return metricas;
    }

    private void calcularMetricasGenerales(List<Tramite> tramites, DashboardMetricasDTO metricas) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);

        metricas.setTotalTramites((long) tramites.size());

        long tramitesUltimoMes = tramites.stream()
            .filter(t -> t.getFechaRadicacion() != null && !t.getFechaRadicacion().isBefore(inicioMes))
            .count();
        metricas.setTramitesUltimoMes(tramitesUltimoMes);

        long tramitesHoy = tramites.stream()
            .filter(t -> t.getFechaRadicacion() != null && t.getFechaRadicacion().equals(hoy))
            .count();
        metricas.setTramitesHoy(tramitesHoy);
    }

    private void calcularDistribucionEstados(List<Tramite> tramites, DashboardMetricasDTO metricas) {
        Map<String, Long> distribucion = tramites.stream()
            .collect(Collectors.groupingBy(
                t -> t.getEstadoActual().name(),
                Collectors.counting()
            ));
        metricas.setDistribucionPorEstado(distribucion);
    }

    private void calcularTiempoPromedioProcesamiento(List<Tramite> tramites, DashboardMetricasDTO metricas) {
        List<Tramite> tramitesFinalizados = tramites.stream()
            .filter(Tramite::estaFinalizado)
            .filter(t -> t.getFechaFinalizacion() != null && t.getFechaCreacion() != null)
            .toList();

        if (!tramitesFinalizados.isEmpty()) {
            double promedioDias = tramitesFinalizados.stream()
                .mapToLong(t -> ChronoUnit.DAYS.between(
                    t.getFechaCreacion().toLocalDate(),
                    t.getFechaFinalizacion().toLocalDate()
                ))
                .average()
                .orElse(0.0);

            metricas.setTiempoPromedioProcesamientoDias(promedioDias);
        } else {
            metricas.setTiempoPromedioProcesamientoDias(0.0);
        }
    }

    private void calcularCargaRevisores(List<Tramite> tramites, DashboardMetricasDTO metricas) {
        Map<String, Long> cargaPorRevisor = tramites.stream()
            .filter(t -> t.getRevisorAsignado() != null)
            .filter(t -> !t.estaFinalizado())
            .collect(Collectors.groupingBy(
                t -> t.getRevisorAsignado().getNombreCompleto(),
                Collectors.counting()
            ));
        metricas.setCargaPorRevisor(cargaPorRevisor);
    }

    private void calcularTramitesPorTipo(List<Tramite> tramites, DashboardMetricasDTO metricas) {
        Map<String, Long> tramitesPorTipo = tramites.stream()
            .filter(t -> t.getTipoTramite() != null)
            .collect(Collectors.groupingBy(
                t -> t.getTipoTramite().getNombre(),
                Collectors.counting()
            ));
        metricas.setTramitesPorTipo(tramitesPorTipo);
    }

    private void calcularTasaAprobacion(List<Tramite> tramites, DashboardMetricasDTO metricas) {
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);

        List<Tramite> tramitesFinalizadosUltimoMes = tramites.stream()
            .filter(Tramite::estaFinalizado)
            .filter(t -> t.getFechaFinalizacion() != null &&
                        !t.getFechaFinalizacion().toLocalDate().isBefore(inicioMes))
            .toList();

        if (!tramitesFinalizadosUltimoMes.isEmpty()) {
            long tramitesAprobados = tramitesFinalizadosUltimoMes.stream()
                .filter(t -> t.getEstadoActual() == Tramite.EstadoTramite.APROBADO)
                .count();

            double tasaAprobacion = (double) tramitesAprobados / tramitesFinalizadosUltimoMes.size() * 100;
            metricas.setTasaAprobacionUltimoMes(tasaAprobacion);
        } else {
            metricas.setTasaAprobacionUltimoMes(0.0);
        }
    }

    private void calcularTramitesVencidos(List<Tramite> tramites, DashboardMetricasDTO metricas) {
        LocalDate hoy = LocalDate.now();

        long tramitesVencidos = tramites.stream()
            .filter(t -> !t.estaFinalizado())
            .filter(t -> t.getFechaLimiteCompletar() != null && t.getFechaLimiteCompletar().isBefore(hoy))
            .count();

        metricas.setTramitesVencidos(tramitesVencidos);
    }

    private CustomUserDetails getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            throw new IllegalStateException("Usuario no autenticado correctamente");
        }
        return (CustomUserDetails) principal;
    }
}