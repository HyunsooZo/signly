package com.signly.contract.application;

import com.signly.common.storage.FileStorageService;
import com.signly.contract.domain.model.*;
import com.signly.contract.domain.repository.ContractRepository;
import com.signly.contract.domain.service.PdfGenerator;
import com.signly.signature.domain.model.ContractSignature;
import com.signly.signature.domain.repository.SignatureRepository;
import com.signly.template.domain.model.TemplateId;
import com.signly.user.domain.model.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractPdfServiceTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private SignatureRepository signatureRepository;

    @Mock
    private PdfGenerator pdfGenerator;

    @Mock
    private FileStorageService fileStorageService;

    private ContractPdfService contractPdfService;

    @BeforeEach
    void setUp() {
        contractPdfService = new ContractPdfService(contractRepository, signatureRepository, pdfGenerator, fileStorageService);
    }

    @Test
    void generateContractPdf_injectsSignatureIntoEncodedPlaceholder() {
        ContractId contractId = ContractId.generate();
        ContractContent content = ContractContent.of("<div>&#91;EMPLOYER_SIGNATURE_IMAGE&#93; &amp;#91;EMPLOYEE_SIGNATURE_IMAGE&amp;#93;</div>");
        PartyInfo firstParty = PartyInfo.of("갑", "owner@example.com", "회사");
        PartyInfo secondParty = PartyInfo.of("을", "employee@example.com", null);
        Contract contract = Contract.restore(
                contractId,
                UserId.generate(),
                TemplateId.generate(),
                "고용 계약서",
                content,
                firstParty,
                secondParty,
                ContractStatus.SIGNED,
                List.of(),
                SignToken.generate(),
                LocalDateTime.now().plusDays(1),
                PresetType.NONE,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );

        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));

        ContractSignature signature = ContractSignature.create(
                contractId,
                "data:image/png;base64,aGVsbG8=",
                secondParty.getEmail(),
                "을",
                "127.0.0.1",
                "Chrome"
        );
        when(signatureRepository.findByContractIdAndSignerEmail(contractId, firstParty.getEmail()))
                .thenReturn(Optional.empty());
        when(signatureRepository.findByContractIdAndSignerEmail(contractId, secondParty.getEmail()))
                .thenReturn(Optional.of(signature));

        ArgumentCaptor<String> htmlCaptor = ArgumentCaptor.forClass(String.class);
        when(pdfGenerator.generateFromHtml(htmlCaptor.capture(), anyString()))
                .thenReturn(GeneratedPdf.of(new byte[]{1}, "dummy.pdf"));

        contractPdfService.generateContractPdf(contractId.getValue());

        String finalHtml = htmlCaptor.getValue();
        assertThat(finalHtml).contains("<img src=\"data:image/png;base64,aGVsbG8=\"");
    }

    @Test
    void generateContractPdf_injectsSignatureIntoEmptyWrapperWhenPlaceholderMissing() {
        ContractId contractId = ContractId.generate();
        String htmlWithoutPlaceholder = "<span class=\"signature-stamp-wrapper\"><span class=\"contract-variable-underline\"></span></span>" +
                "<span class=\"signature-stamp-wrapper\"><span class=\"contract-variable-underline\"></span></span>";
        ContractContent content = ContractContent.of(htmlWithoutPlaceholder);
        PartyInfo firstParty = PartyInfo.of("갑", "owner@example.com", "회사");
        PartyInfo secondParty = PartyInfo.of("을", "employee@example.com", null);
        Contract contract = Contract.restore(
                contractId,
                UserId.generate(),
                TemplateId.generate(),
                "고용 계약서",
                content,
                firstParty,
                secondParty,
                ContractStatus.SIGNED,
                List.of(),
                SignToken.generate(),
                LocalDateTime.now().plusDays(1),
                PresetType.NONE,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );

        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));

        ContractSignature firstSignature = ContractSignature.create(
                contractId,
                "data:image/png;base64,Zmlyc3Q=",
                firstParty.getEmail(),
                "갑",
                "127.0.0.1",
                "Chrome"
        );

        ContractSignature secondSignature = ContractSignature.create(
                contractId,
                "data:image/png;base64,c2Vjb25k",
                secondParty.getEmail(),
                "을",
                "127.0.0.1",
                "Chrome"
        );

        when(signatureRepository.findByContractIdAndSignerEmail(contractId, firstParty.getEmail()))
                .thenReturn(Optional.of(firstSignature));
        when(signatureRepository.findByContractIdAndSignerEmail(contractId, secondParty.getEmail()))
                .thenReturn(Optional.of(secondSignature));

        ArgumentCaptor<String> htmlCaptor = ArgumentCaptor.forClass(String.class);
        when(pdfGenerator.generateFromHtml(htmlCaptor.capture(), anyString()))
                .thenReturn(GeneratedPdf.of(new byte[]{1}, "dummy.pdf"));

        contractPdfService.generateContractPdf(contractId.getValue());

        String finalHtml = htmlCaptor.getValue();
        assertThat(finalHtml).contains("data:image/png;base64,Zmlyc3Q=");
        assertThat(finalHtml).contains("data:image/png;base64,c2Vjb25k");
    }
}
