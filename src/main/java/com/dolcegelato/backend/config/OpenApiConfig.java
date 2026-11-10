package com.dolcegelato.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Documentación de la API con Swagger / OpenAPI (semana 11 del sílabo).
// Se abre en http://localhost:8080/swagger-ui.html
// Botón "Authorize": pegar el token que devuelve POST /api/usuarios/login para probar las rutas protegidas
@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_JWT = "jwt";

    @Bean
    public OpenAPI documentacion() {
        return new OpenAPI()
                .info(new Info()
                        .title("Dolce Gelato API")
                        .version("1.0")
                        .description("API REST del sistema de pedidos en línea de Dolce Gelato y Dolce Burger. "
                                + "Proyecto del curso Soluciones Web y Aplicaciones Distribuidas, UPN 2026."))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT))
                .components(new Components().addSecuritySchemes(ESQUEMA_JWT, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
