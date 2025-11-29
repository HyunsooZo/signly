package com.signly.template.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

/**
 * 템플릿 변수 정의 엔티티
 * 템플릿에서 사용되는 변수들의 메타데이터를 관리
 */
@Entity
@Table(name = "template_variable_definition")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class TemplateVariableDefinition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String variableName;
    
    @Column(nullable = false, length = 100)
    private String displayName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private VariableCategory category;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VariableType variableType;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(length = 50)
    private String iconClass;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer inputSize = 10;
    
    private Integer maxLength;
    
    @Column(length = 200)
    private String placeholderExample;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isRequired = false;
    
    @Column(length = 500)
    private String validationRule;
    
    @Column(length = 200)
    private String validationMessage;
    
    @Column(columnDefinition = "TEXT")
    private String defaultValue;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 변수 정의 생성
     */
    public static TemplateVariableDefinition create(
            String variableName,
            String displayName,
            VariableCategory category,
            VariableType variableType
    ) {
        return TemplateVariableDefinition.builder()
                .variableName(variableName)
                .displayName(displayName)
                .category(category)
                .variableType(variableType)
                .build();
    }
    
    /**
     * 변수 정의 업데이트
     */
    public TemplateVariableDefinition update(
            String displayName,
            String description,
            String iconClass,
            Integer inputSize,
            Integer maxLength,
            String placeholderExample,
            Boolean isRequired,
            String validationRule,
            String validationMessage,
            String defaultValue,
            Integer displayOrder
    ) {
        this.displayName = displayName;
        this.description = description;
        this.iconClass = iconClass;
        this.inputSize = inputSize;
        this.maxLength = maxLength;
        this.placeholderExample = placeholderExample;
        this.isRequired = isRequired;
        this.validationRule = validationRule;
        this.validationMessage = validationMessage;
        this.defaultValue = defaultValue;
        this.displayOrder = displayOrder;
        return this;
    }
    
    /**
     * 활성화 상태 변경
     */
    public TemplateVariableDefinition activate() {
        this.isActive = true;
        return this;
    }
    
    /**
     * 비활성화 상태 변경
     */
    public TemplateVariableDefinition deactivate() {
        this.isActive = false;
        return this;
    }
    
    /**
     * HTML input 타입으로 변환
     */
    public String getHtmlInputType() {
        return switch (variableType) {
            case TIME -> "time";
            case DATE -> "date";
            case DATETIME -> "datetime-local";
            case EMAIL -> "email";
            case PHONE, NUMBER -> "tel";
            case IMAGE -> "file";
            default -> "text";
        };
    }
    
    /**
     * 변수 값 유효성 검증
     */
    public boolean isValidValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return !isRequired;
        }
        
        if (validationRule != null && !validationRule.isEmpty()) {
            return value.matches(validationRule);
        }
        
        return true;
    }
    
    /**
     * 검증 실패 시 에러 메시지 반환
     */
    public String getValidationErrorMessage() {
        if (validationMessage != null && !validationMessage.isEmpty()) {
            return validationMessage;
        }
        return displayName + "의 형식이 올바르지 않습니다.";
    }
}