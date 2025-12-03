#!/bin/bash

# 색상 코드
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

SERVER_IP="134.185.107.181"
SSH_KEY="/Users/hyunsoojo/Downloads/oracleKey/ssh-key-2025-12-01.key"
SSH_USER="ubuntu"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Signly 최종 배포 스크립트${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# DNS 확인
echo -e "${YELLOW}[1/5] DNS 확인 중...${NC}"
dns_check=$(nslookup signly.kr 8.8.8.8 | grep "Address" | tail -1 | awk '{print $2}')

if [ "$dns_check" == "134.185.107.181" ]; then
    echo -e "${GREEN}✅ DNS 설정 확인됨: signly.kr → 134.185.107.181${NC}"
else
    echo -e "${RED}❌ DNS가 아직 전파되지 않았습니다.${NC}"
    echo -e "${YELLOW}현재: $dns_check${NC}"
    echo -e "${YELLOW}기대: 134.185.107.181${NC}"
    echo ""
    echo -e "${YELLOW}도메인 등록 업체에서 A 레코드를 설정하고${NC}"
    echo -e "${YELLOW}1-2시간 후 다시 시도하세요.${NC}"
    exit 1
fi

# 서버에 접속하여 SSL 인증서 발급 및 서비스 시작
echo ""
echo -e "${GREEN}[2/5] SSL 인증서 발급 중...${NC}"
echo -e "${YELLOW}Let's Encrypt 인증서를 발급합니다. 약 1-2분 소요됩니다.${NC}"
echo ""

ssh -i "$SSH_KEY" -t "$SSH_USER@$SERVER_IP" << 'ENDSSH'
cd /opt/signly

echo "========================================="
echo "SSL 인증서 발급 시작"
echo "========================================="
echo ""

# init-letsencrypt.sh 실행
sudo ./init-letsencrypt.sh

echo ""
echo "========================================="
echo "SSL 인증서 발급 완료"
echo "========================================="
ENDSSH

echo ""
echo -e "${GREEN}[3/5] 서비스 시작 중...${NC}"

ssh -i "$SSH_KEY" "$SSH_USER@$SERVER_IP" << 'ENDSSH'
cd /opt/signly

echo "Docker Compose로 서비스 시작..."
sudo docker-compose -f docker-compose.prod.yml up -d

echo ""
echo "서비스 상태 확인..."
sleep 5
sudo docker-compose -f docker-compose.prod.yml ps
ENDSSH

echo ""
echo -e "${GREEN}[4/5] 서비스 헬스체크...${NC}"
sleep 10

# 헬스체크
if curl -f -s https://signly.kr/actuator/health > /dev/null; then
    echo -e "${GREEN}✅ 서비스 정상 동작 중!${NC}"
else
    echo -e "${YELLOW}⚠️  서비스가 아직 시작 중입니다. 잠시 후 다시 확인하세요.${NC}"
fi

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}✅ 배포 완료!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${GREEN}접속 URL:${NC}"
echo "  - HTTP:  http://signly.kr (→ HTTPS 자동 리다이렉트)"
echo "  - HTTPS: https://signly.kr"
echo ""
echo -e "${YELLOW}로그 확인:${NC}"
echo "  ssh -i $SSH_KEY $SSH_USER@$SERVER_IP"
echo "  cd /opt/signly"
echo "  sudo docker-compose -f docker-compose.prod.yml logs -f"
echo ""
echo -e "${YELLOW}서비스 재시작:${NC}"
echo "  sudo docker-compose -f docker-compose.prod.yml restart"
echo ""
echo -e "${YELLOW}서비스 중지:${NC}"
echo "  sudo docker-compose -f docker-compose.prod.yml down"
echo ""
