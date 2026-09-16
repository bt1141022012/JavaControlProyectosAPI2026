package org.esfe.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;

import java.util.List;

@OpenAPIDefinition(
        info = @Info(
                title = "Gestión de Proyectos API",
                description = "API Rest que permite el registro y mantenimiento de los datos de proyectos, categorías y tareas",
                version = "1.0.0"
        ),
        security = @SecurityRequirement(
                name = "Security Token"
        )
)
@SecurityScheme(
        name = "Security Token",
        description = "Acceso a la aplicación mediante Tokens JWT",
        type = SecuritySchemeType.HTTP,
        paramName = HttpHeaders.AUTHORIZATION,
        in = SecuritySchemeIn.HEADER,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        String codespaceName = System.getenv("CODESPACE_NAME");
        String forwardingDomain = System.getenv("GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN");
        String serverUrl = "http://localhost:8080";

        if (codespaceName != null && !codespaceName.isBlank()) {
            if (forwardingDomain != null && !forwardingDomain.isBlank()) {
                serverUrl = "https://" + codespaceName + "-8080." + forwardingDomain;
            } else {
                serverUrl = "https://" + codespaceName + "-8080.app.github.dev";
            }
        }

        Server server = new Server()
                .url(serverUrl)
                .description("Servidor actual");

        return new OpenAPI().servers(List.of(server));
    }
}
