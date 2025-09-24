package com.signly.home.presentation.web;

import com.signly.contract.application.ContractService;
import com.signly.contract.application.dto.ContractResponse;
import com.signly.contract.domain.model.ContractStatus;
import com.signly.template.application.TemplateService;
import com.signly.template.application.dto.TemplateResponse;
import com.signly.template.domain.model.TemplateStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.HashMap;
import java.util.Map;

@Controller
public class HomeWebController {

    private static final Logger logger = LoggerFactory.getLogger(HomeWebController.class);
    private final TemplateService templateService;
    private final ContractService contractService;

    public HomeWebController(TemplateService templateService, ContractService contractService) {
        this.templateService = templateService;
        this.contractService = contractService;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home(@RequestHeader(value = "X-User-Id", defaultValue = "dbd51de0-b234-47d8-893b-241c744e7337") String userId,
                       Model model) {
        try {
            // 대시보드 통계 데이터 수집
            PageRequest pageRequest = PageRequest.of(0, 5, Sort.by("createdAt").descending());

            // 최근 템플릿 목록
            Page<TemplateResponse> recentTemplates = templateService.getTemplatesByOwner(userId, pageRequest);

            // 최근 계약서 목록
            Page<ContractResponse> recentContracts = contractService.getContractsByCreator(userId, pageRequest);

            // 템플릿 통계
            Map<String, Long> templateStats = new HashMap<>();
            templateStats.put("total", getTotalTemplateCount(userId));
            templateStats.put("active", getTemplateCountByStatus(userId, TemplateStatus.ACTIVE));
            templateStats.put("draft", getTemplateCountByStatus(userId, TemplateStatus.DRAFT));

            // 계약서 통계
            Map<String, Long> contractStats = new HashMap<>();
            contractStats.put("total", getTotalContractCount(userId));
            contractStats.put("draft", getContractCountByStatus(userId, ContractStatus.DRAFT));
            contractStats.put("pending", getContractCountByStatus(userId, ContractStatus.PENDING));
            contractStats.put("signed", getContractCountByStatus(userId, ContractStatus.SIGNED));
            contractStats.put("completed", getContractCountByStatus(userId, ContractStatus.COMPLETED));

            model.addAttribute("pageTitle", "대시보드");
            model.addAttribute("recentTemplates", recentTemplates.getContent());
            model.addAttribute("recentContracts", recentContracts.getContent());
            model.addAttribute("templateStats", templateStats);
            model.addAttribute("contractStats", contractStats);

            return "home/dashboard";

        } catch (Exception e) {
            logger.error("대시보드 로드 중 오류 발생", e);
            model.addAttribute("errorMessage", "대시보드를 불러오는 중 오류가 발생했습니다.");
            return "home/dashboard";
        }
    }

    private long getTotalTemplateCount(String userId) {
        try {
            PageRequest pageRequest = PageRequest.of(0, 1);
            Page<TemplateResponse> templates = templateService.getTemplatesByOwner(userId, pageRequest);
            return templates.getTotalElements();
        } catch (Exception e) {
            logger.warn("템플릿 총 개수 조회 실패", e);
            return 0;
        }
    }

    private long getTemplateCountByStatus(String userId, TemplateStatus status) {
        try {
            PageRequest pageRequest = PageRequest.of(0, 1);
            Page<TemplateResponse> templates = templateService.getTemplatesByOwnerAndStatus(userId, status, pageRequest);
            return templates.getTotalElements();
        } catch (Exception e) {
            logger.warn("템플릿 상태별 개수 조회 실패: {}", status, e);
            return 0;
        }
    }

    private long getTotalContractCount(String userId) {
        try {
            PageRequest pageRequest = PageRequest.of(0, 1);
            Page<ContractResponse> contracts = contractService.getContractsByCreator(userId, pageRequest);
            return contracts.getTotalElements();
        } catch (Exception e) {
            logger.warn("계약서 총 개수 조회 실패", e);
            return 0;
        }
    }

    private long getContractCountByStatus(String userId, ContractStatus status) {
        try {
            PageRequest pageRequest = PageRequest.of(0, 1);
            Page<ContractResponse> contracts = contractService.getContractsByCreator(userId, pageRequest);
            return contracts.getTotalElements();
        } catch (Exception e) {
            logger.warn("계약서 상태별 개수 조회 실패: {}", status, e);
            return 0;
        }
    }
}