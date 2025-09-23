package com.signly.signature.presentation.web;

import com.signly.contract.domain.model.Contract;
import com.signly.contract.domain.repository.ContractRepository;
import com.signly.common.exception.NotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sign")
public class SignatureWebController {

    private final ContractRepository contractRepository;

    public SignatureWebController(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    @GetMapping("/{token}")
    public String signaturePage(@PathVariable String token, Model model) {
        // TODO: 토큰으로 계약서 조회 구현 후 실제 데이터 사용

        // 임시 데이터
        model.addAttribute("pageTitle", "계약서 서명 - Signly");
        model.addAttribute("signToken", token);

        // 임시 계약서 데이터
        model.addAttribute("contract", createMockContract());
        model.addAttribute("signerInfo", createMockSignerInfo());

        return "sign/signature";
    }

    @GetMapping("/{token}/verify")
    public String verifyPage(@PathVariable String token, Model model) {
        model.addAttribute("pageTitle", "서명자 인증 - Signly");
        model.addAttribute("signToken", token);
        return "sign/verify";
    }

    @GetMapping("/{token}/complete")
    public String completePage(@PathVariable String token, Model model) {
        model.addAttribute("pageTitle", "서명 완료 - Signly");
        model.addAttribute("signToken", token);
        return "sign/complete";
    }

    private MockContract createMockContract() {
        return new MockContract();
    }

    // Mock 클래스들을 내부 클래스로 정의
    public static class MockContract {
        public String getId() { return "contract-123"; }
        public String getTitle() { return "소프트웨어 개발 계약서"; }
        public String getContent() {
            return """
                <h3>제1조 (목적)</h3>
                <p>본 계약은 갑과 을이 소프트웨어 개발에 관하여 체결하는 계약으로,
                양 당사자의 권리와 의무를 명확히 하는 것을 목적으로 한다.</p>

                <h3>제2조 (개발 범위)</h3>
                <p>을은 갑이 요구하는 다음의 소프트웨어를 개발하여 납품한다:</p>
                <ul>
                    <li>웹 애플리케이션 개발</li>
                    <li>모바일 앱 개발</li>
                    <li>데이터베이스 설계 및 구축</li>
                    <li>시스템 운영 및 유지보수</li>
                </ul>

                <h3>제3조 (계약 금액)</h3>
                <p>본 계약의 총 금액은 금 오천만원(￦50,000,000)으로 한다.</p>

                <h3>제4조 (납품 일정)</h3>
                <p>을은 계약 체결일로부터 6개월 이내에 완성된 소프트웨어를 갑에게 납품한다.</p>
                """;
        }
        public java.util.Date getCreatedAt() {
            return java.util.Date.from(java.time.LocalDateTime.now().minusDays(1)
                .atZone(java.time.ZoneId.systemDefault()).toInstant());
        }
        public java.util.Date getExpiresAt() {
            return java.util.Date.from(java.time.LocalDateTime.now().plusDays(6)
                .atZone(java.time.ZoneId.systemDefault()).toInstant());
        }
        public MockParty getFirstParty() {
            return new MockParty("테크컴퍼니 주식회사", "contact@techcompany.com");
        }
        public MockParty getSecondParty() {
            return new MockParty("김개발", "kim.developer@email.com");
        }
    }

    public static class MockParty {
        private final String name;
        private final String email;

        public MockParty(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() { return name; }
        public String getEmail() { return email; }
    }

    private MockSignerInfo createMockSignerInfo() {
        return new MockSignerInfo();
    }

    public static class MockSignerInfo {
        public String getName() { return "김개발"; }
        public String getEmail() { return "kim.developer@email.com"; }
        public String getPhone() { return "010-1234-5678"; }
        public String getCompany() { return "개발자협회"; }
    }
}