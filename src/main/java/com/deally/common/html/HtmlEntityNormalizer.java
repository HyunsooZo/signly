package com.deally.common.html;

import java.util.Map;

/**
 * HTML Entity 정규화 유틸리티
 * 웹/PDF 렌더링 간 일관된 HTML entity 처리 제공
 *
 * <p>주요 용도:
 * <ul>
 *   <li>HTML 에디터(Quill)에서 생성된 escaped 문자 정규화</li>
 *   <li>서명 플레이스홀더 등 특수 문자 치환</li>
 *   <li>웹 프리뷰와 PDF 생성 간 일관성 보장</li>
 * </ul>
 */
public final class HtmlEntityNormalizer {

    /**
     * HTML entity → 실제 문자 매핑
     * 주로 대괄호([, ])와 관련된 entity 처리
     */
    private static final Map<String, String> ENTITY_MAP = Map.ofEntries(
            // 숫자 형식 entity
            Map.entry("&#91;", "["),
            Map.entry("&#93;", "]"),

            // 이중 인코딩된 entity (&amp;로 시작)
            Map.entry("&amp;#91;", "["),
            Map.entry("&amp;#93;", "]"),

            // Named entity
            Map.entry("&lbrack;", "["),
            Map.entry("&rbrack;", "]"),

            // 중괄호 (필요 시 추가)
            Map.entry("&#123;", "{"),
            Map.entry("&#125;", "}"),
            Map.entry("&lcub;", "{"),
            Map.entry("&rcub;", "}")
    );

    private HtmlEntityNormalizer() {
        // Utility class - prevent instantiation
    }

    /**
     * HTML entity를 실제 문자로 변환
     *
     * @param html 원본 HTML 문자열
     * @return entity가 정규화된 HTML 문자열
     */
    public static String normalizeEntities(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }

        String result = html;
        for (Map.Entry<String, String> entry : ENTITY_MAP.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * 변수 플레이스홀더만 정규화
     * (서명 이미지 플레이스홀더 등 특정 패턴에만 적용)
     *
     * @param html 원본 HTML 문자열
     * @return 플레이스홀더가 정규화된 HTML 문자열
     */
    public static String normalizePlaceholders(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }

        // [EMPLOYER_SIGNATURE_IMAGE] 패턴 정규화
        String result = html;
        result = result.replace("&#91;EMPLOYER_SIGNATURE_IMAGE&#93;", "[EMPLOYER_SIGNATURE_IMAGE]");
        result = result.replace("&#91;EMPLOYEE_SIGNATURE_IMAGE&#93;", "[EMPLOYEE_SIGNATURE_IMAGE]");
        result = result.replace("&amp;#91;EMPLOYER_SIGNATURE_IMAGE&amp;#93;", "[EMPLOYER_SIGNATURE_IMAGE]");
        result = result.replace("&amp;#91;EMPLOYEE_SIGNATURE_IMAGE&amp;#93;", "[EMPLOYEE_SIGNATURE_IMAGE]");
        result = result.replace("&lbrack;EMPLOYER_SIGNATURE_IMAGE&rbrack;", "[EMPLOYER_SIGNATURE_IMAGE]");
        result = result.replace("&lbrack;EMPLOYEE_SIGNATURE_IMAGE&rbrack;", "[EMPLOYEE_SIGNATURE_IMAGE]");

        return result;
    }
}
