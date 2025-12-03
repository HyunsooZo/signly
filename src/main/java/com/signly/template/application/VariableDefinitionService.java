package com.signly.template.application;

import com.signly.common.validation.ValidationResult;
import com.signly.template.application.dto.VariableDefinitionDto;
import com.signly.template.domain.model.TemplateVariableDefinition;
import com.signly.template.domain.model.VariableCategory;
import com.signly.template.domain.model.VariableType;
import com.signly.template.domain.repository.VariableDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 템플릿 변수 정의 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class VariableDefinitionService {
    
    private final VariableDefinitionRepository variableDefinitionRepository;
    
    /**
     * 모든 활성화된 변수 정의 조회
     */
    public List<VariableDefinitionDto> getAllActiveVariables() {
        try {
            List<TemplateVariableDefinition> definitions = variableDefinitionRepository.findAllActive();
            log.debug("Loaded {} active variable definitions from DB", definitions.size());
            
            return definitions.stream()
                    .map(VariableDefinitionDto::from)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to load variable definitions from DB", e);
            throw new RuntimeException("Failed to load variable definitions", e);
        }
    }
    
    /**
     * 카테고리별 변수 정의 조회
     */
    public Map<String, List<VariableDefinitionDto>> getVariablesByCategory() {
        try {
            List<TemplateVariableDefinition> all = variableDefinitionRepository.findAllActive();
            log.debug("Loaded {} active variable definitions grouped by category", all.size());
            
            return all.stream()
                    .map(VariableDefinitionDto::from)
                    .collect(Collectors.groupingBy(
                        dto -> dto.category().getDisplayName(),
                        LinkedHashMap::new,
                        Collectors.toList()
                    ));
        } catch (Exception e) {
            log.error("Failed to load variable definitions grouped by category", e);
            throw new RuntimeException("Failed to load variable definitions by category", e);
        }
    }
    
    /**
     * 특정 변수 정의 조회
     */
    public Optional<VariableDefinitionDto> getVariableByName(String variableName) {
        try {
            return variableDefinitionRepository.findByVariableName(variableName)
                    .map(VariableDefinitionDto::from);
        } catch (Exception e) {
            log.error("Failed to load variable definition by name: {}", variableName, e);
            return Optional.empty();
        }
    }
    
    /**
     * 카테고리별 변수 정의 조회
     */
    public List<VariableDefinitionDto> getVariablesByCategory(VariableCategory category) {
        try {
            return variableDefinitionRepository.findByCategory(category).stream()
                    .map(VariableDefinitionDto::from)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to load variable definitions by category: {}", category, e);
            return List.of();
        }
    }
    
    /**
     * 변수 유효성 검증
     */
    public ValidationResult validateVariableValue(String variableName, String value) {
        try {
            Optional<TemplateVariableDefinition> defOpt = 
                variableDefinitionRepository.findByVariableName(variableName);
            
            if (defOpt.isEmpty()) {
                // 정의가 없으면 통과 (하위 호환성)
                log.debug("No definition found for variable: {}, passing validation", variableName);
                return ValidationResult.success();
            }
            
            TemplateVariableDefinition def = defOpt.get();
            
            // 필수값 검사
            if (def.getIsRequired() && (value == null || value.trim().isEmpty())) {
                String message = def.getDisplayName() + "은(는) 필수 항목입니다.";
                log.debug("Required validation failed for variable: {}", variableName);
                return ValidationResult.failure(message);
            }
            
            // 값이 없으면 검증 통과
            if (value == null || value.trim().isEmpty()) {
                return ValidationResult.success();
            }
            
            // 정규식 검증
            if (def.getValidationRule() != null && !def.getValidationRule().isEmpty()) {
                if (!def.isValidValue(value)) {
                    String message = def.getValidationErrorMessage();
                    log.debug("Pattern validation failed for variable: {}, value: {}", variableName, value);
                    return ValidationResult.failure(message);
                }
            }
            
            log.debug("Validation passed for variable: {}", variableName);
            return ValidationResult.success();
            
        } catch (Exception e) {
            log.error("Error during validation for variable: {}", variableName, e);
            return ValidationResult.failure("검증 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 변수 정의 생성 (관리자용)
     */
    @Transactional
    public VariableDefinitionDto createVariableDefinition(
            String variableName,
            String displayName,
            VariableCategory category,
            VariableType variableType,
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
        // 중복 체크
        if (variableDefinitionRepository.existsByVariableName(variableName)) {
            throw new IllegalArgumentException("Variable name already exists: " + variableName);
        }
        
        TemplateVariableDefinition definition = TemplateVariableDefinition.create(
                variableName, displayName, category, variableType
        );
        
        definition.update(
                displayName, description, iconClass, inputSize, maxLength,
                placeholderExample, isRequired, validationRule, validationMessage,
                defaultValue, displayOrder
        );
        
        TemplateVariableDefinition saved = variableDefinitionRepository.save(definition);
        log.info("Created new variable definition: {}", saved.getVariableName());
        
        return VariableDefinitionDto.from(saved);
    }
    
    /**
     * 변수 정의 업데이트 (관리자용)
     */
    @Transactional
    public VariableDefinitionDto updateVariableDefinition(
            Long id,
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
        TemplateVariableDefinition definition = variableDefinitionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Variable definition not found: " + id));
        
        definition.update(
                displayName, description, iconClass, inputSize, maxLength,
                placeholderExample, isRequired, validationRule, validationMessage,
                defaultValue, displayOrder
        );
        
        TemplateVariableDefinition saved = variableDefinitionRepository.save(definition);
        log.info("Updated variable definition: {}", saved.getVariableName());
        
        return VariableDefinitionDto.from(saved);
    }
    
    /**
     * 변수 정의 활성화/비활성화 (관리자용)
     */
    @Transactional
    public void toggleVariableActivation(Long id, boolean activate) {
        TemplateVariableDefinition definition = variableDefinitionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Variable definition not found: " + id));
        
        if (activate) {
            definition.activate();
            log.info("Activated variable definition: {}", definition.getVariableName());
        } else {
            definition.deactivate();
            log.info("Deactivated variable definition: {}", definition.getVariableName());
        }
        
        variableDefinitionRepository.save(definition);
    }
}