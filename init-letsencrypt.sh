#!/bin/bash

# 설정
domains=(signly.kr www.signly.kr)
email="bzhs1992@icloud.com"
staging=0  # 테스트는 1, 실제는 0
data_path="./certbot"
compose_file="docker-compose.prod.yml"

# 색상 코드
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Let's Encrypt SSL 인증서 초기화 시작${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# Root 권한 체크
if [ "$EUID" -ne 0 ]; then 
  echo -e "${YELLOW}Warning: Root 권한이 필요할 수 있습니다.${NC}"
fi

# 기존 데이터 확인
if [ -d "$data_path" ]; then
  read -p "기존 데이터를 발견했습니다. 삭제하고 다시 시작하시겠습니까? (y/N) " decision
  if [ "$decision" != "Y" ] && [ "$decision" != "y" ]; then
    echo -e "${YELLOW}작업을 취소했습니다.${NC}"
    exit
  fi
fi

# 디렉토리 생성
echo -e "${GREEN}[1/6] 디렉토리 구조 생성 중...${NC}"
mkdir -p "$data_path/conf"
mkdir -p "$data_path/www"

# TLS 파라미터 다운로드
if [ ! -e "$data_path/conf/options-ssl-nginx.conf" ] || [ ! -e "$data_path/conf/ssl-dhparams.pem" ]; then
  echo -e "${GREEN}[2/6] TLS 파라미터 다운로드 중...${NC}"
  curl -s https://raw.githubusercontent.com/certbot/certbot/master/certbot-nginx/certbot_nginx/_internal/tls_configs/options-ssl-nginx.conf > "$data_path/conf/options-ssl-nginx.conf"
  curl -s https://raw.githubusercontent.com/certbot/certbot/master/certbot/certbot/ssl-dhparams.pem > "$data_path/conf/ssl-dhparams.pem"
  echo -e "${GREEN}TLS 파라미터 다운로드 완료${NC}"
fi

# 임시 인증서 생성
echo -e "${GREEN}[3/6] 임시 SSL 인증서 생성 중...${NC}"
path="/etc/letsencrypt/live/${domains[0]}"
mkdir -p "$data_path/conf/live/${domains[0]}"
docker-compose -f $compose_file run --rm --entrypoint "\
  openssl req -x509 -nodes -newkey rsa:4096 -days 1\
    -keyout '$path/privkey.pem' \
    -out '$path/fullchain.pem' \
    -subj '/CN=localhost'" certbot
echo -e "${GREEN}임시 인증서 생성 완료${NC}"

# Nginx 시작
echo -e "${GREEN}[4/6] Nginx 컨테이너 시작 중...${NC}"
docker-compose -f $compose_file up --force-recreate -d nginx
echo -e "${GREEN}Nginx 시작 완료${NC}"

# 임시 인증서 삭제
echo -e "${GREEN}[5/6] 임시 인증서 삭제 중...${NC}"
docker-compose -f $compose_file run --rm --entrypoint "\
  rm -Rf /etc/letsencrypt/live/${domains[0]} && \
  rm -Rf /etc/letsencrypt/archive/${domains[0]} && \
  rm -Rf /etc/letsencrypt/renewal/${domains[0]}.conf" certbot
echo -e "${GREEN}임시 인증서 삭제 완료${NC}"

# Let's Encrypt 인증서 발급
echo -e "${GREEN}[6/6] Let's Encrypt 인증서 발급 중...${NC}"
echo -e "${YELLOW}도메인: ${domains[@]}${NC}"
echo -e "${YELLOW}이메일: $email${NC}"

# Staging 모드 확인
if [ $staging != "0" ]; then
  staging_arg="--staging"
  echo -e "${YELLOW}테스트 모드 (Staging)${NC}"
else
  staging_arg=""
  echo -e "${GREEN}프로덕션 모드${NC}"
fi

# Certbot 실행
docker-compose -f $compose_file run --rm --entrypoint "\
  certbot certonly --webroot -w /var/www/certbot \
    $staging_arg \
    --email $email \
    --agree-tos \
    --no-eff-email \
    --force-renewal \
    $(printf -- "-d %s " "${domains[@]}")" certbot

# 결과 확인
if [ $? -eq 0 ]; then
  echo -e "${GREEN}========================================${NC}"
  echo -e "${GREEN}SSL 인증서 발급 완료!${NC}"
  echo -e "${GREEN}========================================${NC}"
  
  # Nginx 재로드
  echo -e "${GREEN}Nginx 재시작 중...${NC}"
  docker-compose -f $compose_file exec nginx nginx -s reload
  
  echo ""
  echo -e "${GREEN}✅ 모든 작업 완료!${NC}"
  echo -e "${GREEN}HTTPS 접속: https://signly.kr${NC}"
  echo ""
  echo -e "${YELLOW}인증서 갱신은 자동으로 진행됩니다. (12시간마다 체크)${NC}"
else
  echo -e "${RED}========================================${NC}"
  echo -e "${RED}SSL 인증서 발급 실패${NC}"
  echo -e "${RED}========================================${NC}"
  echo ""
  echo -e "${YELLOW}다음 사항을 확인하세요:${NC}"
  echo "1. 도메인 DNS가 올바른 IP(134.185.107.181)를 가리키는지 확인"
  echo "2. 방화벽 포트 80, 443이 열려있는지 확인"
  echo "3. Docker와 docker-compose가 실행 중인지 확인"
  exit 1
fi
