package com.signly.home.application;

import com.signly.contract.application.ContractService;
import com.signly.contract.application.dto.ContractResponse;
import com.signly.contract.domain.model.ContractStatus;
import com.signly.home.application.dto.DashboardResponse;
import com.signly.template.application.TemplateService;
import com.signly.template.application.dto.TemplateResponse;
import com.signly.template.domain.model.TemplateStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 대시보드 통계 및 데이터 조회 서비스
 * SRP: 대시보드 관련 비즈니스 로직만 담당
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);
    private static final int RECENT_ITEMS_SIZE = 5;

    private final TemplateService templateService;
    private final ContractService contractService;

    /**
     * 사용자의 대시보드 데이터를 조회합니다
     */
    public DashboardResponse getDashboardData(String userId) {
        PageRequest recentItemsPageRequest = PageRequest.of(0, RECENT_ITEMS_SIZE, Sort.by("createdAt").descending());

        var recentTemplates = getRecentTemplates(userId, recentItemsPageRequest);
        var recentContracts = getRecentContracts(userId, recentItemsPageRequest);
        var templateStats = getTemplateStatistics(userId);
        var contractStats = getContractStatistics(userId);

        return new DashboardResponse(recentTemplates, recentContracts, templateStats, contractStats);
    }

    /**
     * 최근 템플릿 목록 조회
     */
    private List<TemplateResponse> getRecentTemplates(
            String userId,
            PageRequest pageRequest
    ) {
        try {
            var templates = templateService.getTemplatesByOwner(userId, pageRequest);
            return templates.getContent();
        } catch (Exception e) {
            logger.warn("최근 템플릿 조회 실패: userId={}", userId, e);
            return List.of();
        }
    }

    /**
     * 최근 계약서 목록 조회
     */
    private List<ContractResponse> getRecentContracts(
            String userId,
            PageRequest pageRequest
    ) {
        try {
            var contracts = contractService.getContractsByCreator(userId, pageRequest);
            return contracts.getContent();
        } catch (Exception e) {
            logger.warn("최근 계약서 조회 실패: userId={}", userId, e);
            return List.of();
        }
    }

    /**
     * 템플릿 통계 조회
     */
    private Map<String, Long> getTemplateStatistics(String userId) {
        var stats = new HashMap<String, Long>();
        stats.put("total", getTemplateCount(userId, null));
        stats.put("active", getTemplateCount(userId, TemplateStatus.ACTIVE));
        stats.put("draft", getTemplateCount(userId, TemplateStatus.DRAFT));
        return stats;
    }

    /**
     * 계약서 통계 조회
     */
    private Map<String, Long> getContractStatistics(String userId) {
        var stats = new HashMap<String, Long>();
        stats.put("total", getContractCount(userId, null));
        stats.put("draft", getContractCount(userId, ContractStatus.DRAFT));
        stats.put("pending", getContractCount(userId, ContractStatus.PENDING));
        stats.put("signed", getContractCount(userId, ContractStatus.SIGNED));
        stats.put("completed", getContractCount(userId, ContractStatus.COMPLETED));
        return stats;
    }

    /**
     * 템플릿 개수 조회 (상태별 또는 전체)
     */
    private long getTemplateCount(
            String userId,
            TemplateStatus status
    ) {
        try {
            var pageRequest = PageRequest.of(0, 1);
            Page<TemplateResponse> templates;

            if (status != null) {
                templates = templateService.getTemplatesByOwnerAndStatus(userId, status, pageRequest);
            } else {
                templates = templateService.getTemplatesByOwner(userId, pageRequest);
            }

            return templates.getTotalElements();
        } catch (Exception e) {
            logger.warn("템플릿 개수 조회 실패: userId={}, status={}", userId, status, e);
            return 0;
        }
    }

    /**
     * 계약서 개수 조회 (상태별 또는 전체)
     */
    private long getContractCount(
            String userId,
            ContractStatus status
    ) {
        try {
            var pageRequest = PageRequest.of(0, 1);
            Page<ContractResponse> contracts;

            if (status != null) {
                contracts = contractService.getContractsByCreatorAndStatus(userId, status, pageRequest);
            } else {
                contracts = contractService.getContractsByCreator(userId, pageRequest);
            }

            return contracts.getTotalElements();
        } catch (Exception e) {
            logger.warn("계약서 개수 조회 실패: userId={}, status={}", userId, status, e);
            return 0;
        }
    }
}
