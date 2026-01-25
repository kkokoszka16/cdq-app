package com.banking.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(createApiInfo())
                .servers(createServers());
    }

    private Info createApiInfo() {
        return new Info()
                .title("Bank Transaction Aggregator API")
                .description("REST API for importing and analyzing bank transactions")
                .version("1.0.0")
                .contact(new Contact()
                        .name("API Support")
                        .email("support@example.com")
                );
    }

    private List<Server> createServers() {
        var localServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("Local development server");

        return List.of(localServer);
    }
}
