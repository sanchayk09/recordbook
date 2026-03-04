package com.urviclean.recordbook.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger Configuration
 * Access Swagger UI at: http://localhost:8080/swagger-ui.html
 * Access API Docs at: http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI recordbookOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("Local Development Server");

        Contact contact = new Contact();
        contact.setName("Urviclean Support");
        contact.setEmail("support@urviclean.com");

        License license = new License()
                .name("Proprietary")
                .url("https://urviclean.com/license");

        Info info = new Info()
                .title("Recordbook API - Urviclean")
                .version("1.0.0")
                .description("REST API for Urviclean Recordbook System - " +
                        "Manages sales records, warehouse inventory, salesman operations, " +
                        "daily expenses, and product management.")
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("recordbook-api")
                .pathsToMatch("/api/**")
                .packagesToScan("com.urviclean.recordbook.controllers")
                .build();
    }
}

