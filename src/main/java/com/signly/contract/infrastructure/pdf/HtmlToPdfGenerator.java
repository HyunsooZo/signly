package com.signly.contract.infrastructure.pdf;

import com.lowagie.text.pdf.BaseFont;
import com.signly.contract.domain.model.GeneratedPdf;
import com.signly.contract.domain.service.PdfGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private static final List<String> PDF_CSS_RESOURCES = List.of(
            "static/css/template-preview.css",
            "static/css/contracts.css"
    );
    private static final String PRESET_TEMPLATE_PATH = "presets/templates/standard-employment-contract.html";
    private static final Pattern STYLE_PATTERN = Pattern.compile("<style>(.*?)</style>", Pattern.DOTALL);
    private static final List<String> FONT_RESOURCES = List.of(
            "fonts/NanumGothic-Regular.ttf",
            "fonts/NanumGothic-Bold.ttf"
    );

    @Override
    public GeneratedPdf generateFromHtml(String htmlContent, String fileName) {
        try {
            logger.info("PDF 생성 시작: fileName={}", fileName);

            // HTML을 XHTML로 정리 (Flying Saucer는 엄격한 XHTML 요구)
            String xhtmlContent = sanitizeHtmlToXhtml(htmlContent);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();

            registerFonts(renderer);

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
        StringBuilder cssBuilder = new StringBuilder();

        for (String resourcePath : PDF_CSS_RESOURCES) {
            loadResourceAsString(resourcePath)
                    .map(this::adaptCssForPdf)
                    .ifPresent(css -> appendCss(cssBuilder, css));
        }

        if (cssBuilder.length() == 0) {
            extractCssFromPresetTemplate()
                    .map(this::adaptCssForPdf)
                    .ifPresent(css -> appendCss(cssBuilder, css));
        }

        if (cssBuilder.length() == 0) {
            logger.warn("PDF용 CSS를 찾지 못해 기본 스타일을 사용합니다.");
            return getDefaultCssStyles();
        }

        // PDF 렌더링 호환을 위한 폴백 스타일 보강
        appendCss(cssBuilder, "body, .contract-content, .preset-document { font-family: 'NanumGothic', 'Nanum Gothic', 'Malgun Gothic', sans-serif; line-height: 1.6; font-size: 13px; }");
        appendCss(cssBuilder, ".signature-stamp-image-element { width: 90px; height: auto; }");

        return cssBuilder.toString();
    }

    /**
     * 기본 CSS 스타일 (템플릿 로드 실패 시 대체용)
     */
    private String getDefaultCssStyles() {
        return "body { font-family: 'NanumGothic', 'Nanum Gothic', sans-serif; line-height: 1.6; font-size: 12pt; padding: 20mm; }\n" +
               ".title { text-align: center; font-size: 20pt; font-weight: bold; margin-bottom: 30pt; }\n" +
               ".section { margin: 12pt 0; }\n" +
               ".signature-stamp-image-element { width: 60pt; height: auto; vertical-align: middle; }";
    }

    @Override
    public GeneratedPdf generateFromTemplate(String templateName, Map<String, Object> variables, String fileName) {
        // 현재는 미구현 (필요시 Thymeleaf 템플릿 엔진 연동)
        throw new UnsupportedOperationException("Template-based PDF generation is not yet implemented");
    }

    private Optional<String> loadResourceAsString(String path) {
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            logger.warn("리소스를 찾을 수 없습니다: {}", path);
            return Optional.empty();
        }

        try (InputStream inputStream = resource.getInputStream()) {
            return Optional.of(StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            logger.error("리소스 로딩 실패: {}", path, e);
            return Optional.empty();
        }
    }

    private Optional<String> extractCssFromPresetTemplate() {
        ClassPathResource resource = new ClassPathResource(PRESET_TEMPLATE_PATH);
        if (!resource.exists()) {
            logger.warn("템플릿 파일을 찾을 수 없습니다: {}", PRESET_TEMPLATE_PATH);
            return Optional.empty();
        }

        try (InputStream inputStream = resource.getInputStream()) {
            String templateHtml = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            Matcher matcher = STYLE_PATTERN.matcher(templateHtml);
            if (matcher.find()) {
                logger.debug("템플릿에서 CSS 스타일 추출 완료");
                return Optional.of(matcher.group(1));
            }
            logger.warn("템플릿에 <style> 태그가 없어 기본 CSS로 대체합니다.");
            return Optional.empty();
        } catch (IOException e) {
            logger.error("템플릿 CSS 추출 실패", e);
            return Optional.empty();
        }
    }

    private String adaptCssForPdf(String css) {
        String adapted = css.replace("body:not(:has(.navbar))", "body");
        adapted = adapted.replace("'Malgun Gothic'", "'NanumGothic', 'Nanum Gothic', 'Malgun Gothic'");
        adapted = adapted.replace("\"Malgun Gothic\"", "'NanumGothic', 'Nanum Gothic', 'Malgun Gothic'");
        adapted = adapted.replace("font-family: Malgun Gothic", "font-family: NanumGothic, Nanum Gothic, Malgun Gothic");
        return adapted;
    }

    private void appendCss(StringBuilder builder, String css) {
        if (builder.length() > 0) {
            builder.append('\n');
        }
        builder.append(css);
    }

    private void registerFonts(ITextRenderer renderer) {
        for (String fontResource : FONT_RESOURCES) {
            try {
                ClassPathResource resource = new ClassPathResource(fontResource);
                if (!resource.exists()) {
                    logger.warn("PDF 폰트 리소스를 찾을 수 없습니다: {}", fontResource);
                    continue;
                }

                String fontPath;
                if (resource.isFile()) {
                    fontPath = resource.getFile().getAbsolutePath();
                } else {
                    fontPath = resource.getURL().toExternalForm();
                }

                renderer.getFontResolver().addFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                logger.debug("PDF 폰트 등록 완료: {}", fontResource);
            } catch (Exception e) {
                logger.warn("PDF 폰트 등록 실패: {}", fontResource, e);
            }
        }
    }
}
