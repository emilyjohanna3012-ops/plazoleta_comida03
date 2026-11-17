package com.plazoleta.trazabilidad.infrastructure.configuration;

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
    public OpenAPI trazabilidadOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Plazoleta de comidas - Microservicio Trazabilidad")
                        .version("v1")
                        .description("Historial de estados de los pedidos (HU17) y eficiencia de los restaurantes (HU18). "
                                + "Inicia sesión en usuarios-ms, copia el token y pégalo en el botón Authorize."))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
