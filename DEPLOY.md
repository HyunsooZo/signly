# 🚀 Signly 배포 가이드

## 📋 사전 준비

### 1. 도메인 DNS 설정
- **A 레코드 추가**
  - `signly.kr` → `134.185.107.181`
  - `www.signly.kr` → `134.185.107.181`

### 2. 오라클 클라우드 방화벽 설정

#### 웹 콘솔에서 설정:
1. **Compute > Instances** 이동
2. 인스턴스 클릭
3. **Virtual Cloud Network** 클릭
4. **Security Lists** > **Default Security List** 클릭
5. **Add Ingress Rules** 클릭하여 다음 규칙 추가:

**HTTP (포트 80):**
- Source CIDR: `0.0.0.0/0`
- IP Protocol: `TCP`
- Destination Port Range: `80`
- Description: `Allow HTTP for Let's Encrypt`

**HTTPS (포트 443):**
- Source CIDR: `0.0.0.0/0`
- IP Protocol: `TCP`
- Destination Port Range: `443`
- Description: `Allow HTTPS`

#### 서버 내부 방화벽 설정:
```bash
# iptables 규칙 추가
sudo iptables -I INPUT 6 -p tcp --dport 80 -j ACCEPT
sudo iptables -I INPUT 6 -p tcp --dport 443 -j ACCEPT

# 영구 저장 (Ubuntu)
sudo netfilter-persistent save

# 또는 iptables-persistent 사용
sudo sh -c "iptables-save > /etc/iptables/rules.v4"
```

### 3. 환경 변수 설정
```bash
# .env 파일 생성
cp .env.example .env

# 비밀번호 변경 (필수!)
nano .env
```

**필수 변경 항목:**
- `MYSQL_ROOT_PASSWORD`
- `MYSQL_PASSWORD`
- `REDIS_PASSWORD`
- `JWT_SECRET` (최소 32자 이상)
- `MAIL_USERNAME` / `MAIL_PASSWORD` (Gmail 앱 비밀번호)

---

## 🔐 SSL 인증서 발급

### DNS 설정 확인
```bash
# DNS 전파 확인 (1-2시간 소요 가능)
nslookup signly.kr
nslookup www.signly.kr
# 134.185.107.181 이 나와야 함

# 또는 dig 사용
dig signly.kr +short
dig www.signly.kr +short
```

### 방화벽 확인
```bash
# 포트 리스닝 확인
sudo netstat -tlnp | grep -E ':(80|443)'

# iptables 규칙 확인
sudo iptables -L -n -v | grep -E 'dpt:(80|443)'
```

### SSL 인증서 발급 실행
```bash
# 실행 권한 부여
chmod +x init-letsencrypt.sh

# 스크립트 실행
./init-letsencrypt.sh
```

---

## 🐳 배포 실행

### 1. 전체 서비스 시작
```bash
docker-compose -f docker-compose.prod.yml up -d
```

### 2. 로그 확인
```bash
# 전체 로그
docker-compose -f docker-compose.prod.yml logs -f

# 특정 서비스 로그
docker-compose -f docker-compose.prod.yml logs -f app
docker-compose -f docker-compose.prod.yml logs -f nginx
```

### 3. 상태 확인
```bash
# 컨테이너 상태
docker-compose -f docker-compose.prod.yml ps

# 헬스체크
curl http://localhost:8080/actuator/health
curl https://signly.kr/actuator/health
```

---

## 🔍 트러블슈팅

### SSL 인증서 발급 실패

**원인 1: DNS 미설정**
```bash
# DNS 확인
nslookup signly.kr
# 134.185.107.181 이 나와야 함
```

**원인 2: 방화벽 포트 닫힘**
```bash
# 오라클 클라우드 콘솔에서 Security List 확인
# 포트 80, 443이 열려있어야 함

# 서버 방화벽 확인
sudo iptables -L -n | grep -E 'dpt:(80|443)'
```

**원인 3: Nginx가 실행 중이지 않음**
```bash
# Nginx 상태 확인
docker-compose -f docker-compose.prod.yml ps nginx

# Nginx 재시작
docker-compose -f docker-compose.prod.yml restart nginx
```

### Nginx 오류

**설정 파일 테스트:**
```bash
docker-compose -f docker-compose.prod.yml exec nginx nginx -t
```

**로그 확인:**
```bash
docker-compose -f docker-compose.prod.yml logs nginx
```

### 데이터베이스 연결 실패

**MySQL 상태 확인:**
```bash
docker-compose -f docker-compose.prod.yml ps mysql
docker-compose -f docker-compose.prod.yml logs mysql
```

**연결 테스트:**
```bash
docker-compose -f docker-compose.prod.yml exec mysql mysql -u signly_user -p
```

---

## 🔄 인증서 갱신

Let's Encrypt 인증서는 **자동으로 갱신**됩니다 (12시간마다 체크).

### 수동 갱신:
```bash
# Dry run 테스트
docker-compose -f docker-compose.prod.yml exec certbot certbot renew --dry-run

# 실제 갱신
docker-compose -f docker-compose.prod.yml exec certbot certbot renew

# Nginx 재로드
docker-compose -f docker-compose.prod.yml exec nginx nginx -s reload
```

---

## 🛑 서비스 중지

```bash
# 전체 중지
docker-compose -f docker-compose.prod.yml down

# 볼륨 포함 전체 삭제 (주의!)
docker-compose -f docker-compose.prod.yml down -v
```

---

## 📊 모니터링

### 로그 보기
```bash
# 실시간 로그
docker-compose -f docker-compose.prod.yml logs -f

# 최근 100줄
docker-compose -f docker-compose.prod.yml logs --tail=100
```

### 리소스 사용량
```bash
# 컨테이너 리소스 확인
docker stats

# 디스크 사용량
docker system df
```

---

## 🎯 접속 URL

- **HTTP**: http://signly.kr (자동으로 HTTPS로 리다이렉트)
- **HTTPS**: https://signly.kr
- **헬스체크**: https://signly.kr/actuator/health

---

## 📞 문제 발생 시

1. **로그 확인**: `docker-compose -f docker-compose.prod.yml logs`
2. **DNS 확인**: `nslookup signly.kr`
3. **방화벽 확인**: 오라클 클라우드 콘솔 + 서버 iptables
4. **인증서 확인**: `docker-compose -f docker-compose.prod.yml logs certbot`

---

## 🔐 보안 체크리스트

- [ ] `.env` 파일에 강력한 비밀번호 설정
- [ ] 방화벽에서 불필요한 포트 차단
- [ ] SSL 인증서 정상 발급 확인
- [ ] HTTPS 리다이렉트 동작 확인
- [ ] JWT 시크릿 32자 이상 랜덤 문자열
- [ ] 데이터베이스 외부 접근 제한 (3306 포트)
- [ ] Redis 비밀번호 설정 확인

---

**생성일**: 2025-12-03  
**작성자**: OpenCode & Claude
