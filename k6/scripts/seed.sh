#!/bin/bash
# k6 부하 테스트용 데이터 시딩 스크립트
# 사용법: ./k6/scripts/seed.sh
# 결과: k6/scripts/seed-data.json 에 테스트 데이터 저장

set -e

BASE_URL="${BASE_URL:-http://localhost:8080}"
MYSQL_CMD="docker exec stayhost-mysql mysql -ustayhost -pstayhost1234 stayhost -e"

echo "=== k6 테스트 데이터 시딩 시작 ==="

# ── 1. 파트너 등록 + ACTIVE 전환 ──

PARTNER_LOGIN="k6_partner_$(date +%s)"
PARTNER_PASSWORD="password1234"
BIZ_NUMBER="$(date +%s)"

echo "[1/8] 파트너 등록: ${PARTNER_LOGIN}"
PARTNER_RES=$(curl -s -X POST "${BASE_URL}/api/extranet/partners" \
  -H "Content-Type: application/json" \
  -d "{
    \"businessName\": \"k6 테스트 업체\",
    \"businessNumber\": \"${BIZ_NUMBER}\",
    \"representative\": \"테스트대표\",
    \"phone\": \"02-1234-5678\",
    \"email\": \"${PARTNER_LOGIN}@test.com\",
    \"bankName\": \"국민은행\",
    \"bankAccount\": \"123456-78-901234\",
    \"loginId\": \"${PARTNER_LOGIN}\",
    \"password\": \"${PARTNER_PASSWORD}\",
    \"adminName\": \"관리자\"
  }")

PARTNER_ID=$(echo "$PARTNER_RES" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['partnerId'])")
echo "  파트너 ID: ${PARTNER_ID}"

echo "[2/8] 파트너 ACTIVE 전환 (DB)"
$MYSQL_CMD "UPDATE partner SET status = 'ACTIVE' WHERE id = ${PARTNER_ID};"

# ── 2. 파트너 로그인 ──

echo "[3/8] 파트너 로그인"
LOGIN_RES=$(curl -s -X POST "${BASE_URL}/api/public/extranet/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"loginId\": \"${PARTNER_LOGIN}\", \"password\": \"${PARTNER_PASSWORD}\"}")

PARTNER_TOKEN=$(echo "$LOGIN_RES" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['accessToken'])")

# ── 3. 숙소 5개 생성 + 활성화 ──

START_DATE=$(date -v+1d +%Y-%m-%d)
END_DATE=$(date -v+30d +%Y-%m-%d)

PROPERTY_IDS=()
ROOM_TYPE_IDS=()

echo "[4/8] 숙소 5개 생성"
for i in $(seq 1 5); do
  PROP_RES=$(curl -s -X POST "${BASE_URL}/api/extranet/properties" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${PARTNER_TOKEN}" \
    -d "{
      \"name\": \"k6 테스트 호텔 ${i}\",
      \"type\": \"HOTEL\",
      \"description\": \"k6 부하 테스트용 숙소 ${i}\",
      \"address\": \"서울특별시 강남구 테헤란로 ${i}${i}${i}\",
      \"region\": \"서울\",
      \"latitude\": 37.4979,
      \"longitude\": 127.0276,
      \"checkInTime\": \"15:00\",
      \"checkOutTime\": \"11:00\",
      \"thumbnailUrl\": \"https://cdn.example.com/thumb${i}.jpg\"
    }")

  PROP_ID=$(echo "$PROP_RES" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['id'])")
  PROPERTY_IDS+=("$PROP_ID")

  # 활성화
  curl -s -X PATCH "${BASE_URL}/api/extranet/properties/${PROP_ID}/status" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${PARTNER_TOKEN}" \
    -d '{"status": "ACTIVE"}' > /dev/null

  echo "  숙소 ${i}: ID=${PROP_ID} (ACTIVE)"
done

# ── 4. 각 숙소에 객실 2개 + 요금/재고 설정 ──

echo "[5/8] 객실 유형 생성 (숙소당 2개)"
for PROP_ID in "${PROPERTY_IDS[@]}"; do
  for j in 1 2; do
    if [ "$j" -eq 1 ]; then
      RT_NAME="스탠다드"
      RT_PRICE=100000
    else
      RT_NAME="디럭스"
      RT_PRICE=150000
    fi

    RT_RES=$(curl -s -X POST "${BASE_URL}/api/extranet/properties/${PROP_ID}/room-types" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer ${PARTNER_TOKEN}" \
      -d "{
        \"name\": \"${RT_NAME}\",
        \"description\": \"k6 테스트 ${RT_NAME} 객실\",
        \"maxOccupancy\": 4,
        \"basePrice\": ${RT_PRICE},
        \"amenities\": [\"WiFi\", \"TV\", \"에어컨\"],
        \"totalRoomCount\": 20
      }")

    RT_ID=$(echo "$RT_RES" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['id'])")
    ROOM_TYPE_IDS+=("$RT_ID")
    echo "  숙소 ${PROP_ID} - ${RT_NAME}: roomTypeId=${RT_ID}"
  done
done

echo "[6/8] 요금 설정 (${START_DATE} ~ ${END_DATE})"
IDX=0
for RT_ID in "${ROOM_TYPE_IDS[@]}"; do
  if [ $((IDX % 2)) -eq 0 ]; then
    PRICE=100000
  else
    PRICE=150000
  fi
  curl -s -X PUT "${BASE_URL}/api/extranet/room-types/${RT_ID}/rates" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${PARTNER_TOKEN}" \
    -d "{\"startDate\": \"${START_DATE}\", \"endDate\": \"${END_DATE}\", \"price\": ${PRICE}}" > /dev/null
  IDX=$((IDX + 1))
done

echo "[7/8] 재고 설정 (각 20개)"
for RT_ID in "${ROOM_TYPE_IDS[@]}"; do
  curl -s -X PUT "${BASE_URL}/api/extranet/room-types/${RT_ID}/inventory" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${PARTNER_TOKEN}" \
    -d "{\"startDate\": \"${START_DATE}\", \"endDate\": \"${END_DATE}\", \"totalCount\": 20}" > /dev/null
done

# ── 5. 고객 계정 생성 ──

USER_EMAIL="k6_user_$(date +%s)@test.com"
USER_PASSWORD="password1234"

echo "[8/8] 고객 계정 생성: ${USER_EMAIL}"
curl -s -X POST "${BASE_URL}/api/public/users/signup" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"${USER_EMAIL}\",
    \"password\": \"${USER_PASSWORD}\",
    \"name\": \"k6테스트유저\",
    \"phone\": \"010-9999-8888\"
  }" > /dev/null

USER_LOGIN_RES=$(curl -s -X POST "${BASE_URL}/api/public/users/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\": \"${USER_EMAIL}\", \"password\": \"${USER_PASSWORD}\"}")

USER_TOKEN=$(echo "$USER_LOGIN_RES" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['accessToken'])")

# ── 결과 저장 ──

PROP_IDS_JSON=$(printf '%s\n' "${PROPERTY_IDS[@]}" | python3 -c "import sys,json; print(json.dumps([int(l.strip()) for l in sys.stdin]))")
RT_IDS_JSON=$(printf '%s\n' "${ROOM_TYPE_IDS[@]}" | python3 -c "import sys,json; print(json.dumps([int(l.strip()) for l in sys.stdin]))")

OUTPUT_FILE="$(dirname "$0")/seed-data.json"
cat > "$OUTPUT_FILE" <<EOF
{
  "partnerToken": "${PARTNER_TOKEN}",
  "userEmail": "${USER_EMAIL}",
  "userPassword": "${USER_PASSWORD}",
  "userToken": "${USER_TOKEN}",
  "propertyIds": ${PROP_IDS_JSON},
  "roomTypeIds": ${RT_IDS_JSON},
  "startDate": "${START_DATE}",
  "endDate": "${END_DATE}"
}
EOF

echo ""
echo "=== 시딩 완료 ==="
echo "  숙소: ${#PROPERTY_IDS[@]}개"
echo "  객실 유형: ${#ROOM_TYPE_IDS[@]}개"
echo "  요금/재고: ${START_DATE} ~ ${END_DATE}"
echo "  결과 파일: ${OUTPUT_FILE}"
