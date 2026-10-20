package com.plazoleta.usuarios.infrastructure.configuration;

import com.plazoleta.usuarios.domain.api.UserServicePort;
import com.plazoleta.usuarios.domain.spi.PasswordEncoderPort;
import com.plazoleta.usuarios.domain.spi.UserPersistencePort;
import com.plazoleta.usuarios.domain.usecase.UserUseCase;
import com.plazoleta.usuarios.infrastructure.output.jpa.adapter.BcryptPasswordEncoderAdapter;
import com.plazoleta.usuarios.infrastructure.output.jpa.adapter.UserJpaAdapter;
import com.plazoleta.usuarios.infrastructure.output.jpa.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.plazoleta.usuarios.domain.spi.TokenPort;
import com.plazoleta.usuarios.infrastructure.security.JwtTokenAdapter;

/** AquÃ­ "conectamos" los puertos con sus adaptadores. */
@Configuration
public class BeanConfiguration implements WebMvcConfigurer {

    @Bean
    public UserPersistencePort userPersistencePort(UserRepository repository) {
        return new UserJpaAdapter(repository);
    }

    @Bean
    public PasswordEncoderPort passwordEncoderPort() {
        return new BcryptPasswordEncoderAdapter();
    }

    @Bean
public JwtTokenAdapter tokenPort(
        @org.springframework.beans.factory.annotation.Value("${jwt.secret}") String secreto,
        @org.springframework.beans.factory.annotation.Value("${jwt.expiration}") long expiracion) {

    return new JwtTokenAdapter(secreto, expiracion);
}

    @Bean
    public UserServicePort userServicePort(UserPersistencePort persistencePort, PasswordEncoderPort encoderPort) {
        return new UserUseCase(persistencePort, encoderPort);
    }

    /** Permite que el frontend HTML (otro puerto/origen) llame a esta API. */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins("*").allowedMethods("*").allowedHeaders("*");
    }
}
