# 🌱 webHadoop – WebHDFS 실습 프로젝트

**Spring Boot 3.5.x · Java 17** 기반의 **WebHDFS REST API 실습 프로젝트**입니다.  
Spring WebFlux의 `WebClient`와 Apache Hadoop WebHDFS API를 활용하여  
**HDFS 파일 업로드(WRITE), 삭제(DELETE), 목록 조회(LISTSTATUS)** 기능을 구현했습니다.  

<p align="left">
  <img alt="java" src="https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white">
  <img alt="spring-boot" src="https://img.shields.io/badge/Spring%20Boot-3.5.x-6DB33F?logo=springboot&logoColor=white">
  <img alt="spring-webflux" src="https://img.shields.io/badge/Spring-WebFlux-6DB33F?logo=spring&logoColor=white">
  <img alt="hadoop" src="https://img.shields.io/badge/Apache%20Hadoop-3.3.6-66CCFF?logo=apachehadoop&logoColor=white">
  <img alt="build" src="https://img.shields.io/badge/Build-Gradle-02303A?logo=gradle&logoColor=white">
</p>

---

## ✨ 주요 기능

- **WebHDFS 파일 업로드 (WRITE)**
  - `/webhdfs/v1/upload`  
  - `op=CREATE` 요청 → NameNode 30x Redirect → DataNode PUT까지 처리
  - `WebClient`를 이용해 **리다이렉트까지 직접 처리**하는 실습

- **WebHDFS 파일 삭제 (DELETE)**
  - `/webhdfs/v1/delete`  
  - `op=DELETE` 호출로 파일/디렉토리 삭제

- **디렉토리 목록 조회 (LISTSTATUS)**
  - `/webhdfs/v1/list`  
  - `op=LISTSTATUS` 결과(JSON)를 문자열로 반환  
  - 프론트 단에서 `JSON.parse(...)` 후 **파일/디렉토리 리스트 렌더링**

- **공통 응답 포맷**
  - `CommonResponse<T>`를 통한 **상태코드 + 메시지 + 데이터** 일관된 응답

- **DTO / 유틸**
  - `WebHdfsDTO`로 `path`, `content`를 캡슐화
  - `CmmUtil.nvl()` 로 NPE 방지용 공통 유틸

- **간단한 웹 UI**
  - `static/webHdfs.html` + jQuery  
  - 브라우저에서 **파일 업로드 / 삭제 / 목록 조회**를 바로 실습

> ⚠️ 학습용 코드이므로 운영환경에서는 **보안(HTTPS), 인증/인가, 예외 처리, 설정 분리(application-*.yml)** 등을 반드시 강화해야 합니다.

---

## 🧱 기술 스택

- **Backend**
  - Spring Boot 3.5.x
  - Spring WebFlux (`WebClient`)
  - Spring Web (REST Controller)
  - Apache Hadoop `hadoop-client:3.3.6`
  - Lombok

- **Build & 기타**
  - Gradle (Wrapper 포함)
  - JDK 17

- **Frontend**
  - jQuery 3.6.x
  - 정적 HTML/CSS (`webHdfs.html`, `table.css`)

---

## 📁 프로젝트 구조(요약)

```bash
webHadoop/
├─ build.gradle
├─ settings.gradle
├─ gradlew, gradlew.bat
└─ src/
   ├─ main/
   │  ├─ java/kopo/poly/
   │  │  ├─ WebHadoopApplication.java        # Spring Boot 메인 클래스
   │  │  ├─ controller/
   │  │  │  ├─ WebHdfsController.java        # /webhdfs/v1 REST API (upload/delete/list)
   │  │  │  └─ response/
   │  │  │     └─ CommonResponse.java        # 공통 응답 DTO
   │  │  ├─ dto/
   │  │  │  └─ WebHdfsDTO.java               # path, content DTO
   │  │  ├─ service/
   │  │  │  ├─ IWebHdfsService.java          # HDFS_URI, USER_NAME 상수 정의 + 인터페이스
   │  │  │  └─ impl/
   │  │  │     └─ WebHdfsService.java        # WebClient 기반 WebHDFS 호출(WRITE/DELETE/LISTSTATUS)
   │  │  └─ util/
   │  │     └─ CmmUtil.java                  # nvl 등 공통 유틸
   │  └─ resources/
   │     ├─ application.properties           # 서버 포트, 기타 설정
   │     └─ static/
   │        ├─ webHdfs.html                  # WebHDFS 실습용 웹 페이지
   │        ├─ css/table.css                 # 테이블 스타일
   │        └─ js/jquery-3.6.0.min.js
   └─ test/java/kopo/poly/
      └─ WebHadoopApplicationTests.java
```

---

## ⚙️ 빠른 시작

### 1) 필수 요건

- **JDK 17**
- **Gradle 8.x+**
- WebHDFS가 활성화된 **Hadoop 클러스터**
  - NameNode WebHDFS 포트(예: `9870`)
  - HDFS 사용자 계정 (예: `hadoop`)

### 2) WebHDFS 접속 설정

`IWebHdfsService.java` 내부 상수를 실제 환경에 맞게 수정합니다.

```java
public interface IWebHdfsService {

    String HDFS_URI = "http://192.168.133.131:9870/webhdfs/v1"; // WebHDFS URL
    String USER_NAME = "hadoop"; // HDFS 사용자 이름

    String upload(WebHdfsDTO pDTO);
    String delete(WebHdfsDTO pDTO);
    String list(WebHdfsDTO pDTO);
}
```

- `HDFS_URI` → **본인 네임노드 주소/포트**로 변경
- `USER_NAME` → HDFS에서 사용할 계정명으로 변경

필요하다면 `WebHdfsController` 의 기본 업로드 경로도 조정할 수 있습니다.

```java
// 기본 업로드 디렉토리 (현재는 /01)
private final String hdfsUploadDir = "/01";
```

### 3) 애플리케이션 포트 확인/변경

`src/main/resources/application.properties`:

```properties
# WebServer Port
server.port=11000
```

- 기본 포트는 `11000` 입니다. 필요 시 다른 포트로 변경 가능합니다.

### 4) 빌드 & 실행

프로젝트 루트(`webHadoop/`)에서:

```bash
./gradlew clean bootRun
# 또는
./gradlew bootJar && java -jar build/libs/webHadoop-0.0.1-SNAPSHOT.jar
```

### 5) 웹 UI 접속

서버 실행 후 브라우저에서 아래 주소로 접속합니다.

```text
http://localhost:11000/webHdfs.html
```

- 상단 폼에서 `경로(path)`와 `내용(content)`를 입력 후 **파일 업로드**
- 하단 테이블에서 **HDFS 내 파일/디렉토리 목록** 확인
- 파일명 클릭 시 **삭제 요청** 실행

---

## 🧪 빠른 점검(Quick Test – cURL)

서버가 `localhost:11000` 에서 실행 중이라고 가정합니다.

### 1) 디렉토리 목록 조회

```bash
curl -X GET "http://localhost:11000/webhdfs/v1/list"
```

- `CommonResponse<String>` 형태의 JSON이 내려오며,
- `data` 필드 안에 WebHDFS의 **LISTSTATUS 응답(JSON 문자열)** 이 포함됩니다.

### 2) 파일 업로드

```bash
curl -X POST "http://localhost:11000/webhdfs/v1/upload"   -d "path=test.txt"   -d "content=Hello WebHDFS!"
```

- 실제 HDFS 경로는 컨트롤러의 기본 디렉토리 기준으로  
  `/01/test.txt` 로 저장됩니다.

### 3) 파일 삭제

```bash
curl -X DELETE "http://localhost:11000/webhdfs/v1/delete"   -d "path=test.txt"
```

- `/01/test.txt` 파일 삭제 시도  
- 정상 삭제 시 `"File deleted successfully"` 메시지 응답

---

## 🗺️ API 엔드포인트 요약

| 기능          | HTTP 메서드 | 경로                  | 비고                          |
|---------------|------------|-----------------------|-------------------------------|
| 파일 업로드    | POST       | `/webhdfs/v1/upload`  | `path`, `content` form 필드   |
| 파일 삭제      | DELETE     | `/webhdfs/v1/delete`  | `path` form 필드              |
| 디렉토리 목록  | GET        | `/webhdfs/v1/list`    | 기본 업로드 디렉토리 기준     |

> 모든 응답은 `CommonResponse<T>` 형식으로 내려옵니다.  
> (HTTP 상태 코드 + 메시지 + 데이터)

---

## 📚 학습 포인트

- **WebHDFS REST API 구조 이해**
  - `op=CREATE`, `op=DELETE`, `op=LISTSTATUS` 파라미터
  - NameNode → DataNode **Redirect(30x)** 흐름

- **Spring WebFlux WebClient 활용**
  - `exchangeToMono` 로 상태코드 기반 분기 처리
  - Redirect 응답에서 `Location` 헤더 추출 후 재요청

- **간단한 프론트–백엔드 연동**
  - jQuery Ajax로 REST API 호출
  - JSON 문자열을 파싱해 테이블로 렌더링

---

## 🙋‍♀️ 문의

- **한국폴리텍대학 서울강서캠퍼스 빅데이터소프트웨어과**  
- **이협건 교수** · <hglee67@kopo.ac.kr>  
- 입학/프로젝트 상담 오픈채팅방: <https://open.kakao.com/o/gEd0JIad>
