package com.signly.home.presentation.web;

import com.signly.common.security.CurrentUserProvider;
import com.signly.common.security.SecurityUser;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

@Controller
public class HomeWebController {

    private static final Logger logger = LoggerFactory.getLogger(HomeWebController.class);
    private final TemplateService templateService;
    private final ContractService contractService;
    private final CurrentUserProvider currentUserProvider;

    public HomeWebController(TemplateService templateService,
                             ContractService contractService,
                             CurrentUserProvider currentUserProvider) {
        this.templateService = templateService;
        this.contractService = contractService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home(@RequestHeader(value = "X-User-Id", defaultValue = "01ARZ3NDEKTSV4RRFFQ69G5FAV") String userId,
                       @AuthenticationPrincipal SecurityUser securityUser,
                       HttpServletRequest request,
                       Model model) {
        try {
            String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);
            // 대시보드 통계 데이터 수집
            PageRequest pageRequest = PageRequest.of(0, 5, Sort.by("createdAt").descending());

            // 최근 템플릿 목록
            Page<TemplateResponse> recentTemplates = templateService.getTemplatesByOwner(resolvedUserId, pageRequest);

            // 최근 계약서 목록
            Page<ContractResponse> recentContracts = contractService.getContractsByCreator(resolvedUserId, pageRequest);

            // 템플릿 통계
            Map<String, Long> templateStats = new HashMap<>();
            templateStats.put("total", getTotalTemplateCount(resolvedUserId));
            templateStats.put("active", getTemplateCountByStatus(resolvedUserId, TemplateStatus.ACTIVE));
            templateStats.put("draft", getTemplateCountByStatus(resolvedUserId, TemplateStatus.DRAFT));

            // 계약서 통계
            Map<String, Long> contractStats = new HashMap<>();
            contractStats.put("total", getTotalContractCount(resolvedUserId));
            contractStats.put("draft", getContractCountByStatus(resolvedUserId, ContractStatus.DRAFT));
            contractStats.put("pending", getContractCountByStatus(resolvedUserId, ContractStatus.PENDING));
            contractStats.put("signed", getContractCountByStatus(resolvedUserId, ContractStatus.SIGNED));
            contractStats.put("completed", getContractCountByStatus(resolvedUserId, ContractStatus.COMPLETED));

            model.addAttribute("pageTitle", "대시보드");
            model.addAttribute("recentTemplates", recentTemplates.getContent());
            model.addAttribute("recentContracts", recentContracts.getContent());
            model.addAttribute("templateStats", templateStats);
            model.addAttribute("contractStats", contractStats);

            if (securityUser != null) {
                model.addAttribute("currentUserName", securityUser.getName());
                model.addAttribute("currentUserEmail", securityUser.getEmail());
                model.addAttribute("currentUserId", securityUser.getUserId());
                model.addAttribute("currentUserCompany", securityUser.getCompanyName());
                model.addAttribute("currentUserBusinessPhone", securityUser.getBusinessPhone());
                model.addAttribute("currentUserBusinessAddress", securityUser.getBusinessAddress());
            }

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
