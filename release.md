# IPPlugin 릴리스 노트

## 주요 기능
- `/ip` 기반 IP 정보 조회 (ip-api.com 또는 ipinfo.io 선택)
- IP/유저/국가 밴 및 해제, 상태/밴목록 확인
- 온라인 플레이어 GUI로 IP 밴/해제 및 퇴장 처리
- 접속 시 국가/유저/IP 차단 확인

## 설정/빌드
- `config.yml`에서 API 선택/토큰, 권한 모드(OP/User), 커맨드 별칭 설정
- Java 21, Maven 빌드: `mvn clean package`

## 변경 사항
- Adventure `Component` 기반 킥 메시지 사용
- 한글 안내 메시지 정리 및 릴리스 업로드 워크플로 개선

## 라이선스
- GNU GPL v3 (LICENSE 참조)
