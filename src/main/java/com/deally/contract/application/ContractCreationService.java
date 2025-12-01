package com.deally.contract.application;

import com.deally.common.exception.ForbiddenException;
import com.deally.common.exception.NotFoundException;
import com.deally.common.exception.ValidationException;
import com.deally.contract.application.dto.CreateContractCommand;
import com.deally.contract.application.dto.UpdateContractCommand;
import com.deally.contract.application.support.ContractHtmlSanitizer;
import com.deally.contract.domain.model.Contract;
import com.deally.contract.domain.model.ContractContent;
import com.deally.contract.domain.model.ContractId;
import com.deally.contract.domain.model.PartyInfo;
import com.deally.contract.domain.repository.ContractRepository;
import com.deally.signature.application.FirstPartySignatureService;
import com.deally.template.domain.model.TemplateContent;
import com.deally.template.domain.model.TemplateId;
import com.deally.template.domain.repository.TemplateRepository;
import com.deally.template.domain.service.UnifiedTemplateRenderer;
import com.deally.user.domain.model.User;
import com.deally.user.domain.model.UserId;
import com.deally.user.domain.model.UserType;
import com.deally.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 계약서 생성 서비스
 * SRP: 계약서 생성 및 수정 책임만 담당
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ContractCreationService {

    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final TemplateRepository templateRepository;
    private final FirstPartySignatureService firstPartySignatureService;
    private final UnifiedTemplateRenderer unifiedTemplateRenderer;
    private final ContractAuthorizationService authorizationService;

    public Contract createContract(
            String userId,
            CreateContractCommand command
    ) {
        User user = validateUserAndPermissions(userId);

        if (user.getUserType() == UserType.OWNER) {
            firstPartySignatureService.ensureSignatureExists(userId);
        }

        var content = prepareContent(userId, command);
        var firstParty = createPartyInfo(
                command.firstPartyName(),
                command.firstPartyEmail(),
                command.firstPartyOrganization()
        );
        var secondParty = createPartyInfo(
                command.secondPartyName(),
                command.secondPartyEmail(),
                command.secondPartyOrganization()
        );

        var expiresAt = command.expiresAt() != null ?
                command.expiresAt() : LocalDateTime.now().plusHours(24);

        var templateId = StringUtils.hasText(command.templateId()) ?
                TemplateId.of(command.templateId().trim()) : null;

        var contract = Contract.create(
                UserId.of(userId),
                templateId,
                command.title(),
                content,
                firstParty,
                secondParty,
                expiresAt,
                command.presetType()
        );

        return contractRepository.save(contract);
    }

    public Contract updateContract(
            String userId,
            String contractId,
            UpdateContractCommand command
    ) {
        var contract = contractRepository.findById(ContractId.of(contractId))
                .orElseThrow(() -> new NotFoundException("계약서를 찾을 수 없습니다"));

        authorizationService.validateOwnership(userId, contract);

        contract.updateTitle(command.title());
        String sanitizedContent = ContractHtmlSanitizer.sanitize(command.content());
        ContractContent newContent = ContractContent.of(sanitizedContent);
        contract.updateContent(newContent);

        if (command.expiresAt() != null) {
            contract.updateExpirationDate(command.expiresAt());
        }

        return contractRepository.save(contract);
    }

    public void deleteContract(
            String userId,
            String contractId
    ) {
        var contract = contractRepository.findById(ContractId.of(contractId))
                .orElseThrow(() -> new NotFoundException("계약서를 찾을 수 없습니다"));

        authorizationService.validateOwnership(userId, contract);

        if (!contract.canDelete()) {
            throw new ValidationException("DRAFT 상태의 계약서만 삭제할 수 있습니다");
        }

        contractRepository.delete(contract);
    }

    private User validateUserAndPermissions(String userId) {
        var userIdObj = UserId.of(userId);
        var user = userRepository.findById(userIdObj)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));

        if (!user.canCreateContract()) {
            throw new ForbiddenException("계약서를 생성할 권한이 없습니다");
        }

        return user;
    }

    private ContractContent prepareContent(
            String userId,
            CreateContractCommand command
    ) {
        if (!StringUtils.hasText(command.templateId())) {
            String sanitizedContent = ContractHtmlSanitizer.sanitize(command.content());
            return ContractContent.of(sanitizedContent);
        }

        var templateId = TemplateId.of(command.templateId().trim());
        var template = templateRepository.findById(templateId)
                .orElseThrow(() -> new NotFoundException("템플릿을 찾을 수 없습니다"));

        authorizationService.validateTemplateOwnership(userId, template);

        var variableValues = command.variableValues() != null ?
                command.variableValues() : new HashMap<String, String>();

        validateTemplateVariables(template.getContent(), variableValues);

        String renderedHtml = unifiedTemplateRenderer.renderWithVariables(template.getContent(), variableValues);
        String sanitizedContent = ContractHtmlSanitizer.sanitize(renderedHtml);

        return ContractContent.of(sanitizedContent);
    }

    private PartyInfo createPartyInfo(
            String name,
            String email,
            String organization
    ) {
        return PartyInfo.of(name, email, organization);
    }

    private void validateTemplateVariables(
            TemplateContent templateContent,
            Map<String, String> variableValues
    ) {
        var templateVariables = templateContent.metadata().variables();

        for (var entry : templateVariables.entrySet()) {
            String varName = entry.getKey();
            com.deally.template.domain.model.TemplateVariable varDef = entry.getValue();

            String value = variableValues.get(varName);

            if (varDef.required() && (value == null || value.trim().isEmpty())) {
                throw new ValidationException(
                        String.format("필수 변수 '%s'의 값이 제공되지 않았습니다.", varDef.label())
                );
            }

            if (value != null && !value.trim().isEmpty()) {
                try {
                    varDef.validateValue(value);
                } catch (ValidationException e) {
                    throw new ValidationException(
                            String.format("변수 '%s'의 값이 유효하지 않습니다: %s", varDef.label(), e.getMessage())
                    );
                }
            }
        }
    }
}
