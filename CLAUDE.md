# CLAUDE.md — 이 저장소에서 작업할 때의 헌법

Spring Boot 4.1 (Framework 7) · Java 21 · PostgreSQL 16 모놀리스 **보일러플레이트**.
복사해서 새 서비스를 시작하는 템플릿이므로, 여기서 정한 컨벤션이 모든 파생 서비스로 복제된다.
사람용 안내(빠른 시작·복사 체크리스트·훅 표)는 `README.md` — 중복 설명하지 않는다.

## 설계 관심사 (모든 판단의 기준)
1. **제한** — 메서드·생성자를 의도된 용도로만 쓰게 표면 최소화 (package-private, final, 불변 record, 생성 경로 단일화)
2. **커스텀 훅** — 파생 서비스가 보일러플레이트 코드를 수정하지 않고 변형 (빈 재정의, yaml, 도메인별 enum)
3. **래퍼 계약** — 성공은 `{content}`, 에러는 `{errors:{code,message,traceId,timestamp,fieldErrors?}}` 로 전층 수렴

## 명령
```bash
./gradlew build            # 컴파일 + 단위 테스트 (Docker 불필요)
./gradlew integrationTest  # Testcontainers/PostgreSQL — Docker 필요
docker compose up -d postgres && SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```
변경 후에는 두 레인 모두 green 확인. 동작 주장은 가능하면 실측(부팅 프로브/테스트)으로 검증한다.

## 불변 규칙 (위반 금지)
- **동결 코어 5파일** — `common/` 의 `Response` `DataResponse` `ResponseBuilder` `BaseController` `ErrorResponse` 는 **사용자 승인 없이 수정 금지**. 변형이 필요하면 먼저 "기존 public API 에 위임하는 새 파일"로 풀 수 있는지 검토. (`errorRespond` 는 사용자가 명시적으로 유지 지시함 — 제거 금지)
- **에러 응답은 예외를 던지는 것 하나로 통일** — 컨트롤러에서 에러 바디를 직접 조립하지 않는다. 프레임워크 예외까지 `GlobalExceptionHandler`(+`ErrorDispatchController`)가 래퍼로 수렴한다.
- **와이어 계약 변경 금지** — 에러 `code` 문자열, 래퍼 필드 구조. 회귀 테스트(`ApiContractIntegrationTest`, `DataResponseContractTest`)가 지킨다. 계약을 의도적으로 바꾸면 테스트도 함께.
- 응답 모델은 **직렬화 전용**(creator 없음) — 타 서비스 응답 파싱에 재사용하지 않는다.

## 컨벤션
- **테스트**: 영어 camelCase 메서드명 + `@DisplayName("한글 명세")`. 한글 메서드명 금지. 통합 테스트는 `@Tag("integration")` + `@Import(TestcontainersConfiguration.class)`.
- **외부 연동**: `client/{대상}/` 에 `@HttpExchange` 인터페이스 + record DTO(`Api` 접미사) + 파트너별 엔벨로프. 등록은 `HttpServiceClientsConfig` 의 `@ImportHttpServices` 한 줄 + `spring.http.serviceclient.{대상}.*` yaml. **외부 DTO 는 client 패키지 밖으로 노출 금지**(anti-corruption). 서비스에서 try/catch 하지 않는다(에러 변환은 공통층).
- **에러 코드**: 공통은 `CommonErrorCode`, 도메인 코드는 **해당 도메인 패키지의 enum** (`implements ErrorCode`) — 공통 파일을 수정하지 않는다.
- **설정**: `@ConfigurationProperties` 는 생성자 바인딩 record(불변). 시크릿은 환경변수/SSM — **cloud/prod 프로필의** yaml 에 default 를 두지 않는다(fail-fast). local/test 의 더미 키·default 는 **의도된 것**이니 제거하지 말 것. 기본 빈 제공은 auto-configuration(`@AutoConfiguration` + imports 파일) + `@ConditionalOnMissingBean` — 일반 `@Configuration` 의 조건부 빈은 등록 순서 비보장으로 금지.
- **스키마**: `src/main/resources/db/schema.sql` 이 단일 소스(`ddl-auto: none`, Flyway 미도입). **엔티티를 추가/변경하면 schema.sql 의 DDL 도 함께 동기화** — 안 하면 integrationTest(운영 DDL 드리프트 검증)가 반드시 깨진다.
- 주석·커밋 메시지는 한국어. 주석은 "왜"를 남긴다(적대적 검증 실측 결과 인용 스타일 유지).

## 이 스택의 함정 (실측으로 확인된 것)
- **Jackson 3** (`tools.jackson.*`) — databind 는 신규 패키지. 어노테이션은 **이원화**: jackson-annotations 계열(`@JsonInclude` 등)은 `com.fasterxml` 유지, **databind 계열(`@JsonSerialize` 등)은 `tools.jackson.databind.annotation`**. 커스텀 시리얼라이저는 `tools.jackson.databind.ValueSerializer`. ⚠️ springdoc 이 Jackson 2 databind 도 classpath 에 올려두므로 **잘못된 `com.fasterxml...databind` import 도 컴파일에 성공하고 조용히 무시된다** — import 문을 눈으로 확인할 것.
- **Boot 4 모듈화**: `@AutoConfigureMockMvc` → `spring-boot-starter-webmvc-test` + `org.springframework.boot.webmvc.test.autoconfigure`. `ErrorController` → `org.springframework.boot.webmvc.error`. `PropertyReferenceException` → `org.springframework.data.core`.
- **Framework 7 은 `@Controller` 스테레오타입 없이는 핸들러 미인식** (`@RequestMapping` 클래스만으로 불가).
- **테스트 전용 컨트롤러**는 스캔 밖 패키지(`com.example.testsupport`) + `@Import` — 스캔 범위 안에 두면 이중 등록(Ambiguous mapping). 테스트 클래스도 컴포넌트 스캔 대상임을 잊지 말 것.
- `@ConfigurationProperties` 바인더는 **미해석 `${...}` placeholder 를 예외 없이 리터럴로 바인딩**한다 — 값 검증(예: `EnvKeyProvider` 의 placeholder 감지)이 fail-fast 의 실체다.
- MockMvc 대비 실제 서블릿 동작(406 협상, ERROR 디스패치, Tomcat 파싱 레벨)이 다를 수 있다 — 에러 계약 변경 시 부팅 프로브로 재확인 권장.

## 검증 문화
큰 변경(계약·보안·훅)은 "구현 → 적대적 검증(가드를 일부러 깨서 테스트가 우는지 확인) → 발견 수정 → 재검증" 루프를 따른다. 테스트를 추가하면 뮤테이션(가드 제거 시 해당 테스트만 정확히 실패)으로 유효성을 증명한다.
