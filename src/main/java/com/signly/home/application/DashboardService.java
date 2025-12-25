package com.signly.home.application;

import com.signly.contract.application.ContractService;
import com.signly.contract.application.dto.ContractResponse;
import com.signly.contract.domain.model.ContractStatus;
import com.signly.contract.domain.repository.ContractRepository;
import com.signly.home.application.dto.DashboardResponse;
import com.signly.template.application.TemplateService;
import com.signly.template.application.dto.TemplateResponse;
import com.signly.template.domain.model.TemplateStatus;
import com.signly.template.domain.repository.TemplateRepository;
import com.signly.user.domain.model.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
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
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardService {
    private static final int RECENT_ITEMS_SIZE = 5;

    private final TemplateService templateService;
    private final ContractService contractService;
    private final TemplateRepository templateRepository;
    private final ContractRepository contractRepository;

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
            log.warn("최근 템플릿 조회 실패: userId={}", userId, e);
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
            log.warn("최근 계약서 조회 실패: userId={}", userId, e);
            return List.of();
        }
    }

    /**
     * 템플릿 통계 조회 (최적화: count 쿼리 직접 사용)
     * 캐시 키: userId + ':templates'
     * TTL: 5분 (통계는 실시간일 필요 없음)
     */
    @Cacheable(value = "dashboardStats", key = "#userId + ':templates'")
    public Map<String, Long> getTemplateStatistics(String userId) {
        var stats = new HashMap<String, Long>();
        var userIdObj = UserId.of(userId);

        try {
            stats.put("total", templateRepository.countByOwnerId(userIdObj));
            stats.put("active", templateRepository.countByOwnerIdAndStatus(userIdObj, TemplateStatus.ACTIVE));
            stats.put("draft", templateRepository.countByOwnerIdAndStatus(userIdObj, TemplateStatus.DRAFT));
            log.info("Loaded template stats from DB: {} (cache miss)", userId);
        } catch (Exception e) {
            log.warn("템플릿 통계 조회 실패: userId={}", userId, e);
            stats.put("total", 0L);
            stats.put("active", 0L);
            stats.put("draft", 0L);
        }

        return stats;
    }

    /**
     * 계약서 통계 조회 (최적화: count 쿼리 직접 사용)
     * 캐시 키: userId + ':contracts'
     * TTL: 5분 (통계는 실시간일 필요 없음)
     */
    @Cacheable(value = "dashboardStats", key = "#userId + ':contracts'")
    public Map<String, Long> getContractStatistics(String userId) {
        var stats = new HashMap<String, Long>();
        var userIdObj = UserId.of(userId);

        try {
            stats.put("total", contractRepository.countByCreatorId(userIdObj));
            stats.put("draft", contractRepository.countByCreatorIdAndStatus(userIdObj, ContractStatus.DRAFT));
            stats.put("pending", contractRepository.countByCreatorIdAndStatus(userIdObj, ContractStatus.PENDING));
            stats.put("signed", contractRepository.countByCreatorIdAndStatus(userIdObj, ContractStatus.SIGNED));
            stats.put("completed", contractRepository.countByCreatorIdAndStatus(userIdObj, ContractStatus.SIGNED));
            log.info("Loaded contract stats from DB: {} (cache miss)", userId);
        } catch (Exception e) {
            log.warn("계약서 통계 조회 실패: userId={}", userId, e);
            stats.put("total", 0L);
            stats.put("draft", 0L);
            stats.put("pending", 0L);
            stats.put("signed", 0L);
            stats.put("completed", 0L);
        }

        return stats;
    }

    /**
     * 대시보드 통계 캐시 무효화 헬퍼 메서드
     * 템플릿이나 계약서가 생성/삭제/상태변경될 때 호출
     */
    private void evictDashboardStatsCache(String userId) {
        // 구현은 외부 서비스에서 호출될 때 처리
        // 실제로는 @CacheEvict를 직접 사용하거나 AOP로 처리
    }

}
