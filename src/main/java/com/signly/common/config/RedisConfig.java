package com.signly.common.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // String 직렬화 설정
        StringRedisSerializer serializer = new StringRedisSerializer();
        template.setKeySerializer(serializer);
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(serializer);
        template.setHashValueSerializer(serializer);

        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // ObjectMapper 설정 - Java 8 날짜/시간 타입 지원 (타입 정보 포함)
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // JSON 직렬화 설정
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        // 기본 캐시 설정
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer)
                )
                .entryTtl(Duration.ofHours(1)) // 기본 TTL: 1시간
                .disableCachingNullValues();

        // 캐시별 개별 TTL 설정
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Phase 1: 변수 정의 & 템플릿 프리셋 (적극적 캐싱 - 긴 TTL)
        cacheConfigurations.put("variableDefinitions",
                defaultConfig.entryTtl(Duration.ofHours(24))); // 24시간
        cacheConfigurations.put("templatePresets",
                defaultConfig.entryTtl(Duration.ofDays(7))); // 7일 (거의 변경 안됨)

        // Phase 2 준비: 템플릿 (중간 TTL)
        // 사용자 정보는 캐싱하지 않음 (자주 변경되고 민감함)
        cacheConfigurations.put("templates",
                defaultConfig.entryTtl(Duration.ofHours(1))); // 1시간

        // Phase 3 준비: 대시보드 통계 & 서명 상태 (짧은 TTL)
        cacheConfigurations.put("dashboardStats",
                defaultConfig.entryTtl(Duration.ofMinutes(5))); // 5분
        cacheConfigurations.put("signatureStatus",
                defaultConfig.entryTtl(Duration.ofMinutes(10))); // 10분
        cacheConfigurations.put("contractsByToken",
                defaultConfig.entryTtl(Duration.ofMinutes(2))); // 2분

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}
