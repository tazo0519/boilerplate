package com.example.boilerplate.common.crypto;

import javax.crypto.SecretKey;

public interface KeyProvider {

    SecretKey getActiveKey();

    /**
     * 현재 암호화에 사용할 키 버전 — 암호문 프리픽스(예: "v1:")로 저장된다.
     *
     * <p>default 인 이유: 추상 메서드로 추가하면 기존 람다 훅과 파생 서비스의 커스텀
     * 구현이 전부 깨진다(훅 계약 위반). 로테이션이 필요 없는 구현은 이 메서드를
     * 몰라도 되고, 필요한 구현(다중 키/KMS)만 {@link #getKey(String)} 와 함께 재정의한다.
     */
    default String getActiveVersion() {
        return "v1";
    }

    /**
     * 지정 버전의 키 조회 — 복호화 시 암호문 프리픽스로 파싱된 버전이 넘어온다.
     *
     * <p>기본 구현은 단일 키만 알므로 active 버전 외에는 fail-fast 한다. 조용히
     * active 키로 폴백하면 키 불일치가 "GCM 인증 실패"로 보고되어 운영자가 원인을
     * 오독한다(EnvKeyProvider 의 placeholder 감지와 같은 이유).
     */
    default SecretKey getKey(String version) {
        if (getActiveVersion().equals(version)) {
            return getActiveKey();
        }
        throw new IllegalStateException(
                "알 수 없는 키 버전입니다: " + version + " (지원: " + getActiveVersion()
                        + ") — 구버전 키의 복호화가 필요하면 getKey/getActiveVersion 을"
                        + " 재정의한 로테이션 지원 KeyProvider 를 빈으로 등록하세요.");
    }
}
