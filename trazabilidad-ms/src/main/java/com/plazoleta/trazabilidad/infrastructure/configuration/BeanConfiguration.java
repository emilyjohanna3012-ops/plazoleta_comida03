package com.plazoleta.trazabilidad.infrastructure.configuration;

import com.plazoleta.trazabilidad.domain.api.TrazaServicePort;
import com.plazoleta.trazabilidad.domain.spi.TrazaPersistencePort;
import com.plazoleta.trazabilidad.domain.usecase.TrazaUseCase;
import com.plazoleta.trazabilidad.infrastructure.output.mongo.adapter.TrazaMongoAdapter;
import com.plazoleta.trazabilidad.infrastructure.output.mongo.repository.TrazaMongoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Aquí "conectamos" los puertos con sus adaptadores. */
@Configuration
public class BeanConfiguration {

    @Bean
    public TrazaPersistencePort trazaPersistencePort(TrazaMongoRepository repository) {
        return new TrazaMongoAdapter(repository);
    }

    @Bean
    public TrazaServicePort trazaServicePort(TrazaPersistencePort trazaPersistencePort) {
        return new TrazaUseCase(trazaPersistencePort);
    }
}
