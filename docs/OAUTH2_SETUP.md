# Google OAuth2 로그인 설정 가이드

## 개요
Signly는 Google OAuth2를 통한 소셜 로그인을 지원합니다. 이 문서는 Google OAuth2 설정 방법을 안내합니다.

## 1. Google Cloud Console 설정

### 1.1 프로젝트 생성
1. [Google Cloud Console](https://console.cloud.google.com/)에 접속
2. 새 프로젝트 생성 또는 기존 프로젝트 선택

### 1.2 OAuth 동의 화면 구성
1. 좌측 메뉴에서 **APIs & Services > OAuth consent screen** 선택
2. User Type: **External** 선택 (내부 조직용이면 Internal)
3. 앱 정보 입력:
   - App name: `Signly`
   - User support email: 지원 이메일 주소
   - Developer contact information: 개발자 이메일
4. Scopes 추가:
   - `userinfo.email`
   - `userinfo.profile`
   - `openid`
5. Test users 추가 (개발 중에는 필수)

### 1.3 OAuth 2.0 Client ID 생성
1. 좌측 메뉴에서 **APIs & Services > Credentials** 선택
2. **+ CREATE CREDENTIALS > OAuth client ID** 클릭
3. Application type: **Web application** 선택
4. 정보 입력:
   - Name: `Signly Web Client`
   - Authorized JavaScript origins:
     - 개발: `http://localhost:8080`
     - 운영: `https://yourdomain.com`
   - Authorized redirect URIs:
     - 개발: `http://localhost:8080/login/oauth2/code/google`
     - 운영: `https://yourdomain.com/login/oauth2/code/google`
5. **CREATE** 클릭
6. **Client ID**와 **Client Secret** 복사 (안전하게 보관)

## 2. 애플리케이션 설정

### 2.1 개발 환경 (로컬)

#### 방법 1: 환경 변수 설정
```bash
export GOOGLE_CLIENT_ID="your-client-id"
export GOOGLE_CLIENT_SECRET="your-client-secret"
```

#### 방법 2: application-dev.yml 직접 수정 (권장하지 않음)
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: "your-client-id"
            client-secret: "your-client-secret"
```

### 2.2 운영 환경 (.env 파일)

`.env` 파일에 다음 환경 변수 추가:
```bash
GOOGLE_CLIENT_ID=your-production-client-id
GOOGLE_CLIENT_SECRET=your-production-client-secret
```

### 2.3 GitHub Secrets 설정 (CD 자동 배포)

1. GitHub 저장소 > **Settings > Secrets and variables > Actions** 이동
2. **New repository secret** 클릭하여 추가:
   - Name: `GOOGLE_CLIENT_ID`, Value: 운영 Client ID
   - Name: `GOOGLE_CLIENT_SECRET`, Value: 운영 Client Secret

## 3. OAuth2 로그인 플로우

### 3.1 사용자 관점
1. 로그인 페이지에서 **"Google로 계속하기"** 버튼 클릭
2. Google 로그인 페이지로 리다이렉트
3. Google 계정으로 로그인 (기존 로그인 세션이 있으면 생략)
4. Signly 앱 권한 동의 (최초 1회)
5. Signly 홈 페이지로 리다이렉트 (JWT 토큰 자동 저장)

### 3.2 시스템 동작
1. Spring Security가 `/oauth2/authorization/google`로 리다이렉트
2. Google 인증 서버에서 인증 후 `/login/oauth2/code/google`로 콜백
3. `CustomOAuth2UserService`가 Google 사용자 정보 처리:
   - 기존 사용자: 로그인
   - 신규 사용자: 자동 회원가입 (ACTIVE 상태, 이메일 인증 완료)
4. `OAuth2AuthenticationSuccessHandler`가 JWT 토큰 생성 및 Redis 저장
5. 홈 페이지로 리다이렉트 (토큰은 쿼리 파라미터로 전달)
6. 클라이언트 JavaScript가 토큰을 localStorage에 저장

## 4. 보안 고려사항

### 4.1 Client Secret 관리
- ✅ 환경 변수로 관리 (GitHub Secrets, .env)
- ❌ 소스 코드에 직접 하드코딩 금지
- ❌ Git에 커밋 금지 (.gitignore에 .env 추가)

### 4.2 Redirect URI 검증
- Google Console에 등록된 URI만 허용
- 프로토콜, 도메인, 포트, 경로 모두 정확히 일치해야 함
- HTTPS 사용 권장 (운영 환경)

### 4.3 이메일 인증 상태
- Google에서 `email_verified: true`인 사용자만 허용
- 인증되지 않은 이메일은 차단

## 5. 문제 해결

### 5.1 "redirect_uri_mismatch" 오류
- **원인**: Google Console에 등록된 Redirect URI와 불일치
- **해결**: Google Console > Credentials에서 정확한 URI 등록
  - 개발: `http://localhost:8080/login/oauth2/code/google`
  - 운영: `https://yourdomain.com/login/oauth2/code/google`

### 5.2 "Access blocked: This app's request is invalid"
- **원인**: OAuth 동의 화면 설정 미완료 또는 Scopes 누락
- **해결**: OAuth consent screen에서 `userinfo.email`, `userinfo.profile`, `openid` 추가

### 5.3 "unauthorized_client" 오류
- **원인**: Client ID 또는 Client Secret 불일치
- **해결**: 환경 변수 및 Google Console 정보 재확인

### 5.4 개발 중 "This app isn't verified" 경고
- **원인**: 앱이 Google의 검증을 받지 않음 (정상)
- **해결**: "Advanced > Go to Signly (unsafe)" 클릭하여 계속 진행
  - 또는 OAuth consent screen에서 Test users에 본인 이메일 추가

## 6. 테스트

### 6.1 로컬 테스트
1. 환경 변수 설정
2. 애플리케이션 실행: `./gradlew bootRun`
3. 브라우저에서 `http://localhost:8080/login` 접속
4. "Google로 계속하기" 버튼 클릭
5. Google 로그인 및 권한 동의
6. 홈 페이지로 리다이렉트 확인

### 6.2 운영 테스트
1. GitHub Secrets 설정 확인
2. CD 파이프라인으로 배포
3. `https://yourdomain.com/login` 접속
4. OAuth2 로그인 플로우 테스트

## 7. 참고 자료
- [Google OAuth 2.0 문서](https://developers.google.com/identity/protocols/oauth2)
- [Spring Security OAuth2 로그인](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/core.html)
- [Signly 배포 가이드](../DEPLOY.md)
