package com.signly.template.domain.repository;

import com.signly.template.domain.model.TemplateVariableDefinition;
import com.signly.template.domain.model.VariableCategory;
import com.signly.template.domain.model.VariableType;

import java.util.List;
import java.util.Optional;

/**
 * 템플릿 변수 정의 도메인 Repository
 */
public interface VariableDefinitionRepository {

    /**
     * ID로 변수 정의 조회
     */
    Optional<TemplateVariableDefinition> findById(Long id);

    /**
     * 변수명으로 변수 정의 조회
     */
    Optional<TemplateVariableDefinition> findByVariableName(String variableName);

    /**
     * 모든 활성화된 변수 정의 조회
     */
    List<TemplateVariableDefinition> findAllActive();

    /**
     * 카테고리별 활성화된 변수 정의 조회
     */
    List<TemplateVariableDefinition> findByCategory(VariableCategory category);

    /**
     * 타입별 활성화된 변수 정의 조회
     */
    List<TemplateVariableDefinition> findByVariableType(VariableType variableType);

    /**
     * 변수명 목록으로 변수 정의들 조회
     */
    List<TemplateVariableDefinition> findByVariableNames(List<String> variableNames);

    /**
     * 변수 정의 저장
     */
    TemplateVariableDefinition save(TemplateVariableDefinition definition);

    /**
     * 변수 정의 삭제
     */
    void delete(TemplateVariableDefinition definition);

    /**
     * 변수명 존재 여부 확인
     */
    boolean existsByVariableName(String variableName);

    /**
     * 모든 변수 정의 조회 (관리자용)
     */
    List<TemplateVariableDefinition> findAll();
}