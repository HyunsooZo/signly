#!/bin/bash

# 색상 코드
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Signly SSL 서버 설정 스크립트${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# SSH 키 권한 설정
echo -e "${YELLOW}[1/8] SSH 키 권한 설정...${NC}"
chmod 400 /Users/hyunsoojo/Downloads/oracleKey/ssh-key-2025-12-01.key

# 서버 접속 정보
SERVER_IP="134.185.107.181"
SSH_KEY="/Users/hyunsoojo/Downloads/oracleKey/ssh-key-2025-12-01.key"
SSH_USER="ubuntu"

echo -e "${GREEN}[2/8] 서버 방화벽 설정 중...${NC}"
ssh -i "$SSH_KEY" -o StrictHostKeyChecking=no "$SSH_USER@$SERVER_IP" << 'ENDSSH'
# 방화벽 규칙 추가
echo "포트 80, 443 열기..."
sudo iptables -I INPUT 6 -p tcp --dport 80 -j ACCEPT
sudo iptables -I INPUT 6 -p tcp --dport 443 -j ACCEPT

# iptables-persistent 설치 (자동 yes)
echo "iptables-persistent 설치..."
export DEBIAN_FRONTEND=noninteractive
sudo apt-get update -qq
sudo apt-get install -y iptables-persistent

# 규칙 저장
echo "방화벽 규칙 저장..."
sudo netfilter-persistent save

# 확인
echo ""
echo "=== 방화벽 규칙 확인 ==="
sudo iptables -L -n -v | grep -E 'dpt:(80|443)'
echo ""
ENDSSH

echo -e "${GREEN}[3/8] Docker 설치 확인...${NC}"
ssh -i "$SSH_KEY" "$SSH_USER@$SERVER_IP" << 'ENDSSH'
# Docker 설치 확인
if ! command -v docker &> /dev/null; then
    echo "Docker 설치 중..."
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
    sudo usermod -aG docker $USER
    rm get-docker.sh
fi

# Docker Compose 설치 확인
if ! command -v docker-compose &> /dev/null; then
    echo "Docker Compose 설치 중..."
    sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
fi

echo "Docker 버전:"
docker --version
docker-compose --version
ENDSSH

echo -e "${GREEN}[4/8] 프로젝트 디렉토리 생성...${NC}"
ssh -i "$SSH_KEY" "$SSH_USER@$SERVER_IP" << 'ENDSSH'
# 프로젝트 디렉토리 생성
sudo mkdir -p /opt/signly
sudo chown -R $USER:$USER /opt/signly
cd /opt/signly
echo "프로젝트 디렉토리: /opt/signly"
ENDSSH

echo -e "${GREEN}[5/8] Git 리포지토리 클론...${NC}"
ssh -i "$SSH_KEY" "$SSH_USER@$SERVER_IP" << 'ENDSSH'
cd /opt/signly

# 기존 파일 확인
if [ -d ".git" ]; then
    echo "Git 리포지토리 업데이트..."
    git pull origin master
else
    echo "Git 리포지토리 클론..."
    git clone https://github.com/HyunsooZo/signly.git .
fi
ENDSSH

echo -e "${GREEN}[6/8] .env 파일 생성...${NC}"
ssh -i "$SSH_KEY" "$SSH_USER@$SERVER_IP" << 'ENDSSH'
cd /opt/signly

if [ ! -f ".env" ]; then
    echo ".env 파일 생성..."
    cp .env.example .env
    
    # 랜덤 비밀번호 생성
    MYSQL_ROOT_PW=$(openssl rand -base64 32)
    MYSQL_USER_PW=$(openssl rand -base64 32)
    REDIS_PW=$(openssl rand -base64 32)
    JWT_SECRET=$(openssl rand -base64 48)
    
    # .env 파일 업데이트
    sed -i "s/your_secure_root_password_here/$MYSQL_ROOT_PW/" .env
    sed -i "s/your_secure_mysql_password_here/$MYSQL_USER_PW/" .env
    sed -i "s/your_secure_redis_password_here/$REDIS_PW/" .env
    sed -i "s/your_jwt_secret_key_minimum_32_characters_long_random_string/$JWT_SECRET/" .env
    
    echo ""
    echo "=== .env 파일 생성 완료 ==="
    echo "⚠️  이메일 설정은 수동으로 해야 합니다!"
    echo "   nano .env 로 편집하세요."
    echo ""
else
    echo ".env 파일이 이미 존재합니다."
fi
ENDSSH

echo -e "${GREEN}[7/8] 실행 권한 부여...${NC}"
ssh -i "$SSH_KEY" "$SSH_USER@$SERVER_IP" << 'ENDSSH'
cd /opt/signly
chmod +x init-letsencrypt.sh
echo "init-letsencrypt.sh 실행 권한 부여 완료"
ENDSSH

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}✅ 서버 설정 완료!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${YELLOW}다음 단계:${NC}"
echo ""
echo "1. DNS 설정 확인 (1-2시간 소요):"
echo "   nslookup signly.kr"
echo "   nslookup www.signly.kr"
echo ""
echo "2. 서버 접속:"
echo "   ssh -i $SSH_KEY $SSH_USER@$SERVER_IP"
echo ""
echo "3. 이메일 설정 (필수!):"
echo "   cd /opt/signly"
echo "   nano .env"
echo "   # MAIL_USERNAME, MAIL_PASSWORD 수정"
echo ""
echo "4. SSL 인증서 발급:"
echo "   ./init-letsencrypt.sh"
echo ""
echo "5. 서비스 시작:"
echo "   docker-compose -f docker-compose.prod.yml up -d"
echo ""
echo "6. 로그 확인:"
echo "   docker-compose -f docker-compose.prod.yml logs -f"
echo ""
echo "7. 접속:"
echo "   https://signly.kr"
echo ""
