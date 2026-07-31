# syntax=docker/dockerfile:1

# =====================================================================
# Build stage — Gradle wrapper(9.5.1) + JDK 21 로 실행 가능한 boot jar 생성
# =====================================================================
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# 빌드 스크립트/래퍼 먼저 복사(레이어 캐시), 소스는 이후 복사
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew
COPY src src

# BuildKit 캐시 마운트로 Gradle 다운로드를 빌드 간 재사용. 실패는 숨기지 않고 즉시 노출.
# (bootJar 단독 실행 → build/libs 에는 실행 가능한 jar 하나만 생성)
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon clean bootJar -x test

# =====================================================================
# Runtime stage — JRE 21, non-root 로 실행
# =====================================================================
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# root 로 실행하지 않도록 전용 유저 생성 + 작업 디렉터리 소유권 부여
# (힙덤프 등 /app 하위 쓰기가 필요할 수 있어 소유권을 명시한다)
RUN groupadd --system spring && useradd --system --gid spring spring \
    && chown spring:spring /app

# jar 소유권을 실행 유저로 지정
COPY --chown=spring:spring --from=build /workspace/build/libs/*-SNAPSHOT.jar app.jar
USER spring

EXPOSE 8080

# 컨테이너 메모리 한도를 힙에 반영. 프로필/키 등은 런타임 환경변수로 주입.
# exec 로 JVM 을 PID 1 로 교체 → SIGTERM 이 JVM 에 직접 전달되어 graceful shutdown 동작.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
