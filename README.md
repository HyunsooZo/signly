# E-Contract Signly

전자계약 서비스 애플리케이션

## 🚀 기술 스택

- **Backend**: Spring Boot 3.4.10, Java 21
- **Database**: MySQL 8.0
- **Cache**: Redis 7
- **Build**: Gradle
- **Containerization**: Docker, Docker Compose
- **CI/CD**: GitHub Actions

## 📋 사전 요구사항

- Docker & Docker Compose
- Java 21 (로컬 개발 시)
- Gradle (로컬 개발 시)

## 🏃 빠른 시작

### Docker Compose로 전체 스택 실행

```bash
# 개발 환경 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f app

# 중지
docker-compose down
```

### 로컬 개발 (Gradle)

```bash
# 애플리케이션 실행
./gradlew bootRun

# 테스트
./gradlew test

# 빌드
./gradlew bootJar
```

## 🐳 Docker 설정

### 개발 환경
- **파일**: `docker-compose.yml`
- **용도**: 로컬 개발 및 테스트
- **포함**: MySQL, Redis, Spring Boot App

### 프로덕션 환경
- **파일**: `docker-compose.prod.yml`
- **용도**: 실제 서비스 배포
- **환경변수**: `.env` 파일 사용

### 프로덕션 배포 준비

1. 환경 변수 설정:
```bash
cp .env.example .env
# .env 파일 편집하여 실제 값 입력
```

2. 프로덕션 실행:
```bash
docker-compose -f docker-compose.prod.yml up -d
```

## ⚙️ CI/CD 파이프라인

### CI (Continuous Integration)
- **트리거**: Pull Request to main/develop
- **작업**:
  - 코드 체크아웃
  - Java 21 설정
  - Gradle 빌드
  - 테스트 실행
  - 테스트 결과 리포트

### CD (Continuous Deployment)
- **트리거**: Push to main
- **작업**:
  1. Docker 이미지 빌드
  2. Docker Hub에 이미지 푸시
  3. 프로덕션 서버에 배포
  4. 헬스체크

### GitHub Secrets 설정

Repository Settings > Secrets and variables > Actions에서 다음 시크릿 등록:

```
DOCKER_USERNAME       # Docker Hub 사용자명
DOCKER_PASSWORD       # Docker Hub 비밀번호 또는 토큰
DEPLOY_HOST          # 배포 서버 IP 또는 도메인
DEPLOY_USER          # SSH 사용자명
DEPLOY_SSH_KEY       # SSH Private Key
DEPLOY_PORT          # SSH 포트 (기본: 22)
DEPLOY_PATH          # 배포 경로 (예: /opt/signly)
```

## 🔧 주요 명령어

### Docker Compose

```bash
# 빌드 & 시작
docker-compose up --build -d

# 로그 확인
docker-compose logs -f [service-name]

# 서비스 재시작
docker-compose restart [service-name]

# 중지
docker-compose down

# 볼륨 포함 완전 삭제
docker-compose down -v

# 컨테이너 상태 확인
docker-compose ps
```

### Gradle

```bash
# 애플리케이션 실행
./gradlew bootRun

# 테스트
./gradlew test

# 빌드 (테스트 포함)
./gradlew build

# 빌드 (테스트 제외)
./gradlew bootJar -x test

# 의존성 확인
./gradlew dependencies

# 클린 빌드
./gradlew clean build
```

## 📡 API 엔드포인트

- **애플리케이션**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs
- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics

## 🗄️ 데이터베이스

### 로컬 개발 (Docker)
- **Host**: localhost
- **Port**: 3306
- **Database**: signly_dev
- **Username**: signly
- **Password**: (docker-compose.yml 참조)

### 마이그레이션
Flyway를 사용한 데이터베이스 마이그레이션:
```
src/main/resources/db/migration/
```

## 📦 프로젝트 구조

```
e-contract-singly/
├── src/
│   ├── main/
│   │   ├── java/com/signly/
│   │   │   ├── common/         # 공통 유틸리티
│   │   │   ├── contract/       # 계약 도메인
│   │   │   ├── signature/      # 서명 도메인
│   │   │   ├── template/       # 템플릿 도메인
│   │   │   └── user/           # 사용자 도메인
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── application-prod.yml
│   └── test/
├── .github/
│   └── workflows/
│       ├── ci.yml              # CI 파이프라인
│       └── cd.yml              # CD 파이프라인
├── Dockerfile                  # Docker 이미지 빌드
├── docker-compose.yml          # 개발 환경
├── docker-compose.prod.yml     # 프로덕션 환경
├── .dockerignore
├── .env.example                # 환경변수 예시
├── build.gradle
└── README.md
```

## 🔒 보안

- JWT 기반 인증
- Rate Limiting (Resilience4j)
- CORS 설정
- SQL Injection 방지
- XSS 방지
- HTTPS 지원 (프로덕션)

## 📝 라이센스

Proprietary - All rights reserved

## 👥 기여

프로젝트 관리자에게 문의하세요.

## 📞 문의

- Email: support@signly.kr
- GitHub: https://github.com/your-org/e-contract-singly
