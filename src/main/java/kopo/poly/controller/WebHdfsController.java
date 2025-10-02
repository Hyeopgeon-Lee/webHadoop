package kopo.poly.controller;

import kopo.poly.controller.response.CommonResponse;
import kopo.poly.dto.WebHdfsDTO;
import kopo.poly.service.impl.WebHdfsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/webhdfs/v1")
@Slf4j
public class WebHdfsController {

    // WebHDFS 객체
    private final WebHdfsService webHdfsService;

    // HDFS에 저장되는 폴더 시작 위치
    private final String hdfsUploadDir = "/01";

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
