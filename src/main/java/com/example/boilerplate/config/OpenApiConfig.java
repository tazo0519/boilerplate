package com.example.boilerplate.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"local", "dev", "stg"})
public class OpenApiConfig {

    private final String applicationName;
    private final ObjectProvider<BuildProperties> buildProperties;

    public OpenApiConfig(@Value("${spring.application.name}") String applicationName,
                         ObjectProvider<BuildProperties> buildProperties) {
        this.applicationName = applicationName;
        this.buildProperties = buildProperties;
    }

    @Bean
    public OpenAPI api() {
        // 버전/이름을 하드코딩하지 않고 빌드 정보(build-info.properties)에서 읽는다.
        // build-info 가 없는 실행(예: IDE에서 buildInfo 태스크 미실행)에서는 "unknown".
        BuildProperties build = buildProperties.getIfAvailable();
        String version = build != null ? build.getVersion() : "unknown";
        return new OpenAPI()
                .info(new Info()
                        .title(applicationName + " API")
                        .version(version)
                        .description("Spring Boot Boilerplate API"));
    }
}
