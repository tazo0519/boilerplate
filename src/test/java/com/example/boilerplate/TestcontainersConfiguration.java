package com.example.boilerplate;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * 테스트용 PostgreSQL 컨테이너.
 *
 * <p>{@code @ServiceConnection} 이 컨테이너의 접속 정보를 스프링 데이터소스로 자동 연결한다.
 * 스키마는 운영과 동일하게 Flyway(db/migration)가 컨텍스트 부팅 시 적용하고,
 * {@code ddl-auto: validate} 가 엔티티 매핑과 마이그레이션 DDL 사이의 드리프트를 검증한다.
 *
 * <p>컨테이너는 스프링 컨텍스트 캐시를 공유하는 테스트들 사이에서 한 번만 기동된다.
 * 실행에는 Docker 데몬이 필요하다.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:16"));
    }
}
