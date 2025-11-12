package kopo.poly.controller;

import kopo.poly.controller.response.CommonResponse;
import kopo.poly.dto.WebHdfsDTO;
import kopo.poly.service.impl.WebHdfsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * WebHdfsController
 *
 * 웹 애플리케이션에서 HDFS와 상호작용하는 REST 컨트롤러입니다.
 * 업로드, 삭제, 디렉토리 목록 조회 API를 제공합니다.
 *
 * 엔드포인트 베이스 경로: /webhdfs/v1
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/webhdfs/v1")
@Slf4j
public class WebHdfsController {

    /**
     * WebHDFS 서비스 객체 주입
     * 실제 HDFS 연동 로직은 이 서비스에서 구현됩니다.
     */
    private final WebHdfsService webHdfsService;

    /**
     * HDFS에 저장되는 기본 업로드 디렉토리
     * 클라이언트에서 전달한 상대 경로는 이 값 아래에 저장됩니다.
     * 예: 클라이언트가 "file.txt"를 전달하면 실제 경로는 "/01/file.txt"가 됩니다.
     */
    private final String hdfsUploadDir = "/01";

    /**
     * 파일 업로드 API
     *
     * @param path    HDFS 내 상대 경로(예: "subdir/file.txt"). 서버에서는 기본 업로드 디렉토리(hdfsUploadDir)를 앞에 붙입니다.
     * @param content 업로드할 파일의 내용(문자열)
     * @return 업로드 결과 메시지와 함께 HTTP 200 또는 에러 상태
     */
    @PostMapping("/upload")
    public ResponseEntity<CommonResponse<String>> upload(@RequestParam("path") String path,
                                                         @RequestParam("content") String content) {

        log.info("{}.upload Start!", this.getClass().getName());

        try {
            log.info("path : {}, content : {}", path, content);

            WebHdfsDTO pDTO = new WebHdfsDTO();
            pDTO.setPath(hdfsUploadDir + "/" + path);
            pDTO.setContent(content);

            String result = webHdfsService.upload(pDTO);

            log.info("{}.upload End!", this.getClass().getName());

            return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, "File uploaded successfully", result));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "File upload failed", e.getMessage()));
        }

    }

    /**
     * 파일 삭제 API
     *
     * @param path HDFS 내 상대 경로(예: "subdir/file.txt"). 실제 경로는 기본 업로드 디렉토리를 앞에 붙여 사용합니다.
     * @return 삭제 결과 메시지와 함께 HTTP 200 또는 에러 상태
     */
    @DeleteMapping("/delete")
    public ResponseEntity<CommonResponse<String>> delete(@RequestParam("path") String path) {

        log.info("{}.delete Start!", this.getClass().getName());

        try {
            WebHdfsDTO pDTO = new WebHdfsDTO();
            pDTO.setPath(hdfsUploadDir + "/" + path);

            String result = webHdfsService.delete(pDTO);
            return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, "File deleted successfully", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "File deletion failed", e.getMessage()));
        }
    }

    /**
     * 디렉토리 목록 조회 API
     *
     * 기본 업로드 디렉토리(hdfsUploadDir) 아래의 파일/디렉토리 목록을 조회합니다.
     * @return 디렉토리 목록(문자열 형태)과 함께 HTTP 200 또는 에러 상태
     */
    @GetMapping("/list")
    public ResponseEntity<CommonResponse<String>> list() {

        log.info("{}.list Start!", this.getClass().getName());

        try {
            WebHdfsDTO pDTO = new WebHdfsDTO();
            pDTO.setPath(hdfsUploadDir);


            String result = webHdfsService.list(pDTO);
            return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, "Directory listed successfully", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to list directory", e.getMessage()));
        }
    }
}
