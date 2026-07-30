# syntax=docker/dockerfile:1

# =====================================================================
# Build stage — Gradle wrapper(9.5.1) + JDK 21 로 실행 가능한 boot jar 생성
# =====================================================================
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# 1) 의존성 캐시 레이어: 빌드 스크립트/래퍼만 먼저 복사
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

# 2) 소스 복사 후 boot jar 빌드 (plain jar 태스크는 실행하지 않음)
COPY src src
RUN ./gradlew --no-daemon clean bootJar -x test

# =====================================================================
# Runtime stage — JRE 21, non-root 로 실행
# =====================================================================
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# root 로 실행하지 않도록 전용 유저 생성
RUN groupadd --system spring && useradd --system --gid spring spring

# bootJar 단독 실행 시 build/libs 에는 실행 가능한 jar 하나만 생성됨
COPY --from=build /workspace/build/libs/*-SNAPSHOT.jar app.jar
USER spring

EXPOSE 8080

# 컨테이너 메모리 한도를 힙에 반영. 프로필/키 등은 런타임 환경변수로 주입
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
