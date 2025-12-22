package com.signly.common.cache;

import com.signly.template.application.VariableDefinitionService;
import com.signly.template.application.dto.VariableDefinitionDto;
import com.signly.template.application.preset.TemplatePresetService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@SpringBootTest
@ActiveProfiles("test")
class CacheIntegrationTest {

    @Autowired
    private VariableDefinitionService variableDefinitionService;

    @Autowired
    private TemplatePresetService templatePresetService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CacheManagementService cacheManagementService;

    @Test
    void testCacheManager() {
        assertThat(cacheManager).isNotNull();

        var cacheNames = cacheManager.getCacheNames();

        assertThat(cacheNames).contains(
                "variableDefinitions",
                "templatePresets",
                "templates",
                "users",
                "userDetails",
                "dashboardStats",
                "signatureStatus",
                "contractsByToken"
        );
    }

    @Test
    void testVariableDefinitionsCaching() {
        // 캐시 초기화
        cacheManagementService.evictCache("variableDefinitions");

        long startTime1 = System.currentTimeMillis();
        List<VariableDefinitionDto> result1 = variableDefinitionService.getAllActiveVariables();
        long duration1 = System.currentTimeMillis() - startTime1;
        long startTime2 = System.currentTimeMillis();
        List<VariableDefinitionDto> result2 = variableDefinitionService.getAllActiveVariables();
        long duration2 = System.currentTimeMillis() - startTime2;

        // 결과 검증
        assertThat(result1).isEqualTo(result2);

        // 캐시에서 데이터 확인
        var cache = cacheManager.getCache("variableDefinitions");
        assertThat(cache).isNotNull();
        assertThat(cache.get("all")).isNotNull();
    }

    @Test
    void testVariableDefinitionsCachingByCategory() {
        // 캐시 초기화
        cacheManagementService.evictCache("variableDefinitions");

        // 첫 번째 호출 - DB에서 조회
        long startTime1 = System.currentTimeMillis();
        var result1 = variableDefinitionService.getVariablesByCategory();
        long duration1 = System.currentTimeMillis() - startTime1;

        // 두 번째 호출 - 캐시에서 조회
        long startTime2 = System.currentTimeMillis();
        var result2 = variableDefinitionService.getVariablesByCategory();
        long duration2 = System.currentTimeMillis() - startTime2;

        // 결과 검증
        assertThat(result1).isEqualTo(result2);

    }

    @Test
    void testTemplatePresetCaching() {
        // 캐시 초기화
        cacheManagementService.evictCache("templatePresets");

        // 첫 번째 호출 - DB에서 조회
        long startTime1 = System.currentTimeMillis();
        var result1 = templatePresetService.getSummaries();
        long duration1 = System.currentTimeMillis() - startTime1;

        // 두 번째 호출 - 캐시에서 조회
        long startTime2 = System.currentTimeMillis();
        var result2 = templatePresetService.getSummaries();
        long duration2 = System.currentTimeMillis() - startTime2;

        // 결과 검증
        assertThat(result1).isEqualTo(result2);


        // 캐시에서 데이터 확인
        var cache = cacheManager.getCache("templatePresets");
        assertThat(cache).isNotNull();
        assertThat(cache.get("summaries")).isNotNull();
    }

    @Test
    void testCacheEviction() {
        // 캐시 초기화 및 데이터 로드
        cacheManagementService.evictCache("variableDefinitions");
        variableDefinitionService.getAllActiveVariables(); // 캐시 채우기

        // 캐시가 있는지 확인
        var cache = cacheManager.getCache("variableDefinitions");
        assertThat(cache).isNotNull();
        assertThat(cache.get("all")).isNotNull();

        // 캐시 삭제
        cacheManagementService.evictCache("variableDefinitions");

        // 캐시가 삭제되었는지 확인
        assertThat(cache.get("all")).isNull();
    }

    @Test
    void testCacheManagementServiceInfo() {
        var info = cacheManagementService.getCacheInfo();

        assertThat(info).containsKeys("totalCaches", "cacheNames", "cacheManager");
        assertThat((Integer) info.get("totalCaches")).isGreaterThan(0);
    }
}
