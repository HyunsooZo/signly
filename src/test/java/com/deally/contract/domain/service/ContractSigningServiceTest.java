package com.deally.contract.domain.service;

import com.deally.common.exception.ValidationException;
import com.deally.contract.domain.model.Contract;
import com.deally.contract.domain.model.ContractContent;
import com.deally.contract.domain.model.ContractStatus;
import com.deally.contract.domain.model.PartyInfo;
import com.deally.contract.domain.model.Signature;
import com.deally.template.domain.model.TemplateId;
import com.deally.user.domain.model.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Contract Signing Service 테스트")
class ContractSigningServiceTest {

    private ContractSigningService contractSigningService;
    private Contract testContract;
    private final String firstPartyEmail = "first@example.com";
    private final String secondPartyEmail = "second@example.com";
    private final String invalidEmail = "invalid@example.com";

    @BeforeEach
    void setUp() {
        contractSigningService = new ContractSigningService();
        
        // 테스트용 계약서 생성
        PartyInfo firstParty = PartyInfo.of("First Party", firstPartyEmail, null);
        PartyInfo secondParty = PartyInfo.of("Second Party", secondPartyEmail, null);
        
        testContract = Contract.create(
            UserId.generate(),
            TemplateId.generate(),
            "Test Contract",
            ContractContent.of("Test Content"),
            firstParty,
            secondParty,
            LocalDateTime.now().plusDays(7)
        );
        testContract.sendForSigning(); // 서명 대기 상태로 변경
    }

    @Test
    @DisplayName("유효한 서명 요청 처리 성공")
    void shouldProcessValidSigningRequest() {
        // Given
        ContractSigningService.SigningRequest request = new ContractSigningService.SigningRequest(
            firstPartyEmail,
            "First Party",
            "signature-data-base64",
            "192.168.1.1"
        );

        // When
        ContractSigningService.SigningResult result = contractSigningService.processSigning(testContract, request);

        // Then
        assertThat(result.signature()).isNotNull();
        assertThat(result.signature().isSignedBy(firstPartyEmail)).isTrue();
        assertThat(result.isFullySigned()).isFalse(); // 한 명만 서명했으므로 완전 서명 아님
        
        assertThat(testContract.getSignatures()).hasSize(1);
        assertThat(testContract.getStatus()).isEqualTo(ContractStatus.PENDING);
    }

    @Test
    @DisplayName("두 당사자 모두 서명 시 계약서 완전 서명 상태로 변경")
    void shouldMarkContractAsFullySignedWhenBothPartiesSign() {
        // Given
        ContractSigningService.SigningRequest firstRequest = new ContractSigningService.SigningRequest(
            firstPartyEmail,
            "First Party",
            "first-signature-data",
            "192.168.1.1"
        );
        
        ContractSigningService.SigningRequest secondRequest = new ContractSigningService.SigningRequest(
            secondPartyEmail,
            "Second Party",
            "second-signature-data",
            "192.168.1.2"
        );

        // When
        contractSigningService.processSigning(testContract, firstRequest);
        ContractSigningService.SigningResult result = contractSigningService.processSigning(testContract, secondRequest);

        // Then
        assertThat(result.isFullySigned()).isTrue();
        assertThat(testContract.getStatus()).isEqualTo(ContractStatus.SIGNED);
        assertThat(testContract.getSignatures()).hasSize(2);
    }

    @Test
    @DisplayName("서명 대기 상태가 아닌 계약서 서명 시 예외 발생")
    void shouldThrowExceptionWhenContractNotInSigningStatus() {
        // Given - 이미 완료된 계약서
        testContract.markAsFullySigned();
        
        ContractSigningService.SigningRequest request = new ContractSigningService.SigningRequest(
            firstPartyEmail,
            "First Party",
            "signature-data",
            "192.168.1.1"
        );

        // When & Then
        assertThatThrownBy(() -> contractSigningService.processSigning(testContract, request))
            .isInstanceOf(ValidationException.class)
            .hasMessage("서명 대기 상태에서만 서명할 수 있습니다");
    }

    @Test
    @DisplayName("만료된 계약서 서명 시 예외 발생")
    void shouldThrowExceptionWhenContractExpired() {
        // Given - 만료된 계약서 (유효한 만료일로 생성 후 수동 만료 처리)
        PartyInfo firstParty = PartyInfo.of("First Party", firstPartyEmail, null);
        PartyInfo secondParty = PartyInfo.of("Second Party", secondPartyEmail, null);
        
        Contract expiredContract = Contract.create(
            UserId.generate(),
            TemplateId.generate(),
            "Expired Contract",
            ContractContent.of("Content"),
            firstParty,
            secondParty,
            LocalDateTime.now().plusDays(7) // 유효한 만료일로 생성
        );
        expiredContract.sendForSigning(); // 서명 대기 상태로 변경
        expiredContract.expire(); // 수동으로 만료 상태로 변경
        
        ContractSigningService.SigningRequest request = new ContractSigningService.SigningRequest(
            firstPartyEmail,
            "First Party",
            "signature-data",
            "192.168.1.1"
        );

        // When & Then
        assertThatThrownBy(() -> contractSigningService.processSigning(expiredContract, request))
            .isInstanceOf(ValidationException.class)
            .hasMessage("서명 대기 상태에서만 서명할 수 있습니다");
    }

    @Test
    @DisplayName("권한 없는 사용자 서명 시 예외 발생")
    void shouldThrowExceptionWhenUserNotAuthorizedToSign() {
        // Given
        ContractSigningService.SigningRequest request = new ContractSigningService.SigningRequest(
            invalidEmail,
            "Invalid User",
            "signature-data",
            "192.168.1.1"
        );

        // When & Then
        assertThatThrownBy(() -> contractSigningService.processSigning(testContract, request))
            .isInstanceOf(ValidationException.class)
            .hasMessage("해당 계약서에 서명할 권한이 없습니다");
    }

    @Test
    @DisplayName("이미 서명한 사용자 재서명 시 예외 발생")
    void shouldThrowExceptionWhenUserAlreadySigned() {
        // Given - 첫 번째 서명
        ContractSigningService.SigningRequest firstRequest = new ContractSigningService.SigningRequest(
            firstPartyEmail,
            "First Party",
            "first-signature-data",
            "192.168.1.1"
        );
        contractSigningService.processSigning(testContract, firstRequest);
        
        // When & Then - 동일인 재서명 시도
        assertThatThrownBy(() -> contractSigningService.processSigning(testContract, firstRequest))
            .isInstanceOf(ValidationException.class)
            .hasMessage("이미 서명한 계약서입니다");
    }

    @Test
    @DisplayName("markSignedBy 메서드로 서명 상태 업데이트 성공")
    void shouldMarkSignedBySuccessfully() {
        // Given
        boolean allSignaturesComplete = false;

        // When
        contractSigningService.markSignedBy(testContract, firstPartyEmail, allSignaturesComplete);

        // Then
        assertThat(testContract.getStatus()).isEqualTo(ContractStatus.PENDING);
    }

    @Test
    @DisplayName("markSignedBy 메서드로 모든 서명 완료 상태로 변경")
    void shouldMarkAsFullySignedWhenAllSignaturesComplete() {
        // Given
        boolean allSignaturesComplete = true;

        // When
        contractSigningService.markSignedBy(testContract, firstPartyEmail, allSignaturesComplete);

        // Then
        assertThat(testContract.getStatus()).isEqualTo(ContractStatus.SIGNED);
    }

    @Test
    @DisplayName("markSignedBy 호출 시 권한 없는 사용자 예외 발생")
    void shouldThrowExceptionWhenMarkSignedByUnauthorizedUser() {
        // Given
        boolean allSignaturesComplete = false;

        // When & Then
        assertThatThrownBy(() -> contractSigningService.markSignedBy(testContract, invalidEmail, allSignaturesComplete))
            .isInstanceOf(ValidationException.class)
            .hasMessage("해당 계약서에 서명할 권한이 없습니다");
    }

    @Test
    @DisplayName("서명자 이메일 대소문자 구분 없이 처리")
    void shouldHandleEmailCaseInsensitive() {
        // Given
        ContractSigningService.SigningRequest request = new ContractSigningService.SigningRequest(
            firstPartyEmail.toUpperCase(), // 대문자로 변환
            "First Party",
            "signature-data",
            "192.168.1.1"
        );

        // When
        ContractSigningService.SigningResult result = contractSigningService.processSigning(testContract, request);

        // Then
        assertThat(result.signature()).isNotNull();
        assertThat(result.signature().isSignedBy(firstPartyEmail)).isTrue();
    }

    @Test
    @DisplayName("서명 데이터 정확히 저장")
    void shouldStoreSignatureDataCorrectly() {
        // Given
        String expectedSignatureData = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...";
        String expectedIpAddress = "192.168.1.100";
        
        ContractSigningService.SigningRequest request = new ContractSigningService.SigningRequest(
            firstPartyEmail,
            "First Party",
            expectedSignatureData,
            expectedIpAddress
        );

        // When
        ContractSigningService.SigningResult result = contractSigningService.processSigning(testContract, request);

        // Then
        Signature signature = result.signature();
        assertThat(signature.signatureData()).isEqualTo(expectedSignatureData);
        assertThat(signature.ipAddress()).isEqualTo(expectedIpAddress);
        assertThat(signature.signerName()).isEqualTo("First Party");
        assertThat(signature.signerEmail()).isEqualTo(firstPartyEmail);
    }
}