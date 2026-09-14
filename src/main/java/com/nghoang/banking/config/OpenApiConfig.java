package com.nghoang.banking.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.Getter;
import lombok.Setter;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "open.api")
public class OpenApiConfig {
    private String title;
    private String description;
    private String version;
    private String serverUrl;
    private String serverName;
    SecurityScheme securityScheme = new SecurityScheme()
            .name("BearerAuth")
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT");
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .description(description)
                        .version(version)
                        .contact(new Contact()
                                .name("Nguyen Hoang")
                                .email("hoangpmhuevn@gmail.com")
                                .url("https://github.com/NguyenHoangdh"))
                        .license(new License()
                                .name("Banking App API License")
                                .url("https://github.com/NguyenHoangdh")))
                .servers(List.of(new Server().url(serverUrl).description(serverName)))
                .externalDocs(new ExternalDocumentation()
                        .description("Banking App Documentary")
                        .url("https://github.com/NguyenHoangdb"))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components().addSecuritySchemes("BearerAuth", securityScheme));
    }

    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("01. Tất cả API (All)")
                .pathsToMatch("/api/**", "/bankStatement/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userAndBankingApi() {
        return GroupedOpenApi.builder()
                .group("02. Tài khoản & Giao dịch (Banking)")
                .pathsToMatch("/api/user/**", "/bankStatement/**")
                .build();
    }

    @Bean
    public GroupedOpenApi authAndSecurityApi() {
        return GroupedOpenApi.builder()
                .group("03. Xác thực & Phân quyền (Security)")
                .pathsToMatch("/api/auth/**", "/api/roles/**", "/api/permissions/**")
                .build();
    }

}
