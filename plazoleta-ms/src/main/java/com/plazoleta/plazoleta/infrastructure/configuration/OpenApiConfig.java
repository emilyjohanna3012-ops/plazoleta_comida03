package com.plazoleta.plazoleta.infrastructure.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Datos de la cabecera de Swagger y botón "Authorize" para pegar el token. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI plazoletaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Plazoleta de comidas - Microservicio Plazoleta")
                        .version("v1")
                        .description("Gestión de restaurantes, platos y pedidos. Inicia sesión en usuarios-ms "
                                + "(POST /api/v1/usuarios/login), copia el token y pégalo en el botón Authorize."))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
