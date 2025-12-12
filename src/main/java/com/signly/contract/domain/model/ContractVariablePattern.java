package com.signly.contract.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 계약서 변수 패턴 정의
 * 웹 프리뷰(JavaScript)와 서버(Java) 간 일관된 변수 처리 제공
 *
 * <p>지원 패턴:
 * <ul>
 *   <li>{변수명} - 중괄호 패턴</li>
 *   <li>[변수명] - 대괄호 패턴</li>
 * </ul>
 *
 * <p>JavaScript 동등 코드:
 * <pre>
 * const PLACEHOLDER_REGEX = /\{([^{}]+)\}|\[([^\[\]]+)\]/g;
 * const IGNORED_PLACEHOLDERS = new Set(['EMPLOYER_SIGNATURE_IMAGE']);
 * </pre>
 */
public final class ContractVariablePattern {

    /**
     * 중괄호 패턴: {변수명}
     */
    public static final String CURLY_BRACE = "\\{([^{}]+)\\}";

    /**
     * 대괄호 패턴: [변수명]
     */
    public static final String SQUARE_BRACKET = "\\[([^\\[\\]]+)\\]";

    /**
     * 통합 패턴: {변수명} 또는 [변수명]
     * JavaScript: /\{([^{}]+)\}|\[([^\[\]]+)\]/g
     */
    public static final Pattern COMBINED = Pattern.compile(
            CURLY_BRACE + "|" + SQUARE_BRACKET
    );

    /**
     * 무시할 플레이스홀더 (서명 이미지 등)
     * 이 변수들은 일반 변수 처리 대상에서 제외됨
     */
    public static final Set<String> IGNORED_PLACEHOLDERS = Set.of(
            "EMPLOYER_SIGNATURE_IMAGE",
            "EMPLOYEE_SIGNATURE_IMAGE"
    );

    private ContractVariablePattern() {
        // Utility class - prevent instantiation
    }

    /**
     * HTML에서 모든 변수 추출
     *
     * @param html 원본 HTML 문자열
     * @return 추출된 변수 이름 리스트 (중복 제거됨, 무시 변수 제외)
     */
    public static List<String> extractVariables(String html) {
        if (html == null || html.isEmpty()) {
            return List.of();
        }

        List<String> variables = new ArrayList<>();
        Matcher matcher = COMBINED.matcher(html);

        while (matcher.find()) {
            // group(1) = {변수명}의 변수명, group(2) = [변수명]의 변수명
            String varName = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);

            if (varName != null) {
                varName = varName.trim();
                if (!varName.isEmpty() && !IGNORED_PLACEHOLDERS.contains(varName)) {
                    if (!variables.contains(varName)) {
                        variables.add(varName);
                    }
                }
            }
        }

        return variables;
    }

    /**
     * 변수가 무시 대상인지 확인
     *
     * @param variableName 변수 이름
     * @return 무시 대상이면 true
     */
    public static boolean isIgnored(String variableName) {
        return IGNORED_PLACEHOLDERS.contains(variableName);
    }

    /**
     * HTML에 특정 변수가 포함되어 있는지 확인
     *
     * @param html         원본 HTML
     * @param variableName 찾을 변수 이름
     * @return 포함 여부
     */
    public static boolean containsVariable(
            String html,
            String variableName
    ) {
        if (html == null || variableName == null) {
            return false;
        }

        // {변수명} 또는 [변수명] 패턴으로 검색
        String curlyPattern = "\\{" + Pattern.quote(variableName) + "\\}";
        String squarePattern = "\\[" + Pattern.quote(variableName) + "\\]";

        return html.matches(".*(" + curlyPattern + "|" + squarePattern + ").*");
    }
}
