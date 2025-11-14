package com.signly.contract.domain.service;

import com.signly.contract.domain.model.GeneratedPdf;

import java.util.Map;

/**
 * PDF 생성을 위한 도메인 서비스 인터페이스
 * DDD 원칙: DIP(Dependency Inversion Principle) 적용
 * - 도메인이 Infrastructure에 의존하지 않고 추상화에 의존
 * - 실제 구현은 Infrastructure 레이어에서 담당
 */
public interface PdfGenerator {

    /**
     * HTML 문자열을 PDF로 변환
     *
     * @param htmlContent HTML 문자열
     * @param fileName    PDF 파일명
     * @return 생성된 PDF
     */
    GeneratedPdf generateFromHtml(
            String htmlContent,
            String fileName
    );

    /**
     * HTML 템플릿과 변수를 사용하여 PDF 생성
     *
     * @param templateName 템플릿 파일명
     * @param variables    템플릿 변수
     * @param fileName     PDF 파일명
     * @return 생성된 PDF
     */
    GeneratedPdf generateFromTemplate(
            String templateName,
            Map<String, Object> variables,
            String fileName
    );
}
