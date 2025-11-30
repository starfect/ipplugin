# IPPlugin

Minecraft Paper 1.21 용 IP 정보 조회 및 IP/국가 차단 플러그인입니다. `ip-api.com` 혹은 `ipinfo.io` API를 선택해 사용할 수 있습니다.

## 설치
1. `config.yml`, `banlist.yml`, `userlist.yml` 가 없다면 플러그인이 기본값을 생성합니다.
2. `config.yml` 의 `api` 섹션을 설정합니다.  
   - `api: a` -> ip-api.com (API Key 사용 시 `api-token` 입력)  
   - `api: b` -> ipinfo.io (`api-token` 입력)  
3. 필요 시 `custom` 섹션으로 `/ip` 하위 명령어 이름을 변경할 수 있습니다.
4. 서버 `plugins` 폴더에 JAR를 넣고 서버를 재시작 또는 리로드합니다.

## 주요 설정 (`config.yml`)
- `country-ban`: true/false 국가 차단 기능 사용 여부
- `user-ban`: true/false IP/유저 차단 기능 사용 여부
- `permission`: OP 또는 User  
  - `OP`: 관리자 기능을 OP만 사용  
  - `User`: 권한 노드 사용 (`ipplugin.user`, `ipplugin.admin`)
- `custom.command-help`: `/ip` 도움말 하위 명령어 이름
- `custom.command-ip-check`: `/ip` 자기/타인 IP 조회 하위 명령어 이름
- `custom.command-all`: `/ip` GUI 하위 명령어 이름

## 명령어
- `/ip` , `/ip <helpAlias>`: 도움말
- `/ip <checkAlias>`: 자신의 IP 정보 조회
- `/ip <checkAlias> <username>`: 대상 IP 정보 조회 (관리자)
- `/ip ban <username|IP|CountryCode>`: IP/유저 혹은 국가 차단 (관리자)
- `/ip pardon <username|IP|CountryCode>`: 차단 해제 (관리자)
- `/ip status`: API/설정 상태 확인
- `/ip banlist`: 차단 목록 확인
- `/ip <allAlias>`: 온라인 플레이어 GUI(머리 아이템 좌클릭 IP 밴/해제, 우클릭 퇴장) (관리자)

## 동작
- 플레이어 접속 시 `userlist.yml` 에 방문 기록 저장.
- `banlist.yml` 의 `ban-user` 에는 IP 또는 유저명이 저장되며, `ban-country` 에는 국가 코드(대문자)가 저장됩니다.
- 접속 시 국가/유저/IP가 차단 목록에 있으면 즉시 킥됩니다.

## 빌드
Java 21, Maven 사용:
```
mvn clean package
```
완성된 JAR는 `target/ipplugin-1.0.0-SNAPSHOT.jar` 에 생성됩니다.

## 라이선스 및 주의사항 (GNU GPL v3)
- 이 프로젝트는 `LICENSE`의 GNU GPL 3.0을 따릅니다. 플러그인 배포 시 **동일 라이선스**로 배포해야 합니다.
- 바이너리(JAR)를 배포하거나 수정본을 배포할 경우, **전체 소스코드와 라이선스 사본(GPLv3)**을 함께 제공해야 합니다.
- 코드 수정 시 변경 사실과 날짜를 명시하고, 원저작자 표기를 유지해야 합니다.
- GPL 호환이 아닌 추가적인 사용 제한을 부가할 수 없습니다.
- 공개 배포본을 업데이트할 때는 변경사항을 사용자에게 고지하고, 새로운 소스/라이선스 사본을 함께 배포하세요.
