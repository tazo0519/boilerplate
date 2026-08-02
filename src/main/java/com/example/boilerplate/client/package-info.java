/**
 * 외부 시스템 연동 클라이언트 계층 — 새 연동을 추가할 때 이 컨벤션을 따른다.
 *
 * <h2>패키지 구조 (대상 시스템별 격리)</h2>
 * <pre>
 * client/{대상}/
 *   ├─ XxxClient.java        : @HttpExchange 선언형 인터페이스 (메서드 = API 계약)
 *   └─ dto/
 *       ├─ XxxApiRequest.java   : 요청 DTO — "Api" 접미사로 우리 API DTO 와 구분
 *       ├─ XxxApiResponse.java  : 응답 DTO — record 사용(읽기 전용 데이터)
 *       └─ XxxApiEnvelope.java  : (해당 파트너가 공통 껍데기를 쓰면) 파트너별 제네릭 엔벨로프
 * </pre>
 *
 * <h2>등록 (HTTP Service Group — Config 클래스/팩토리 코드 불필요)</h2>
 * <ol>
 *   <li>{@code config/HttpServiceClientsConfig} 에
 *       {@code @ImportHttpServices(group = "{대상}", types = XxxClient.class)} 한 줄 추가</li>
 *   <li>yaml: {@code spring.http.serviceclient.{대상}.base-url} (그룹별 default-header/타임아웃 가능,
 *       전역 {@code spring.http.clients.*} 는 fallback)</li>
 * </ol>
 *
 * <h2>원칙</h2>
 * <ul>
 *   <li><b>경계 변환(anti-corruption)</b> — 외부 DTO 는 client 패키지 밖(도메인·우리 응답)으로
 *       노출하지 않는다. 서비스 경계에서 도메인 모델/우리 DTO 로 변환해 파트너 API 변경이
 *       도메인 전체로 번지지 않게 한다.</li>
 *   <li><b>엔벨로프는 파트너별</b> — 응답 껍데기({@code code/message/data})는 파트너마다 다르므로
 *       전역 공통 부모를 만들지 않고 파트너 패키지 안에 제네릭으로 둔다.</li>
 *   <li><b>에러 변환은 공통층</b> — 4xx/5xx 는 HttpServiceClientsConfig 의 group configurer,
 *       I/O 오류는 {@code GlobalExceptionHandler} 가 처리한다. 서비스에서 try/catch 하지 않는다.</li>
 *   <li><b>trace/logging 자동 적용</b> — RestClientConfig 의 {@code RestClientCustomizer} 를
 *       Boot 이 모든 그룹 클라이언트에 적용한다.</li>
 * </ul>
 */
package com.example.boilerplate.client;
