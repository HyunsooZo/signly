package com.deally.home.application.dto;

import com.deally.contract.application.dto.ContractResponse;
import com.deally.template.application.dto.TemplateResponse;

import java.util.List;
import java.util.Map;

/**
 * 대시보드 데이터 응답 DTO
 */
public record DashboardResponse(
        List<TemplateResponse> recentTemplates,
        List<ContractResponse> recentContracts,
        Map<String, Long> templateStats,
        Map<String, Long> contractStats
) {
}
