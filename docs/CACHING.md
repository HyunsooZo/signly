# 캐싱 가이드

## 개요

이 문서는 e-Signature Signly 애플리케이션의 캐싱 전략과 사용법을 설명합니다.

## 캐시 아키텍처

### 캐시 저장소
- **Redis**: 분산 캐시 저장소 사용
- **Spring Cache Abstraction**: `@Cacheable`, `@CacheEvict` 어노테이션 기반

### 캐시 설정 위치
- `RedisConfig.java`: Redis 캐시 매니저 설정
- `CacheMetricsConfig.java`: 캐시 모니터링 및 메트릭 수집
- `application.yml`: Redis 연결 설정

---

## 구현된 캐시 (Phase 1)

### 1. Variable Definitions 캐시
**캐시 이름**: `variableDefinitions`  
**TTL**: 24시간  
**용도**: 템플릿 변수 정의 (거의 변경되지 않는 참조 데이터)

#### 캐시된 메서드
```java
// 모든 활성 변수 조회
@Cacheable(value = "variableDefinitions", key = "'all'")
List<VariableDefinitionDto> getAllActiveVariables()

// 카테고리별 변수 조회
@Cacheable(value = "variableDefinitions", key = "'byCategory'")
Map<String, List<VariableDefinitionDto>> getVariablesByCategory()

// 특정 변수 조회
@Cacheable(value = "variableDefinitions", key = "'byName:' + #variableName")
Optional<VariableDefinitionDto> getVariableByName(String variableName)
```

#### 캐시 무효화
관리자가 변수 정의를 생성/수정/활성화/비활성화할 때 자동으로 캐시가 삭제됩니다.

```java
@CacheEvict(value = "variableDefinitions", allEntries = true)
VariableDefinitionDto createVariableDefinition(...)

@CacheEvict(value = "variableDefinitions", allEntries = true)
VariableDefinitionDto updateVariableDefinition(...)

@CacheEvict(value = "variableDefinitions", allEntries = true)
void toggleVariableActivation(...)
```

**예상 효과**: 템플릿 관련 쿼리 60-80% 감소

---

### 2. Template Presets 캐시
**캐시 이름**: `templatePresets`  
**TTL**: 7일  
**용도**: 프리셋 템플릿 (배포 후 거의 변경되지 않는 데이터)

#### 캐시된 메서드
```java
// 프리셋 목록 조회
@Cacheable(value = "templatePresets", key = "'summaries'")
List<TemplatePresetSummary> getSummaries()

// 특정 프리셋 조회
@Cacheable(value = "templatePresets", key = "#presetId")
Optional<TemplatePreset> getPreset(String presetId)
```

#### 캐시 무효화
프리셋은 배포 후 변경되지 않으므로 자동 무효화가 없습니다.  
필요시 관리자가 수동으로 삭제할 수 있습니다.

**예상 효과**: 프리셋 템플릿 조회 쿼리 90% 이상 감소

---

## 캐시 모니터링

### 1. Spring Boot Actuator
캐시 상태 확인:
```bash
curl http://localhost:8080/actuator/caches
```

### 2. 캐시 메트릭
5분마다 자동으로 캐시 통계가 로그에 기록됩니다.

```
=== Cache Statistics Report ===
Cache [variableDefinitions] is active
Cache [templatePresets] is active
Total active caches: 2
===============================
```

### 3. Prometheus 메트릭
Spring Boot Actuator가 자동으로 캐시 메트릭을 수집합니다:

- `cache_gets_total{cache, result="hit"}` - 캐시 히트 수
- `cache_gets_total{cache, result="miss"}` - 캐시 미스 수
- `cache_puts_total{cache}` - 캐시 저장 수
- `cache_evictions_total{cache}` - 캐시 삭제 수

#### 히트율 계산 (Prometheus 쿼리)
```promql
# 히트율
rate(cache_gets_total{result="hit"}[5m]) / rate(cache_gets_total[5m])

# 미스율
rate(cache_gets_total{result="miss"}[5m]) / rate(cache_gets_total[5m])
```

---

## 캐시 관리 API

### 관리자용 캐시 관리 엔드포인트

**Base URL**: `/api/admin/cache`

#### 1. 캐시 정보 조회
```bash
GET /api/admin/cache/info
```

응답:
```json
{
  "totalCaches": 8,
  "cacheNames": ["variableDefinitions", "templatePresets", ...],
  "cacheManager": "RedisCacheManager"
}
```

#### 2. 모든 캐시 이름 조회
```bash
GET /api/admin/cache/names
```

#### 3. 특정 캐시 삭제
```bash
DELETE /api/admin/cache/variableDefinitions
```

#### 4. 모든 캐시 삭제
```bash
DELETE /api/admin/cache/all
```

#### 5. 특정 캐시의 특정 키 삭제
```bash
DELETE /api/admin/cache/variableDefinitions/all
```

#### 6. Phase별 캐시 삭제
```bash
# Phase 1 캐시 삭제 (variableDefinitions, templatePresets)
DELETE /api/admin/cache/phase1

# Phase 2 캐시 삭제 (templates, users, userDetails)
DELETE /api/admin/cache/phase2

# Phase 3 캐시 삭제 (dashboardStats, signatureStatus, contractsByToken)
DELETE /api/admin/cache/phase3
```

#### 7. 캐시 워밍 관리 (Phase 4)
```bash
# 캐시 워밍 통계 조회
GET /api/admin/cache/warming/stats

# 수동 캐시 워밍 실행
POST /api/admin/cache/warming/manual

# 특정 캐시만 워밍
POST /api/admin/cache/warming/variables      # 변수 정의만
POST /api/admin/cache/warming/presets        # 프리셋 템플릿만

# 수동 스케줄링 실행
POST /api/admin/cache/scheduling/daily-refresh   # 일일 변수 정의 갱신
POST /api/admin/cache/scheduling/weekly-refresh  # 주간 프리셋 갱신
```

---

## 향후 계획

### Phase 2: 템플릿 & 사용자 캐싱
**예정일**: TBD

- `templates` (TTL: 1시간) - 템플릿 조회
- `users` (TTL: 30분) - 사용자 프로필
- `userDetails` (TTL: 15분) - 인증 정보

**예상 효과**: 인증/계약 쿼리 40-50% 감소

### Phase 4: 캐시 워밍 & 스케줄링
**완료일**: 2025-12-22 ✅

- `CacheWarmingService` - 애플리케이션 시작 시 자동 캐시 워밍
- `CacheScheduleService` - 주기적 캐시 갱신 스케줄링
- `WarmupMetrics` - 캐시 워밍 성능 메트릭 수집
- **자동 워밍**: `variableDefinitions`, `templatePresets`
- **스케줄링**: 일일(2am) 변수 정의, 주간(월 3am) 프리셋 갱신
- **API**: 수동 워밍, 통계 조회, 스케줄링 제공

**기능**:
- Cold Start 방지
- 안정적인 초기 성능 보장
- 주기적 데이터 갱신
- 관리자 모니터링

**예상 효과**: 애플리케이션 시작 시 응답 속도 70% 개선

### Phase 3: 대시보드 & 통계 캐싱
**예정일**: TBD

- `dashboardStats` (TTL: 5분) - 대시보드 통계
- `signatureStatus` (TTL: 10분) - 서명 상태
- `contractsByToken` (TTL: 2분) - 토큰 기반 계약 조회

**예상 효과**: 대시보드/서명 쿼리 30-40% 감소

---

## 전체 성능 개선 예상치

- **Phase 1**: 템플릿 관련 쿼리 60-80% 감소 ✅ **완료**
- **Phase 4**: 캐시 워밍 및 스케줄링, Cold Start 방지 ✅ **완료**
- **Phase 2**: 인증/계약 쿼리 40-50% 감소
- **Phase 3**: 대시보드/서명 쿼리 30-40% 감소
- **전체**: 데이터베이스 부하 45-60% 감소 예상, 시작 시 응답 속도 70% 개선

---

## 개발 환경 설정

### Redis 실행 (Docker)
```bash
docker run -d \
  --name redis \
  -p 6379:6379 \
  redis:latest
```

### Redis 실행 (로컬)
```bash
brew install redis
brew services start redis
```

### 환경 변수 설정
`.env` 파일:
```properties
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
```

---

## 트러블슈팅

### 캐시가 작동하지 않는 경우

1. **Redis 연결 확인**
   ```bash
   redis-cli ping
   # 응답: PONG
   ```

2. **애플리케이션 로그 확인**
   ```
   # 캐시 미스 로그 (첫 번째 호출)
   Loaded 45 active variable definitions from DB (cache miss)
   
   # 캐시 히트 로그 (두 번째 호출 시 로그 없음)
   ```

3. **Actuator로 캐시 상태 확인**
   ```bash
   curl http://localhost:8080/actuator/caches
   ```

### 캐시 삭제가 필요한 경우

- 데이터 불일치가 발견된 경우
- 배포 후 새로운 데이터를 즉시 반영해야 하는 경우
- 테스트 환경 초기화가 필요한 경우

```bash
# 관리자 API로 삭제
curl -X DELETE http://localhost:8080/api/admin/cache/all
```

---

## 모범 사례

### 1. 캐시 키 네이밍
- 명확하고 일관된 네이밍 사용
- 예: `'all'`, `'byCategory'`, `'byName:' + #variableName`

### 2. TTL 설정
- **매우 정적**: 7일 (프리셋 템플릿)
- **정적**: 24시간 (변수 정의)
- **중간**: 1시간 (템플릿)
- **동적**: 5-30분 (사용자, 통계)
- **매우 동적**: 2분 (토큰 기반 조회)

### 3. 캐시 무효화
- `@CacheEvict(allEntries = true)`: 전체 삭제 (드문 업데이트)
- `@CacheEvict(key = "#id")`: 특정 키만 삭제 (빈번한 업데이트)

### 4. 로깅
- 캐시 미스 시 `log.info()` 사용 (성능 분석용)
- 캐시 히트는 로깅하지 않음 (불필요한 로그 방지)

---

## 참고 자료

- [Spring Cache Abstraction](https://docs.spring.io/spring-framework/reference/integration/cache.html)
- [Redis Documentation](https://redis.io/docs/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
