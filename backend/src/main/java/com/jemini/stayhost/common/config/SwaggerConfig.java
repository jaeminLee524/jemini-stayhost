package com.jemini.stayhost.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .components(new Components()
                .addSecuritySchemes("Bearer-Partner", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("파트너(Extranet) JWT 토큰"))
                .addSecuritySchemes("Bearer-User", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("고객(Customer) JWT 토큰")))
            .security(List.of(
                new SecurityRequirement().addList("Bearer-Partner"),
                new SecurityRequirement().addList("Bearer-User")
            ))
            .info(new Info()
                .title("StayHost API")
                .version("v1")
                .description("OTA 숙박 플랫폼 API. Extranet(파트너)과 Customer(고객) 토큰을 구분하여 사용합니다."));
    }

    @Bean
    public GroupedOpenApi extranetApi() {
        return GroupedOpenApi.builder()
            .group("1-extranet")
            .pathsToMatch("/api/extranet/**", "/api/public/extranet/**")
            .build();
    }

    @Bean
    public GroupedOpenApi customerApi() {
        return GroupedOpenApi.builder()
            .group("2-customer")
            .pathsToMatch("/api/public/users/**", "/api/public/search/**", "/api/public/properties/**",
                "/api/users/**", "/api/reservations/**")
            .build();
    }

    @Bean
    public GroupedOpenApi supplierApi() {
        return GroupedOpenApi.builder()
            .group("3-supplier")
            .pathsToMatch("/api/suppliers/**")
            .build();
    }

    @Bean
    public GroupedOpenApi commonApi() {
        return GroupedOpenApi.builder()
            .group("4-common")
            .pathsToMatch("/api/public/health", "/actuator/**")
            .build();
    }
}
