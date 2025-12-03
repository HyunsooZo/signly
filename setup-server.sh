#!/bin/bash
# =================================
# 오라클 서버 자동 설정 스크립트
# =================================

set -e  # 에러 시 중단

# 색상 코드
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}==================================${NC}"
echo -e "${GREEN}오라클 서버 자동 설정 스크립트${NC}"
echo -e "${GREEN}==================================${NC}"
echo ""

# 서버 정보
SERVER_IP="134.185.107.181"
SSH_KEY="/Users/hyunsoojo/Downloads/ssh-key-2025-12-01.key"
SSH_USER="ubuntu"  # ubuntu 또는 opc

echo -e "${YELLOW}1. SSH Key 권한 설정...${NC}"
chmod 400 "$SSH_KEY"
echo -e "${GREEN}✓ SSH Key 권한 설정 완료${NC}"
echo ""

echo -e "${YELLOW}2. SSH 접속 테스트...${NC}"
ssh -o StrictHostKeyChecking=no -o ConnectTimeout=5 -i "$SSH_KEY" "$SSH_USER@$SERVER_IP" "echo '✓ SSH 접속 성공!'" || {
    echo -e "${RED}✗ ubuntu 계정으로 접속 실패. opc 계정으로 재시도...${NC}"
    SSH_USER="opc"
    ssh -o StrictHostKeyChecking=no -o ConnectTimeout=5 -i "$SSH_KEY" "$SSH_USER@$SERVER_IP" "echo '✓ SSH 접속 성공!'" || {
        echo -e "${RED}✗ SSH 접속 실패!${NC}"
        echo -e "${RED}오라클 콘솔에서 SSH Key를 다시 추가해주세요.${NC}"
        exit 1
    }
}
echo -e "${GREEN}✓ 사용자: $SSH_USER${NC}"
echo ""

echo -e "${YELLOW}3. 서버에 Docker 설치...${NC}"
ssh -i "$SSH_KEY" "$SSH_USER@$SERVER_IP" << 'ENDSSH'
    # Docker 설치 확인
    if command -v docker &> /dev/null; then
        echo "✓ Docker가 이미 설치되어 있습니다."
    else
        echo "Docker 설치 중..."
        curl -fsSL https://get.docker.com -o get-docker.sh
        sudo sh get-docker.sh
        sudo usermod -aG docker $USER
        echo "✓ Docker 설치 완료"
    fi
    
    # Docker Compose 설치 확인
    if command -v docker-compose &> /dev/null; then
        echo "✓ Docker Compose가 이미 설치되어 있습니다."
    else
        echo "Docker Compose 설치 중..."
        sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
        sudo chmod +x /usr/local/bin/docker-compose
        echo "✓ Docker Compose 설치 완료"
    fi
ENDSSH
echo -e "${GREEN}✓ Docker 설치 완료${NC}"
echo ""

echo -e "${YELLOW}4. 서버 방화벽 설정 (포트 8080)...${NC}"
ssh -i "$SSH_KEY" "$SSH_USER@$SERVER_IP" << 'ENDSSH'
    echo "방화벽 규칙 추가 중..."
    
    # iptables 규칙 추가
    sudo iptables -C INPUT -p tcp --dport 8080 -j ACCEPT 2>/dev/null || \
    sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 8080 -j ACCEPT
    
    # netfilter-persistent 설치 및 저장
    if ! command -v netfilter-persistent &> /dev/null; then
        sudo apt-get update -qq
        sudo DEBIAN_FRONTEND=noninteractive apt-get install -y iptables-persistent
    fi
    sudo netfilter-persistent save
    
    echo "✓ 방화벽 규칙 추가 완료"
ENDSSH
echo -e "${GREEN}✓ 방화벽 설정 완료${NC}"
echo ""

echo -e "${YELLOW}5. 배포 디렉토리 생성...${NC}"
ssh -i "$SSH_KEY" "$SSH_USER@$SERVER_IP" << 'ENDSSH'
    sudo mkdir -p /opt/signly
    sudo chown $USER:$USER /opt/signly
    echo "✓ /opt/signly 디렉토리 생성 완료"
ENDSSH
echo -e "${GREEN}✓ 배포 디렉토리 생성 완료${NC}"
echo ""

echo -e "${YELLOW}6. 서버에 필요한 파일 업로드...${NC}"
scp -i "$SSH_KEY" docker-compose.prod.yml "$SSH_USER@$SERVER_IP:/opt/signly/"
scp -i "$SSH_KEY" .env.example "$SSH_USER@$SERVER_IP:/opt/signly/.env"
echo -e "${GREEN}✓ 파일 업로드 완료${NC}"
echo ""

echo -e "${GREEN}==================================${NC}"
echo -e "${GREEN}✓ 서버 설정 완료!${NC}"
echo -e "${GREEN}==================================${NC}"
echo ""
echo -e "${YELLOW}다음 단계:${NC}"
echo "1. 서버에 접속해서 .env 파일 수정:"
echo "   ssh -i $SSH_KEY $SSH_USER@$SERVER_IP"
echo "   cd /opt/signly"
echo "   nano .env"
echo ""
echo "2. GitHub Secrets 등록:"
echo "   DOCKER_USERNAME: (Docker Hub ID)"
echo "   DOCKER_PASSWORD: (Docker Hub Token)"
echo "   DEPLOY_HOST: $SERVER_IP"
echo "   DEPLOY_USER: $SSH_USER"
echo "   DEPLOY_SSH_KEY: (아래 명령어로 복사)"
echo ""
echo "   SSH Private Key 복사:"
echo "   cat $SSH_KEY | pbcopy"
echo ""
echo -e "${GREEN}준비 완료! 🚀${NC}"
