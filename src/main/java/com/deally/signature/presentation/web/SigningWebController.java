package com.deally.signature.presentation.web;

import com.deally.common.image.ImageResizer;
import com.deally.contract.application.ContractService;
import com.deally.contract.application.dto.ContractResponse;
import com.deally.signature.application.FirstPartySignatureService;
import com.deally.signature.application.SignatureService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sign")
@RequiredArgsConstructor
public class SigningWebController {

    private static final Logger logger = LoggerFactory.getLogger(SigningWebController.class);
    private final ContractService contractService;
    private final SignatureService signatureService;
    private final FirstPartySignatureService firstPartySignatureService;
    private final ImageResizer imageResizer;

    @GetMapping("/{token}")
    public String signingPage(
            @PathVariable String token,
            Model model) {
        try {
            ContractResponse contract = contractService.getContractByToken(token);

            // 계약서 상태가 SIGNED 또는 COMPLETED인 경우 완료 페이지로 리다이렉트
            if (contract.getStatus().name().equals("SIGNED") || contract.getStatus().name().equals("COMPLETED")) {
                logger.info("이미 서명 완료된 계약서, 완료 페이지로 이동: token={}", token);
                return "redirect:/sign/" + token + "/complete";
            }

            model.addAttribute("pageTitle", "계약서 서명");
            model.addAttribute("contract", contract);
            model.addAttribute("token", token);

            // 서명 이미지 처리 및 플레이스홀더 교체
            String processedContent = processSignaturePlaceholders(contract);
            model.addAttribute("processedContent", processedContent);

            return "sign/signature";

        } catch (Exception e) {
            logger.error("서명 페이지 로드 중 오류 발생: token={}", token, e);
            model.addAttribute("errorMessage", "계약서를 찾을 수 없거나 만료된 링크입니다.");
            return "sign/error";
        }
    }

    @GetMapping("/{token}/verify")
    public String verifyPage(
            @PathVariable String token,
            Model model) {
        try {
            ContractResponse contract = contractService.getContractByToken(token);

            model.addAttribute("pageTitle", "서명자 인증");
            model.addAttribute("contract", contract);
            model.addAttribute("token", token);

            return "sign/verify";

        } catch (Exception e) {
            logger.error("서명자 인증 페이지 로드 중 오류 발생: token={}", token, e);
            model.addAttribute("errorMessage", "계약서를 찾을 수 없거나 만료된 링크입니다.");
            return "sign/error";
        }
    }

    @PostMapping("/{token}/verify")
    public String verifyAccess(
            @PathVariable String token,
            @RequestParam String signerEmail,
            @RequestParam String signerName,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            ContractResponse contract = contractService.getContractByToken(token);

            if (!contract.getSecondParty().getEmail().equals(signerEmail)) {
                model.addAttribute("errorMessage", "서명자 정보가 일치하지 않습니다.");
                model.addAttribute("contract", contract);
                model.addAttribute("token", token);
                return "sign/verify";
            }

            redirectAttributes.addAttribute("verified", "true");
            return "redirect:/sign/" + token;

        } catch (Exception e) {
            logger.error("서명자 인증 중 오류 발생: token={}", token, e);
            model.addAttribute("errorMessage", "인증 중 오류가 발생했습니다.");
            return "sign/verify";
        }
    }

    @PostMapping("/{token}/sign")
    @ResponseBody
    public String processSignature(
            @PathVariable String token,
            @RequestParam String signatureData,
            @RequestParam String signerEmail,
            @RequestParam String signerName,
            HttpServletRequest request) {
        try {
            logger.info("서명 처리 요청: token={}, signerEmail={}", token, signerEmail);

            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");

            // Contract 상태 업데이트 및 서명 처리
            ContractResponse contract = contractService.processSignature(token, signerEmail, signerName, signatureData,
                    ipAddress);

            logger.info("서명 처리 완료: contractId={}", contract.getId());

            return "{\"success\": true, \"message\": \"서명이 완료되었습니다.\"}";

        } catch (Exception e) {
            logger.error("서명 처리 중 오류 발생: token={}", token, e);
            return "{\"success\": false, \"message\": \"" + e.getMessage() + "\"}";
        }
    }

    @GetMapping("/{token}/complete")
    public String completePage(
            @PathVariable String token,
            Model model) {
        try {
            ContractResponse contract = contractService.getContractByToken(token);

            model.addAttribute("pageTitle", "서명 완료");
            model.addAttribute("contract", contract);

            return "sign/complete";

        } catch (Exception e) {
            logger.error("서명 완료 페이지 로드 중 오류 발생: token={}", token, e);
            model.addAttribute("errorMessage", "계약서를 찾을 수 없습니다.");
            return "sign/error";
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private String processSignaturePlaceholders(ContractResponse contract) {
        String content = contract.getContent();
        if (content == null) {
            return "";
        }

        // 1. 갑(사업주) 서명 이미지 처리
        // 갑의 서명 이미지를 가져와서 [EMPLOYER_SIGNATURE_IMAGE] 태그를 교체
        String employerSignatureImage = getEmployerSignatureImage(contract.getCreatorId());
        if (employerSignatureImage != null) {
            String imageTag = String.format(
                    "<img src=\"%s\" class=\"signature-stamp-image-element\" alt=\"서명\" style=\"max-width: 100%%; max-height: 100%%;\"/>",
                    employerSignatureImage);
            content = content.replace("[EMPLOYER_SIGNATURE_IMAGE]", imageTag);
        } else {
            // 서명 이미지가 없으면 빈 문자열로 대체하거나 기본 텍스트 표시
            content = content.replace("[EMPLOYER_SIGNATURE_IMAGE]", "");
        }

        // 2. 을(근로자) 서명란 처리
        // 을의 서명란은 사용자가 서명해야 할 곳이므로 점선 박스로 표시
        // [EMPLOYEE_SIGNATURE_IMAGE] 태그를 점선 박스 div로 교체
        String placeholderBox = "<div class=\"signature-placeholder-box\"></div>";
        content = content.replace("[EMPLOYEE_SIGNATURE_IMAGE]", placeholderBox);

        return content;
    }

    private String getEmployerSignatureImage(String ownerId) {
        try {
            // FirstPartySignatureService를 통해 파일에서 원본 서명 로드
            String dataUrl = firstPartySignatureService.getSignatureDataUrl(ownerId);
            return imageResizer.resizeSignatureImage(dataUrl);
        } catch (Exception e) {
            logger.warn("갑(사업주) 서명을 찾을 수 없습니다: ownerId={}", ownerId);
            return null;
        }
    }
}
