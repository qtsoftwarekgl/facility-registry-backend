package com.frpr.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfigNew {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .setGroup("springshop-public")
                .pathsToMatch("/**")
                .build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Facility Registry").version("1.0.0"))
                // Components section defines Security Scheme "mySecretHeader"
                .components(new Components()
                        .addSecuritySchemes("mySecretHeader", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("x-auth-token")))
                // AddSecurityItem section applies created scheme globally
                .addSecurityItem(new SecurityRequirement().addList("mySecretHeader"));
    }


   /* @Bean
    public OperationCustomizer customGlobalHeaders() {

        return (Operation operation, HandlerMethod handlerMethod) -> {

            Parameter missingParam1 = new Parameter().in(ParameterIn.HEADER.toString()).schema(new StringSchema())
                    .name("Authorization").description("Authorization header description2").required(false);
            operation.addParametersItem(missingParam1);
            return operation;
        };
    }*/
}
