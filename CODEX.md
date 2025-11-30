# IP Plugin 개발 가이드
## 빌드 프로그램
- Maven `mvn` 사용
## 코드 스타일
- **Package**: `me.starfect.ip-plugin.[기능역역]`
- **종속성:**
    - Paper API 1.21.4 사용
    - ** Java 21 사용
- **import**: 모든 임포트 명시적으로 작성, 와일드카드(*) 사용 금지
- **API**: `ip-api.com` 또는 `ipinfo.io` API 사용
    - 사용설정은 `config.yml` 에서 계속
## 명령어와 기능들
- **명령어 목록**:
    - `/ip`, `/ip help`: *사용 가능한 명령어 표시*
    - `/ip check`: 자신의 *IP, TCP 주소* 체크  
    - `/ip check <username>`: 해당 유저의 *ip, TCP 주소* 체크 **{관리자, OP 만 사용가능}**
    - `/ip ban <username or IP Address>`: 해당 유저의 IP 를 강제 추방 **{관리자, OP 만 사용가능}**
    - `/ip ban <Country Code>`: 해당 국가에서 접속 하는것을 차단 **{관리자, OP 만 사용가능}**
    - `/ip pardon <username or IP Address>`: 해당 유저의 IP 의 접속차단을 해제 **{관리자, OP 만 사용가능}**
    - `/ip pardon <country Code>`: 해당 국가의 접속차단을 해제 **{관리자, OP 만 사용가능}**
    - `/ip status`: IP 정보를 제공하는 API 제공업체의 API 활성, 비활성 정보 가져오기
    - `/ip banlist`: 차단한 유저 목록 표시
    - `/ip all`: GUI(ChestGUI)에 유저의 머리를 띄운뒤, 머리 아이템 제목에는 {username} 을 띄우고, 설명에는 접속국가와 제목을 띄움.
    - 머리 아이템을 누르면 밴하거나 나가게 `/kick` 할 수 있음 **{인게임 전용 명령어}**
- **설정 파일들**:
    - `config.yml`:

```yaml
# config.yml
# your api token here

api:
    api: 
    # a = ip-api.com API Key
    # b = ipinfo.io API Key
    # ex. api: a
    api-token:

# config plugin

custom:
    command-help: help
    command-ip-check: check
    command-all: all

country-ban: true

user-ban: true

permission: OP
# permission: OP / User
```


  - `banlist.yml`



```yml
# banlist.yml
# this is a blacklist

ban-user:
    # ex. example: ban
    # ex. username: ban

ban-country:
    # ex. US: ban
    # ex. UK: ban
    # ex. RU: ban
```


 - `userlist.yml`


```yml
# userlist.yml
# visited users

user-list:
    # ex. username
    # ex. Madmovies

```

## *끝*

> 내가 원할때만 Release 로 빌드가 올라갈 수 있는 Github Action YAMl 파일도 추가해줘.

> 한국어 READEME.md 파일도 추가해줘.

    
