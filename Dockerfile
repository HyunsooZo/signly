# =================================
# Stage 1: Build
# =================================
FROM --platform=$BUILDPLATFORM gradle:8.5-jdk21 AS builder

WORKDIR /app

# Gradle 캐시 최적화를 위해 의존성 파일만 먼저 복사
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# 의존성 다운로드 (이 레이어는 build.gradle 변경 시에만 재빌드됨)
RUN gradle dependencies --no-daemon || true

# 소스코드 복사
COPY src ./src

# 빌드 (테스트 제외)
RUN gradle bootWar --no-daemon -x test

# =================================
# Stage 2: Runtime
# =================================
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# 보안: non-root 유저 생성
RUN groupadd -r spring && useradd -r -g spring spring

# 필요한 디렉토리 생성
RUN mkdir -p /app/uploads /app/logs && \
    chown -R spring:spring /app

# 빌드된 WAR 파일 복사
COPY --from=builder /app/build/libs/*.war app.war

# 폰트 파일 복사 (PDF 생성용)
COPY --from=builder /app/src/main/resources/fonts /app/fonts

# 유저 변경
USER spring:spring

# 환경변수 기본값 설정
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 포트 노출
EXPOSE 8080

# 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.war"]
# Build timestamp: Tue Dec  2 23:49:17 KST 2025
