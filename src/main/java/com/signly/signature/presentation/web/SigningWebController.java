package com.signly.signature.presentation.web;

import com.signly.contract.application.ContractService;
import com.signly.contract.application.dto.ContractResponse;
import com.signly.signature.application.SignatureService;
import com.signly.signature.application.dto.CreateSignatureCommand;
import com.signly.signature.application.dto.SignatureResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sign")
public class SigningWebController {

    private static final Logger logger = LoggerFactory.getLogger(SigningWebController.class);
    private final ContractService contractService;
    private final SignatureService signatureService;

    public SigningWebController(ContractService contractService, SignatureService signatureService) {
        this.contractService = contractService;
        this.signatureService = signatureService;
    }

    @GetMapping("/{token}")
    public String signingPage(@PathVariable String token, Model model) {
        try {
            ContractResponse contract = contractService.getContractByToken(token);

            boolean alreadySigned = signatureService.isContractSigned(
                    contract.getId(),
                    contract.getSecondParty().getEmail()
            );

            if (alreadySigned) {
                model.addAttribute("errorMessage", "이미 서명된 계약서입니다.");
                return "sign/already_signed";
            }

            model.addAttribute("pageTitle", "계약서 서명");
            model.addAttribute("contract", contract);
            model.addAttribute("token", token);

            return "sign/signature";

        } catch (Exception e) {
            logger.error("서명 페이지 로드 중 오류 발생: token={}", token, e);
            model.addAttribute("errorMessage", "계약서를 찾을 수 없거나 만료된 링크입니다.");
            return "sign/error";
        }
    }

    @GetMapping("/{token}/verify")
    public String verifyPage(@PathVariable String token, Model model) {
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
    public String verifyAccess(@PathVariable String token,
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
    public String processSignature(@PathVariable String token,
                                 @RequestParam String signatureData,
                                 @RequestParam String signerEmail,
                                 @RequestParam String signerName,
                                 HttpServletRequest request) {
        try {
            ContractResponse contract = contractService.getContractByToken(token);

            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");

            CreateSignatureCommand command = new CreateSignatureCommand(
                    contract.getId(),
                    signatureData,
                    signerEmail,
                    signerName,
                    ipAddress,
                    userAgent
            );

            SignatureResponse signature = signatureService.createSignature(command);

            contractService.processSignature(contract.getId(), signerEmail, signerName,
                                           signatureData, ipAddress);

            logger.info("서명 처리 완료: contractId={}, signatureId={}",
                       contract.getId(), signature.signatureId());

            return "{\"success\": true, \"message\": \"서명이 완료되었습니다.\"}";

        } catch (Exception e) {
            logger.error("서명 처리 중 오류 발생: token={}", token, e);
            return "{\"success\": false, \"message\": \"" + e.getMessage() + "\"}";
        }
    }

    @GetMapping("/{token}/complete")
    public String completePage(@PathVariable String token, Model model) {
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
}
