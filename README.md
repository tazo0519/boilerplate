# boilerplate

Spring Boot 모놀리식 서비스 보일러플레이트 — **복사해서 새 서비스를 시작**한다.

- **스택**: Java 21 · Spring Boot 4.1 (Framework 7) · PostgreSQL 16 · Gradle
- **배포 전제**: ALB + ECS Fargate + Docker (로컬은 docker compose)
- **설계 관심사**: ① 제한(메서드·생성자를 의도된 용도로만 쓰게 표면 최소화) ② 커스텀 훅(보일러플레이트 수정 없이 변형) ③ 응답 래퍼 계약의 전층 수렴

---

## 1. 빠른 시작 (로컬)

```bash
# 전체 기동 (PostgreSQL + 앱)
docker compose up --build

# 또는 DB 만 컨테이너로 띄우고 앱은 IDE/gradle 로
docker compose up -d postgres
./gradlew bootRun            # 기본 local 프로필

# 테스트
./gradlew test               # 단위 (Docker 불필요)
./gradlew integrationTest    # 통합 (Testcontainers — Docker 필요)
```

- Swagger: `http://localhost:8080/swagger-ui.html` (local/dev/stg 만 — prod 차단)
- 헬스체크: `/actuator/health/readiness`, `/actuator/health/liveness` (액세스 로그에서 제외됨)

> **참고**: 로컬에서 `GET /goods` 는 예제 외부 API(더미 주소) 호출을 포함하므로 **502(`COMMON_EXTERNAL_API_ERROR`) 래퍼가 나오는 것이 정상**이다 — 외부 연동 에러 계약을 시연하는 예제. 또한 `docker-compose.yml` 의 `container_name` 이 고정이라 같은 호스트에서 이 저장소 복사본 두 개를 동시에 띄울 수 없다.

## 2. 새 서비스 시작 체크리스트 (복사 후 순서대로)

### 2-1. 이름 치환
| 위치 | 바꿀 것 |
|---|---|
| `settings.gradle` | `rootProject.name` |
| `build.gradle` | `group`, `description` |
| 패키지 | `com.example.boilerplate` → 새 패키지 (IDE rename 사용) |
| `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` | `CryptoAutoConfiguration` FQCN (패키지 rename 반영) |
| `application.yaml` | `spring.application.name` |
| 프로퍼티 prefix `boilerplate.*` | `EncryptionProperties`(`boilerplate.security.crypto`) · `CorsProperties`(`boilerplate.web.cors`) · `MdcLoggingProperties`(`boilerplate.logging`) 의 prefix 와 각 yaml 을 **함께** 변경 (또는 그대로 둬도 동작함) |
| `docker-compose.yml` | 컨테이너명, DB 명/계정 — **healthcheck 의 `pg_isready -U ... -d ...` 계정도 함께** (누락 시 healthcheck 영구 실패 → app 이 영원히 기동 안 함) |
| `application-local.yaml` | datasource default 값(`${DB_NAME:...}` 등 — compose 와 동일하게), `logging.level.com.example.boilerplate`(패키지 rename 시 로거 키도 — 누락해도 동작하나 로컬 DEBUG 로그가 조용히 사라짐) |
| `.env.example` | `DB_NAME`/`DB_USERNAME` 등 기본값 (compose 와 동일하게) |
| `taskdef.json` | `family`, `awslogs-group`, SSM 파라미터 경로(`/boilerplate/...`) |
| `application-local.yaml` / `application-test.yaml` | 로컬·테스트용 더미 암호화 키 교체(선택 — 32byte Base64) |

### 2-2. 예제 도메인 제거 (goods ↔ coupon 은 FK 로 결합되어 있으므로 함께 제거)
1. 삭제: `coupon/`, `goods/`, `client/goods/` 패키지, `GoodsRepositoryTest`
2. `config/HttpServiceClientsConfig` 에서 `@ImportHttpServices(group = "goods", ...)` 와 **상단의 `import ...client.goods.GoodsClient;` 를 함께 제거** (import 잔존 시 컴파일 에러 — 미사용이 된 `ImportHttpServices` import 도 정리)
3. `application.yaml` 에서 `spring.http.serviceclient.goods` 블록 제거
4. `db/migration/V1__init.sql` 에서 `goods`/`coupons` 테이블·시드 제거 (아직 어느 DB 에도 적용 전인 복사 직후에만 — 이미 적용된 뒤라면 V파일 수정 금지, DROP 마이그레이션을 새 V파일로)
5. 도메인 에러 코드(`CouponErrorCode`/`GoodsErrorCode`)는 도메인 패키지와 함께 사라짐 — 공통 코드는 무손상 (`exception/ErrorCode` javadoc 의 coupon 예시 언급은 무해한 잔존)

### 2-3. 운영 배포 전 준비
- SSM 파라미터 생성: `/{서비스명}/{env}/db_host,db_port,db_name,db_username,db_password,encryption_key_base64`(SecureString, 32byte Base64) — 키 미주입 시 **부팅이 의도적으로 실패**한다(fail-fast)
- 환경변수: `CORS_ALLOWED_ORIGINS`(프론트 도메인, 쉼표 구분), 외부 연동 `*_BASE_URL`
- RDS: 저장 암호화 활성화 + TLS 연결
- ALB 타깃그룹 헬스체크 경로: `/actuator/health/readiness`

## 3. 무엇이 들어있나

| 영역 | 내용 |
|---|---|
| **응답 계약** | `Response<T>`(content/errors 래퍼) + `DataResponse`(목록·페이징, items 는 항상 배열) + 컨트롤러는 `BaseController.respond(...)` 한 줄 |
| **에러 계약** | 앱 예외·프레임워크 예외(~20종)·필터/컨테이너 레벨까지 **동일 래퍼로 수렴** (`GlobalExceptionHandler` + `ErrorDispatchController`). 모든 에러 본문에 `traceId`. 4xx=warn/5xx=error+스택 로깅 규약 |
| **에러 코드** | `ErrorCode` 인터페이스 — 공통은 `CommonErrorCode`, 도메인 코드는 각 도메인 패키지의 enum (공통 수정 없이 추가) |
| **컬럼 암호화** | AES-256-GCM(`@Convert(converter = EncryptedConverter.class)` 한 줄) + `KeyProvider` 교체 훅 + 응답 마스킹 `@Masked(MaskType.PHONE)` |
| **외부 연동** | HTTP Service Group — `@HttpExchange` 인터페이스 + `@ImportHttpServices` 한 줄 + yaml(그룹별 base-url/헤더/타임아웃). 4xx/5xx→`ExternalApiException` 자동 변환, traceId 전파·로깅 자동 |
| **관측성** | traceId(MDC→응답 헤더·에러 본문·외부 전파), 액세스 로그(헬스체크 제외), cloud 프로필 ECS 구조화 로그 |
| **웹 방어** | CORS(yaml, `*`+credentials 조합은 부팅 거부), 보안 응답 헤더, 페이징 `max-page-size` 캡, 잘못된 sort→400 |
| **설정** | file-per-profile(local/cloud그룹/prod) + 환경변수 주입 + 불변 record 프로퍼티 + fail-fast |
| **테스트/CI** | 단위·통합(Testcontainers, 운영 DDL 드리프트 검증) 2레인, GitHub Actions |
| **배포** | 멀티스테이지 Dockerfile(non-root, exec PID1 graceful shutdown), ECS taskdef 템플릿(SSM secrets) |

## 4. 커스터마이징 훅 (보일러플레이트 코드 수정 없이)

| 하고 싶은 것 | 방법 |
|---|---|
| 암호화 키를 KMS/Vault 로 | 자체 `KeyProvider` `@Bean` 정의 — 기본(`EnvKeyProvider`)이 자동으로 물러남 (`CryptoAutoConfiguration`) |
| 도메인 에러 코드 추가 | 도메인 패키지에 `enum Xxx implements ErrorCode` |
| 새 외부 API 연동 | `client/{대상}/` 에 `@HttpExchange` 인터페이스 + `HttpServiceClientsConfig` 에 `@ImportHttpServices` 한 줄 + yaml (`spring.http.serviceclient.{대상}.*`) — 상세 컨벤션은 `client/package-info.java` |
| CORS 허용 도메인 | `CORS_ALLOWED_ORIGINS` 환경변수 (cloud) / `application-local.yaml` |
| 액세스 로그 제외 경로 | `boilerplate.logging.exclude-paths` |
| 페이징 기본/상한 | `spring.data.web.pageable.*` |
| 파트너별 타임아웃·API 키 | `spring.http.serviceclient.{대상}.read-timeout` / `default-header` |

## 5. 변경 시 주의 (동결 코어)

다음 5파일은 **응답 계약의 코어**로, 기능 추가로 부풀리지 않는다. 변형이 필요하면 먼저 "기존 public API 에 위임하는 새 파일"로 풀 수 있는지 검토한다:

```
common/Response.java, DataResponse.java, ResponseBuilder.java,
BaseController.java, ErrorResponse.java
```

- 생성 경로는 `ResponseBuilder`/`respond(...)` 로 단일화되어 있다(생성자 package-private) — 직렬화 전용 모델이라 역직렬화 불가(의도)
- 에러 응답은 **예외를 던지는 것** 하나로 통일 — 컨트롤러에서 에러 바디를 직접 조립하지 않는다

### RFC 9457(Problem Details) 을 쓰지 않는 이유
기계가 분기할 안정적 `code` 원칙은 채택했으나, `application/problem+json` top-level 전환은 자사 프론트 대상 API 라 실익이 낮아 래퍼 계약을 유지한다. 외부 공개 API 가 생기면 재검토.

## 6. 알려진 한계 / 로드맵

- **Tomcat 요청 파싱 실패**(잘못된 percent-encoding URI 등)는 앱 도달 전이라 래퍼 밖 — 유일하게 남은 에러 계약 한계
- ~~스키마 마이그레이션(Flyway) 미도입~~ → **도입 완료** — `db/migration/V*.sql` + `ddl-auto: validate`. 적용된 V파일은 수정 금지, 변경은 새 V파일로
- **메트릭 미노출** — 운영 시 Micrometer 레지스트리 추가 예정. OTel 트레이싱·서킷브레이커는 규모가 커질 때까지 의도적 보류
- Spring Security 미포함(의도) — 도입 시: 시큐리티 필터체인에 CORS 활성화, `SecurityHeaderFilter` 와 중복 정리, 401/403 을 에러 래퍼에 수렴
