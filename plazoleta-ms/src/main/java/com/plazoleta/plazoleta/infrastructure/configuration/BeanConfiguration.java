package com.plazoleta.plazoleta.infrastructure.configuration;

import com.plazoleta.plazoleta.domain.api.PlatoServicePort;
import com.plazoleta.plazoleta.domain.api.RestauranteServicePort;
import com.plazoleta.plazoleta.domain.spi.PlatoPersistencePort;
import com.plazoleta.plazoleta.domain.spi.RestauranteConsultaPort;
import com.plazoleta.plazoleta.domain.spi.RestaurantePersistencePort;
import com.plazoleta.plazoleta.domain.spi.UsuarioConsultaPort;
import com.plazoleta.plazoleta.domain.usecase.PlatoUseCase;
import com.plazoleta.plazoleta.domain.usecase.RestauranteUseCase;
import com.plazoleta.plazoleta.infrastructure.output.jpa.adapter.PlatoJpaAdapter;
import com.plazoleta.plazoleta.infrastructure.output.jpa.adapter.RestauranteJpaAdapter;
import com.plazoleta.plazoleta.infrastructure.output.jpa.repository.PlatoRepository;
import com.plazoleta.plazoleta.infrastructure.output.jpa.repository.RestauranteRepository;
import com.plazoleta.plazoleta.infrastructure.output.rest.PropagarTokenInterceptor;
import com.plazoleta.plazoleta.infrastructure.output.rest.UsuarioRestClientAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/** Aquí "conectamos" los puertos con sus adaptadores. */
@Configuration
public class BeanConfiguration {

    @Bean
    public PlatoPersistencePort platoPersistencePort(PlatoRepository repository) {
        return new PlatoJpaAdapter(repository);
    }

    /** Un solo adaptador cumple dos puertos: RestaurantePersistencePort y RestauranteConsultaPort. */
    @Bean
    public RestauranteJpaAdapter restauranteJpaAdapter(RestauranteRepository repository) {
        return new RestauranteJpaAdapter(repository);
    }

    @Bean
    public UsuarioConsultaPort usuarioConsultaPort(RestClient.Builder builder,
                                                   @Value("${usuarios.url}") String usuariosUrl) {
        return new UsuarioRestClientAdapter(builder
                .baseUrl(usuariosUrl)
                .requestInterceptor(new PropagarTokenInterceptor())
                .build());
    }

    @Bean
    public PlatoServicePort platoServicePort(PlatoPersistencePort platoPersistencePort,
                                             RestauranteConsultaPort restauranteConsultaPort) {
        return new PlatoUseCase(platoPersistencePort, restauranteConsultaPort);
    }

    @Bean
    public RestauranteServicePort restauranteServicePort(RestaurantePersistencePort restaurantePersistencePort,
                                                         UsuarioConsultaPort usuarioConsultaPort) {
        return new RestauranteUseCase(restaurantePersistencePort, usuarioConsultaPort);
    }
}
