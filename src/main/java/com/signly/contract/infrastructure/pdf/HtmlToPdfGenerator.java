package com.signly.contract.infrastructure.pdf;

import com.signly.contract.domain.model.GeneratedPdf;
import com.signly.contract.domain.service.PdfGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Flying Saucer를 사용한 PDF 생성 구현체
 * DDD 원칙: Infrastructure Layer에서 기술적 구현 담당
 * DIP: PdfGenerator 인터페이스를 구현하여 도메인에 의존성 주입
 * SRP: HTML to PDF 변환만 담당
 */
@Component
public class HtmlToPdfGenerator implements PdfGenerator {

    private static final Logger logger = LoggerFactory.getLogger(HtmlToPdfGenerator.class);

    @Override
    public GeneratedPdf generateFromHtml(String htmlContent, String fileName) {
        try {
            logger.info("PDF 생성 시작: fileName={}", fileName);

            // HTML을 XHTML로 정리 (Flying Saucer는 엄격한 XHTML 요구)
            String xhtmlContent = sanitizeHtmlToXhtml(htmlContent);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();

            // HTML을 PDF로 렌더링
            renderer.setDocumentFromString(xhtmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);

            byte[] pdfBytes = outputStream.toByteArray();
            outputStream.close();

            GeneratedPdf pdf = GeneratedPdf.of(pdfBytes, fileName);
            logger.info("PDF 생성 완료: fileName={}, size={}bytes", fileName, pdf.getSizeInBytes());

            return pdf;

        } catch (Exception e) {
            logger.error("PDF 생성 실패: fileName={}", fileName, e);
            throw new PdfGenerationException("PDF 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * HTML을 XHTML로 정리
     * Flying Saucer는 엄격한 XHTML을 요구하므로, 닫히지 않은 태그들을 self-closing 태그로 변환
     * SRP: HTML 정규화 로직 분리
     */
    private String sanitizeHtmlToXhtml(String html) {
        if (html == null || html.isBlank()) {
            return wrapInXhtmlDocument("");
        }

        String xhtml = html;

        // Self-closing 태그들을 XHTML 형식으로 변환
        xhtml = xhtml.replaceAll("<br\\s*/?>", "<br/>");
        xhtml = xhtml.replaceAll("</br>", ""); // 잘못된 </br> 태그 제거
        xhtml = xhtml.replaceAll("<hr\\s*/?>", "<hr/>");
        xhtml = xhtml.replaceAll("<input([^>]*?)(?<!/)>", "<input$1/>");
        xhtml = xhtml.replaceAll("<meta([^>]*?)(?<!/)>", "<meta$1/>");
        xhtml = xhtml.replaceAll("<link([^>]*?)(?<!/)>", "<link$1/>");

        // img 태그 처리 (이미 />로 닫혀있지 않은 경우만)
        xhtml = xhtml.replaceAll("<img([^>]*?)(?<!/)>", "<img$1/>");

        // HTML이 완전한 문서가 아니면 XHTML 문서로 감싸기
        if (!xhtml.trim().toLowerCase().startsWith("<!doctype") &&
            !xhtml.trim().toLowerCase().startsWith("<html")) {
            xhtml = wrapInXhtmlDocument(xhtml);
        }

        logger.debug("HTML을 XHTML로 정리 완료");
        return xhtml;
    }

    /**
     * HTML 내용을 완전한 XHTML 문서로 감싸기
     * Flying Saucer는 완전한 XML 문서를 요구함
     */
    private String wrapInXhtmlDocument(String bodyContent) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" " +
               "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
               "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
               "<head>\n" +
               "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n" +
               "  <title>Contract PDF</title>\n" +
               "  <style type=\"text/css\">\n" +
               getContractCssStyles() +
               "  </style>\n" +
               "</head>\n" +
               "<body>\n" +
               bodyContent +
               "\n</body>\n" +
               "</html>";
    }

    /**
     * 계약서 CSS 스타일을 원본 템플릿에서 추출
     * 템플릿 파일의 <style> 태그 내용을 그대로 사용
     */
    private String getContractCssStyles() {
        try {
            // 표준 근로계약서 템플릿에서 CSS 추출
            ClassPathResource resource = new ClassPathResource("presets/templates/standard-employment-contract.html");

            if (!resource.exists()) {
                logger.warn("템플릿 파일을 찾을 수 없습니다. 기본 스타일 사용");
                return getDefaultCssStyles();
            }

            try (InputStream inputStream = resource.getInputStream()) {
                String templateHtml = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

                // <style> 태그 안의 내용 추출
                Pattern stylePattern = Pattern.compile("<style>(.*?)</style>", Pattern.DOTALL);
                Matcher matcher = stylePattern.matcher(templateHtml);

                if (matcher.find()) {
                    String originalCss = matcher.group(1);
                    // Flying Saucer 호환을 위해 폰트만 수정
                    String adaptedCss = originalCss.replaceAll("font-family:\\s*['\"]?Malgun Gothic['\"]?[^;]*;", "font-family: serif;");

                    logger.debug("템플릿에서 CSS 스타일 추출 완료");
                    return adaptedCss;
                } else {
                    logger.warn("템플릿에서 <style> 태그를 찾을 수 없습니다. 기본 스타일 사용");
                    return getDefaultCssStyles();
                }
            }
        } catch (Exception e) {
            logger.error("템플릿 CSS 추출 실패, 기본 스타일 사용", e);
            return getDefaultCssStyles();
        }
    }

    /**
     * 기본 CSS 스타일 (템플릿 로드 실패 시 대체용)
     */
    private String getDefaultCssStyles() {
        return "body { font-family: serif; line-height: 1.6; font-size: 12pt; padding: 20mm; }\n" +
               ".title { text-align: center; font-size: 20pt; font-weight: bold; margin-bottom: 30pt; }\n" +
               ".section { margin: 12pt 0; }\n" +
               ".signature-stamp-image-element { width: 60pt; height: auto; vertical-align: middle; }";
    }

    @Override
    public GeneratedPdf generateFromTemplate(String templateName, Map<String, Object> variables, String fileName) {
        // 현재는 미구현 (필요시 Thymeleaf 템플릿 엔진 연동)
        throw new UnsupportedOperationException("Template-based PDF generation is not yet implemented");
    }
}
