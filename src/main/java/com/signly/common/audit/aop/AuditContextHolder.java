package com.signly.common.audit.aop;

/**
 * 감사 로그 컨텍스트 홀더
 * ThreadLocal을 사용하여 요청별 IP 주소와 User-Agent 정보를 저장
 */
public class AuditContextHolder {

    private static final ThreadLocal<String> IP_ADDRESS = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_AGENT = new ThreadLocal<>();

    private AuditContextHolder() {
        // 유틸리티 클래스 - 인스턴스화 방지
    }

    /**
     * 클라이언트 IP 주소 설정
     */
    public static void setIpAddress(String ipAddress) {
        IP_ADDRESS.set(ipAddress);
    }

    /**
     * 클라이언트 IP 주소 조회
     */
    public static String getIpAddress() {
        return IP_ADDRESS.get();
    }

    /**
     * User-Agent 설정
     */
    public static void setUserAgent(String userAgent) {
        USER_AGENT.set(userAgent);
    }

    /**
     * User-Agent 조회
     */
    public static String getUserAgent() {
        return USER_AGENT.get();
    }

    /**
     * ThreadLocal 데이터 정리
     * 요청 처리 완료 후 반드시 호출해야 함
     */
    public static void clear() {
        IP_ADDRESS.remove();
        USER_AGENT.remove();
    }
}