package com.signly.signature.application;

import com.signly.common.storage.FileStorageService;
import com.signly.common.storage.StoredFile;
import com.signly.contract.domain.model.ContractId;
import com.signly.signature.application.dto.CreateSignatureCommand;
import com.signly.signature.application.mapper.SignatureDtoMapper;
import com.signly.signature.domain.model.ContractSignature;
import com.signly.signature.domain.repository.SignatureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignatureServiceTest {

    @Mock
    private SignatureRepository signatureRepository;

    @Mock
    private FileStorageService fileStorageService;

    private SignatureService signatureService;

    @BeforeEach
    void setUp() {
        signatureService = new SignatureService(signatureRepository, new SignatureDtoMapper(), fileStorageService);
    }

    @Test
    void createSignature_normalizesSignerEmailBeforeSaving() {
        String contractId = "contract-123";
        String rawEmail = "User@Example.com \t";
        String normalizedEmail = "user@example.com";
        CreateSignatureCommand command = new CreateSignatureCommand(
                contractId,
                "data:image/png;base64,aGVsbG8=",
                rawEmail,
                "홍길동",
                "127.0.0.1",
                "Chrome"
        );

        when(signatureRepository.existsByContractIdAndSignerEmail(ContractId.of(contractId), normalizedEmail))
                .thenReturn(false);
        when(fileStorageService.storeFile(any(byte[].class), anyString(), anyString(), anyString()))
                .thenReturn(new StoredFile(
                        "stored.png",
                        "signature.png",
                        "signatures/contracts/contract-123/user-example-com/stored.png",
                        "image/png",
                        5,
                        LocalDateTime.now()
                ));

        signatureService.createSignature(command);

        ArgumentCaptor<ContractSignature> captor = ArgumentCaptor.forClass(ContractSignature.class);
        verify(signatureRepository).save(captor.capture());
        assertThat(captor.getValue().signerInfo().signerEmail()).isEqualTo(normalizedEmail);

        verify(signatureRepository).existsByContractIdAndSignerEmail(ContractId.of(contractId), normalizedEmail);
    }

    @Test
    void isContractSigned_usesNormalizedEmail() {
        String contractId = "contract-456";
        String normalizedEmail = "user@example.com";
        when(signatureRepository.existsByContractIdAndSignerEmail(ContractId.of(contractId), normalizedEmail))
                .thenReturn(true);

        boolean result = signatureService.isContractSigned(contractId, "User@Example.com");

        assertThat(result).isTrue();
        verify(signatureRepository).existsByContractIdAndSignerEmail(ContractId.of(contractId), normalizedEmail);
    }
}
