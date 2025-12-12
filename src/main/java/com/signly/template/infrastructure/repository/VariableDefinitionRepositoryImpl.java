package com.signly.template.infrastructure.repository;

import com.signly.template.domain.model.TemplateVariableDefinition;
import com.signly.template.domain.model.VariableCategory;
import com.signly.template.domain.model.VariableType;
import com.signly.template.domain.repository.VariableDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 템플릿 변수 정의 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class VariableDefinitionRepositoryImpl implements VariableDefinitionRepository {

    private final VariableDefinitionJpaRepository jpaRepository;

    @Override
    public Optional<TemplateVariableDefinition> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<TemplateVariableDefinition> findByVariableName(String variableName) {
        return jpaRepository.findByVariableName(variableName);
    }

    @Override
    public List<TemplateVariableDefinition> findAllActive() {
        return jpaRepository.findByIsActiveTrueOrderByDisplayOrder();
    }

    @Override
    public List<TemplateVariableDefinition> findByCategory(VariableCategory category) {
        return jpaRepository.findByCategoryAndIsActiveTrueOrderByDisplayOrder(category);
    }

    @Override
    public List<TemplateVariableDefinition> findByVariableType(VariableType variableType) {
        return jpaRepository.findByVariableTypeAndIsActiveTrueOrderByDisplayOrder(variableType);
    }

    @Override
    public List<TemplateVariableDefinition> findByVariableNames(List<String> variableNames) {
        return jpaRepository.findByVariableNamesAndIsActiveTrue(variableNames);
    }

    @Override
    public TemplateVariableDefinition save(TemplateVariableDefinition definition) {
        return jpaRepository.save(definition);
    }

    @Override
    public void delete(TemplateVariableDefinition definition) {
        jpaRepository.delete(definition);
    }

    @Override
    public boolean existsByVariableName(String variableName) {
        return jpaRepository.existsByVariableName(variableName);
    }

    @Override
    public List<TemplateVariableDefinition> findAll() {
        return jpaRepository.findAll();
    }
}