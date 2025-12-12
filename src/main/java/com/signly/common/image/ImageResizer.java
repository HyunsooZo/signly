package com.signly.common.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * 이미지 리사이징 유틸리티
 * 웹 미리보기와 PDF 생성에서 일관된 이미지 크기 제공
 */
@Component
public class ImageResizer {

    private static final Logger logger = LoggerFactory.getLogger(ImageResizer.class);

    // 서명 이미지 표준 크기 (PDF CSS와 동일)
    private static final int SIGNATURE_MAX_WIDTH = 600; // 고해상도 (기존 180 -> 600)
    private static final int SIGNATURE_MAX_HEIGHT = 350; // 고해상도 (기존 105 -> 350)

    /**
     * Base64 Data URL을 받아서 리사이즈 후 다시 Base64 Data URL로 반환
     *
     * @param dataUrl Base64 이미지 Data URL (data:image/png;base64,...)
     * @return 리사이즈된 Base64 Data URL, 실패 시 원본 반환
     */
    public String resizeSignatureImage(String dataUrl) {
        if (dataUrl == null || dataUrl.isBlank()) {
            return dataUrl;
        }

        try {
            // Data URL 파싱
            if (!dataUrl.startsWith("data:image/")) {
                logger.warn("잘못된 이미지 Data URL 형식: {}", dataUrl.substring(0, Math.min(50, dataUrl.length())));
                return dataUrl;
            }

            // MIME 타입과 Base64 데이터 분리
            String[] parts = dataUrl.split(",", 2);
            if (parts.length != 2) {
                logger.warn("Data URL 파싱 실패");
                return dataUrl;
            }

            String mimeHeader = parts[0]; // "data:image/png;base64"
            String base64Data = parts[1];

            // MIME 타입 추출
            String mimeType = extractMimeType(mimeHeader);
            String formatName = mimeType.split("/")[1]; // "png", "jpeg" 등

            // Base64 디코딩
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            // 이미지 리사이징
            byte[] resizedBytes = resizeImage(imageBytes, formatName);

            // Base64 인코딩
            String resizedBase64 = Base64.getEncoder().encodeToString(resizedBytes);

            String resizedDataUrl = mimeHeader + "," + resizedBase64;

            logger.debug("이미지 리사이징 완료: 원본={}bytes, 리사이즈={}bytes",
                    imageBytes.length, resizedBytes.length);

            return resizedDataUrl;

        } catch (Exception e) {
            logger.error("이미지 리사이징 실패, 원본 반환", e);
            return dataUrl;
        }
    }

    /**
     * 이미지 바이트 배열을 받아서 리사이즈
     */
    private byte[] resizeImage(
            byte[] imageBytes,
            String formatName
    ) throws IOException {
        // 원본 이미지 로드
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (originalImage == null) {
            throw new IOException("이미지를 읽을 수 없습니다");
        }

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // 비율 계산
        double widthRatio = (double) SIGNATURE_MAX_WIDTH / originalWidth;
        double heightRatio = (double) SIGNATURE_MAX_HEIGHT / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);

        // 이미 작은 이미지면 그대로 반환
        if (ratio >= 1.0) {
            logger.debug("이미지가 이미 작음, 리사이징 생략: {}x{}", originalWidth, originalHeight);
            return imageBytes;
        }

        // 새 크기 계산
        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        logger.debug("이미지 리사이징: {}x{} -> {}x{}",
                originalWidth, originalHeight, newWidth, newHeight);

        // 고품질 리사이징
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = resizedImage.createGraphics();

        // 고품질 렌더링 설정
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        graphics.dispose();

        // PNG로 변환 (투명도 유지)
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, formatName, outputStream);
        return outputStream.toByteArray();
    }

    /**
     * MIME 타입 추출
     */
    private String extractMimeType(String mimeHeader) {
        // "data:image/png;base64" -> "image/png"
        String mimeType = mimeHeader.substring(5); // "data:" 제거
        int semicolonIndex = mimeType.indexOf(';');
        if (semicolonIndex > 0) {
            mimeType = mimeType.substring(0, semicolonIndex);
        }
        return mimeType;
    }
}
