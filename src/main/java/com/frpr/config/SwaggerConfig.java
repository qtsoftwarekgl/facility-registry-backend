package com.frpr.config;

/*
@Configuration
@EnableSwagger2*/
public class SwaggerConfig {
} /*{

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String DEFAULT_INCLUDE_PATTERN = "/*";
    private final Logger log = LoggerFactory.getLogger(SwaggerConfig.class);


    @Bean
    public Docket postsApi() {

        Docket docket = new Docket(DocumentationType.SWAGGER_2).groupName("public-api")
                .apiInfo(apiInfo()).select().paths(PathSelectors.ant("/*")).build()

                *//*.apiInfo(ApiInfo.DEFAULT)
                .forCodeGeneration(true)
                .genericModelSubstitutes(ResponseEntity.class)
                .ignoredParameterTypes(Pageable.class)
                .ignoredParameterTypes(java.sql.Date.class)
                .directModelSubstitute(java.time.LocalDate.class, java.sql.Date.class)
                .directModelSubstitute(java.time.ZonedDateTime.class, Date.class)
                .directModelSubstitute(java.time.LocalDateTime.class, Date.class)*//*
                //   .securityContexts(Collections.singletonList(securityContext()))
                .securitySchemes(Collections.singletonList(apiKey()));

        //   docket = docket.

        return docket;
       *//* return new Docket(DocumentationType.SWAGGER_2).groupName("public-api")
                .apiInfo(apiInfo()).select().paths(postPaths()).build().
                securityContexts(Collections.singletonList(securityContext()))
                .securitySchemes(Collections.singletonList(basicAuthScheme()));*//*
    }*//*

    private Predicate postPaths() {
        return (regex("/.*"), regex("/.*");
    }*//*

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("FR and PR API")
                .description("FR and PR API reference for developers")
                .termsOfServiceUrl("http://localhost")
                .contact(new Contact("muthu.gennext@gmail.com", "", "muthu.gennext@gmail.com")).license("Gennext License")
                .licenseUrl("muthu.gennext@gmail.com").version("1.0").build();
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.regex(DEFAULT_INCLUDE_PATTERN))
                .build();
    }

    private ApiKey apiKey() {
        return new ApiKey("Authorization", AUTHORIZATION_HEADER, "header");
    }

    List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope
                = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Collections.singletonList(
                new SecurityReference("JWT", authorizationScopes));
    }

    private SecurityScheme basicAuthScheme() {
        return new BasicAuth("basicAuth");
    }

    private SecurityReference basicAuthReference() {
        return new SecurityReference("basicAuth", new AuthorizationScope[0]);
    }
}*/