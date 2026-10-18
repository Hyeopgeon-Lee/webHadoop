# webHadoop — Spring Boot 4 / WebHDFS 실습 프로젝트

Spring Boot **4.1.1**, Java **17**, Spring MVC, `WebClient`, Gradle 기반의 WebHDFS REST API 실습 예제입니다.
Hadoop의 **WebHDFS HTTP API**를 직접 호출하므로 `hadoop-client` Java 라이브러리는 필요하지 않습니다.

## 주요 기능

- `POST /webhdfs/v1/upload` — 텍스트 파일 업로드 (NameNode HTTP 307 → DataNode PUT → HTTP 201)
- `DELETE /webhdfs/v1/delete?path=file.txt` — 파일 삭제 (응답 `{"boolean":true}` 확인)
- `GET /webhdfs/v1/list` — 파일/디렉터리 목록 조회 (WebHDFS JSON 문자열 반환)
- `http://localhost:11000/webHdfs.html` — 파일 업로드·삭제·조회 실습 화면

## 실행 환경

- JDK 17 이상 (Java 17 툴체인으로 컴파일)
- Gradle 8.14.3 (Wrapper 제공)
- WebHDFS가 활성화된 Hadoop NameNode 및 접근 가능한 DataNode
- 실습용 HDFS 경로 `/01` (사전에 디렉터리 생성 및 권한 설정 필요)

### 1. WebHDFS 서버 연결 설정

`src/main/resources/application.properties`에서 환경변수로 값을 주입합니다.

```properties
server.port=11000
webhdfs.base-url=${WEBHDFS_BASE_URL:http://localhost:9870/webhdfs/v1}
webhdfs.username=${WEBHDFS_USER:hadoop}
webhdfs.upload-dir=${WEBHDFS_UPLOAD_DIR:/01}
```

예시 (Linux/macOS):

```bash
export WEBHDFS_BASE_URL="http://192.168.133.131:9870/webhdfs/v1"
export WEBHDFS_USER="hadoop"
export WEBHDFS_UPLOAD_DIR="/01"
```

**주의:** 예시 IP와 계정은 각 실습 환경에 맞게 변경해야 합니다. NameNode뿐 아니라 리다이렉트된 DataNode 주소에도 애플리케이션 서버에서 접근할 수 있어야 합니다.

### 2. 빌드 / 자동화 테스트

```bash
./gradlew clean test bootJar
./gradlew bootRun
```

Windows: `gradlew.bat clean test bootJar`, `gradlew.bat bootRun`

GitHub Actions (`.github/workflows/gradle-build.yml`)에서도 컴파일, 단위 테스트, JAR 패키징을 자동 실행합니다. 단위 테스트는 가짜 WebHDFS HTTP 응답을 사용하므로 실제 Hadoop 없이 실행할 수 있습니다.

### 3. 웹 화면

```text
http://localhost:11000/webHdfs.html
```

### 4. API 확인

```bash
# 목록
curl "http://localhost:11000/webhdfs/v1/list"

# /01/hello.txt 업로드
curl -X POST "http://localhost:11000/webhdfs/v1/upload" \
  --data-urlencode "path=hello.txt" \
  --data-urlencode "content=Hello, WebHDFS!"

# 삭제 (DELETE 경로는 쿼리 파라미터)
curl -X DELETE "http://localhost:11000/webhdfs/v1/delete?path=hello.txt"
```

## 구현·검증 포인트

1. NameNode에는 **본문이 없는 PUT**으로 `op=CREATE` 요청 후 HTTP 307 `Location`을 확인합니다.
2. DataNode에 **UTF-8 바이트**를 전송하고 HTTP 201을 확인합니다. 성공 시 응답 본문이 없어도 `data="true"`를 반환합니다.
3. Hadoop의 삭제 응답 JSON에서 `boolean=true`인지 확인합니다.
4. 잘못된 상대 경로(`../`, 절대 경로, 빈 파일명)는 HTTP 400으로 거부합니다.
5. WebHDFS 연결 오류는 HTTP 502로 구분하며 내부 Hadoop 주소를 브라우저 응답에 노출하지 않습니다.
6. 브라우저는 파일명을 HTML에 삽입하지 않고 DOM 텍스트로 처리합니다.

## 보안 및 운영 참고

이 코드는 **수업용**입니다. 기본 인증/인가가 없으므로 인터넷에 직접 공개하지 말고 신뢰할 수 있는 교육용 내부망에서 사용하세요. 실제 운영 시에는 사용자 인증과 HDFS 권한 분리, HTTPS, 접근 제한 및 감사 로그가 필요합니다.

기존 설정 파일에 들어 있던 사용하지 않는 MariaDB 비밀번호는 삭제했습니다. 해당 비밀번호가 실제 시스템에서 사용 중이라면 **반드시 변경**해야 합니다. Git 기록에 남아 있던 과거 내용은 파일 수정만으로 제거되지 않습니다.

---
한국폴리텍대학 서울강서캠퍼스 빅데이터소프트웨어공학과
