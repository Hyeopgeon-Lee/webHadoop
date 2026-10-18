package kopo.poly.controller;

import java.util.Arrays;

import kopo.poly.controller.response.CommonResponse;
import kopo.poly.dto.WebHdfsDTO;
import kopo.poly.service.IWebHdfsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/webhdfs/v1")
public class WebHdfsController {

    private final IWebHdfsService webHdfsService;
    private final String uploadDir;

    public WebHdfsController(IWebHdfsService webHdfsService,
                             @Value("${webhdfs.upload-dir:/01}") String uploadDir) {
        this.webHdfsService = webHdfsService;
        if (uploadDir == null || !uploadDir.startsWith("/")) {
            throw new IllegalArgumentException("webhdfs.upload-dir must be an absolute HDFS path");
        }
        this.uploadDir = uploadDir.replaceAll("/+$", "");
    }

    @PostMapping("/upload")
    public ResponseEntity<CommonResponse<String>> upload(@RequestParam("path") String path,
                                                           @RequestParam("content") String content) {
        try {
            WebHdfsDTO request = new WebHdfsDTO();
            request.setPath(resolvePath(path));
            request.setContent(content);
            return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK,
                    "File uploaded successfully", webHdfsService.upload(request)));
        } catch (IllegalArgumentException e) {
            return badRequest(e);
        } catch (Exception e) {
            return upstreamFailure("File upload failed", e);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<CommonResponse<String>> delete(@RequestParam("path") String path) {
        try {
            WebHdfsDTO request = new WebHdfsDTO();
            request.setPath(resolvePath(path));
            return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK,
                    "File deleted successfully", webHdfsService.delete(request)));
        } catch (IllegalArgumentException e) {
            return badRequest(e);
        } catch (Exception e) {
            return upstreamFailure("File deletion failed", e);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<CommonResponse<String>> list() {
        try {
            WebHdfsDTO request = new WebHdfsDTO();
            request.setPath(uploadDir.isEmpty() ? "/" : uploadDir);
            return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK,
                    "Directory listed successfully", webHdfsService.list(request)));
        } catch (Exception e) {
            return upstreamFailure("Failed to list directory", e);
        }
    }

    private String resolvePath(String relativePath) {
        if (relativePath == null || relativePath.isBlank() || relativePath.startsWith("/")
                || relativePath.indexOf('\\') >= 0
                || relativePath.chars().anyMatch(Character::isISOControl)
                || Arrays.stream(relativePath.split("/", -1)).anyMatch(segment ->
                segment.isBlank() || segment.equals(".") || segment.equals(".."))) {
            throw new IllegalArgumentException("Enter a relative filename without '.' or '..' segments");
        }
        return uploadDir + "/" + relativePath;
    }

    private ResponseEntity<CommonResponse<String>> badRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(CommonResponse.of(HttpStatus.BAD_REQUEST, e.getMessage(), null));
    }

    private ResponseEntity<CommonResponse<String>> upstreamFailure(String message, Exception e) {
        // Do not return internal NameNode/DataNode addresses or exception traces to clients.
        log.warn("{}: {}", message, e.toString());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(CommonResponse.of(HttpStatus.BAD_GATEWAY,
                        message + " (check WebHDFS connection and permissions)", null));
    }
}
