package com.signly.template.infrastructure.repository;

import com.signly.template.domain.model.TemplateVariableDefinition;
import com.signly.template.domain.model.VariableCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 템플릿 변수 정의 JPA Repository
 */
public interface VariableDefinitionJpaRepository extends JpaRepository<TemplateVariableDefinition, Long> {

    /**
     * 변수명으로 변수 정의 조회
     */
    Optional<TemplateVariableDefinition> findByVariableName(String variableName);

    /**
     * 활성화된 모든 변수 정의를 표시 순서대로 조회
     */
    List<TemplateVariableDefinition> findByIsActiveTrueOrderByDisplayOrder();

    /**
     * 카테고리별 활성화된 변수 정의를 표시 순서대로 조회
     */
    List<TemplateVariableDefinition> findByCategoryAndIsActiveTrueOrderByDisplayOrder(VariableCategory category);

    /**
     * 변수명 존재 여부 확인
     */
    boolean existsByVariableName(String variableName);

    /**
     * 특정 타입의 활성화된 변수 정의 조회
     */
    List<TemplateVariableDefinition> findByVariableTypeAndIsActiveTrueOrderByDisplayOrder(
            com.signly.template.domain.model.VariableType variableType
    );

    /**
     * 변수명 목록으로 변수 정의들 조회
     */
    @Query("""
            SELECT v 
            FROM TemplateVariableDefinition v 
            WHERE v.variableName IN :variableNames AND v.isActive = true
            """)
    List<TemplateVariableDefinition> findByVariableNamesAndIsActiveTrue(@Param("variableNames") List<String> variableNames);

    /**
     * 표시 순서 범위 내의 변수 정의 조회 (페이지네이션용)
     */
    @Query("""
            SELECT v 
            FROM TemplateVariableDefinition v 
            WHERE v.isActive = true 
            ORDER BY v.displayOrder
            """)
    List<TemplateVariableDefinition> findActiveVariablesOrderByDisplayOrder();
}