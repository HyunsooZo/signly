package com.signly.common.storage.util;

import com.signly.common.exception.ValidationException;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class FileSecurityValidator {

    private static final Tika tika = new Tika();
    private static final Pattern SAFE_CATEGORY_PATTERN = Pattern.compile("^[a-zA-Z0-9_/-]+$");
    
    private static final Map<String, String> MIME_TO_EXTENSION = Map.of(
            "application/pdf", ".pdf",
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/gif", ".gif"
    );
    
    private static final Set<String> ALLOWED_MIME_TYPES = MIME_TO_EXTENSION.keySet();

    public String detectAndValidateMimeType(byte[] data, String declaredContentType) {
        String detectedMimeType = tika.detect(data);
        
        if (!ALLOWED_MIME_TYPES.contains(detectedMimeType)) {
            throw new ValidationException("지원하지 않는 파일 형식입니다. 감지된 타입: " + detectedMimeType);
        }
        
        if (declaredContentType != null && !declaredContentType.equals(detectedMimeType)) {
        }
        
        return detectedMimeType;
    }

    public String getSafeExtension(String mimeType) {
        String extension = MIME_TO_EXTENSION.get(mimeType);
        if (extension == null) {
            throw new ValidationException("지원하지 않는 MIME 타입입니다: " + mimeType);
        }
        return extension;
    }

    public void validateCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new ValidationException("카테고리가 유효하지 않습니다");
        }
        
        if (category.contains("..")) {
            throw new ValidationException("카테고리에 상위 디렉토리 참조(..)는 사용할 수 없습니다");
        }
        
        if (category.startsWith("/") || category.contains("\\")) {
            throw new ValidationException("카테고리는 상대 경로만 가능하며 백슬래시는 사용할 수 없습니다");
        }
        
        if (!SAFE_CATEGORY_PATTERN.matcher(category).matches()) {
            throw new ValidationException("카테고리는 영문, 숫자, 언더스코어, 하이픈, 슬래시만 사용할 수 있습니다");
        }
    }

    public String sanitizeFilename(String filename) {
        if (filename == null) {
            return "";
        }
        
        String sanitized = filename.replace("\0", "");
        
        sanitized = sanitized.replaceAll("[<>:\"|?*]", "");
        
        return sanitized.trim();
    }
}
