package com.gestion.tramites;

import com.gestion.tramites.model.Tramite;
import com.gestion.tramites.tramite.dto.TramiteRequestDTO;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies; // Importar esto
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PlataformaTramitesApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlataformaTramitesApplication.class, args);
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Configurar ModelMapper para un mapeo más estricto
        // y evitar conflictos de nombres de propiedades similares.
        // Aquí puedes elegir entre STRICT, STANDARD o LOOSE.
        // STRICT es generalmente preferible para evitar ambigüedades.
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);

        // Configuración específica para el mapeo de TramiteRequestDTO a Tramite
        // Ignoramos las propiedades que se manejan manualmente o se autogeneran
        modelMapper.createTypeMap(TramiteRequestDTO.class, Tramite.class)
                .addMappings(mapper -> {
                    // Ignora el ID propio del trámite, que es generado automáticamente
                    mapper.skip(Tramite::setIdTramite);

                    // Ignora el mapeo directo a los objetos de entidad completos (Solicitante, Entidad, Revisor),
                    // ya que en el servicio se buscan y asignan manualmente.
                    // Esto es diferente a los IDs del DTO, esto evita mapear el objeto completo.
                    mapper.skip(Tramite::setSolicitante);
                    mapper.skip(Tramite::setEntidad);
                    mapper.skip(Tramite::setRevisor);

                    // Ignora el estado y fechas, ya que se manejan en la lógica de negocio
                    mapper.skip(Tramite::setEstado);
                    mapper.skip(Tramite::setFechaCreacion);
                    mapper.skip(Tramite::setFechaActualizacion);
                    mapper.skip(Tramite::setFechaFinalizacion);
                });

        return modelMapper;
    }
}