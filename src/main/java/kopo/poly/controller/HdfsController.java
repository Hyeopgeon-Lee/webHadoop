package kopo.poly.controller;

import kopo.poly.controller.response.CommonResponse;
import kopo.poly.dto.HdfsDTO;
import kopo.poly.service.IHdfsService;
import kopo.poly.util.CmmUtil;
import kopo.poly.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RequestMapping(value = "/hdfs/v1")
@RequiredArgsConstructor
@RestController
public class HdfsController {

    // 하둡 설정 객체
    private final Configuration hadoopConfig;

    // HDFS 업로드된 파일을 DB에 저장 및 조회하기
    private final IHdfsService hdfsService;

    // HDFS에 저장되는 폴더 시작 위치
    private final String hdfsUploadDir = "/01";

    /**
     * HTML 파일로부터 받은 파일 정보를 하둡 분산 파일 시스템에 저장하기
     */
    @ResponseBody
    @PostMapping(value = "fileUpload")
    public ResponseEntity fileUpload(@RequestParam(value = "hdfsUpload") MultipartFile mf) throws Exception {

        log.info("{}.fileUpload Start!", this.getClass().getName());

        // 업로드하는 실제 파일명
        // 다운로드 기능 구현시, 임의로 정의된 파일명을 원래대로 만들어주기 위한 목적
        String originalFileName = CmmUtil.nvl(mf.getOriginalFilename());

        // 파일 확장자 가져오기(파일 확장자를 포함한 전체 이름(myimage.jpg)에서 뒤쪽부터 .이 존재하는 위치 찾기
        String ext = originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();

        log.info("originalFileName : {}", originalFileName);
        log.info("ext : {}", ext);

        // HDFS에 저장되는 파일명(사용자가 업로드하는 파일명에 한글 등 특수문자가 존재할 수 있기 때문에
        // 실제 저장되는 이름은 영어, 숫자만 사용함
        String hadoopUploadFileName = DateUtil.getDateTime("HHmmss") + "." + ext;

        // CentOS에 설치된 하둡 분산 파일 시스템 연결 및 설정하기
        FileSystem hdfs = FileSystem.get(hadoopConfig);

        // 하둡에 파일을 업로드할 폴더
        // 예 : /01/2022/11/20
        String hadoopUploadFilePath = hdfsUploadDir + "/" + DateUtil.getDateTime("yyyy/MM/dd");

        log.info("hadoopUploadFileName : {}", hadoopUploadFileName);
        log.info("hadoopUploadFilePath : {}", hadoopUploadFilePath);

        // 하둡에 폴더 생성하기
        // hadoop fs -mkdir -p /01/2022/11/20
        hdfs.mkdirs(new Path(hadoopUploadFilePath));

        // 하둡분산파일시스템에 저장될 파일경로 및 폴더명
        // 예 : hadoop fs -put access_log.gz /01/02/access_log.gz
        String hadoopFile = hadoopUploadFilePath + "/" + hadoopUploadFileName;

        // 하둡분산파일시스템에 저장가능한 객체로 변환
        Path path = new Path(hadoopFile);

        if (hdfs.exists(path)) { // 하둡분산파일시스템에 파일 존재하면...
            // 기존 업로드되어 있는 파일 삭제하기
            // hadoop fs -rm -r /01/2022/11/20/203030
            hdfs.delete(path, true);

        }

        // HDFS에 파일 만들기
        FSDataOutputStream out = hdfs.create(path);

        // HDFS에 파일 내용 기록하기
        IOUtils.copyBytes(mf.getInputStream(), out, hadoopConfig);

        out.close(); // FSDataOutputStream 객체 닫기(사용이 완료되면 객체을 꼭 닫자!)
        hdfs.close(); // FileSystem 하둡 연결 끊고 닫기

        log.info("DB 저장될 정보들 출력하기");
        log.info("hadoopUploadFileName : {}", hadoopUploadFileName);
        log.info("hadoopUploadFilePath : {}", hadoopUploadFilePath);
        log.info("originalFileName : {}", originalFileName);
        log.info("ext : {}", ext);

        HdfsDTO pDTO = new HdfsDTO();
        pDTO.setFileName(hadoopUploadFileName); // 하둡분산파일시스템에 저장되는 파일명
        pDTO.setFilePath(hadoopUploadFilePath); // 하둡분산파일시스템에 저장되는 파일 경로
        pDTO.setOrgName(originalFileName);  // 업로도는 실제 파일 이름
        pDTO.setExt(ext);

        List<HdfsDTO> rList = hdfsService.insertHdfsFileInfo(pDTO);

        log.info("{}.fileUpload End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), rList));
    }

    /**
     * 하둡 분산 파일 시스템에 저장된 전체 파일 조회하기
     */
    @ResponseBody
    @PostMapping(value = "fileList")
    public ResponseEntity fileList() throws Exception {

        log.info("{}.fileList Start!", this.getClass().getName());

        List<HdfsDTO> rList = hdfsService.getHdfsInfoList();

        log.info("{}.fileList End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), rList));

    }

    /**
     * 하둡 분산 파일 시스템에 저장된 파일 다운로드하기
     */
    @GetMapping(value = "fileDownload")
    public ResponseEntity<Object> downloadFile(HttpServletRequest request) throws Exception {

        log.info("{}.downloadFile Start!", this.getClass().getName());

        String fileSeq = CmmUtil.nvl(request.getParameter("fileSeq"), "0");
        log.info("fileSeq : {}", fileSeq);

        HdfsDTO pDTO = new HdfsDTO();
        pDTO.setFileSeq(Long.parseLong(fileSeq));

        // DB 저장된 HDFS 저장 정보가져오기
        HdfsDTO rDTO = hdfsService.getHdfsInfo(pDTO);

        // CentOS에 설치된 하둡 분산 파일 시스템 연결 및 설정하기
        FileSystem hdfs = FileSystem.get(hadoopConfig);

        // HDFS 업로드된 파일 중 다운로드할 파일을 스트림으로 변환하여 가져오기
        FSDataInputStream in = hdfs.open(new Path(
                CmmUtil.nvl(rDTO.getFilePath()) + "/" + CmmUtil.nvl(rDTO.getFileName())));

        // 웹에서 다운로드 가능하도록 Byte[] 구조를 다운로 가능한 객체로 변경하기
        // IOUtils.readFullyToByteArray : HDFS 파일 스트림을을 Byte[]로 변환하는 함수
        ByteArrayResource resource = new ByteArrayResource(IOUtils.readFullyToByteArray(in));

        in.close(); // FSDataInputStream 객체 닫기(사용이 완료되면 객체을 꼭 닫자!)
        hdfs.close(); // FileSystem 하둡 연결 끊고 닫기

        // 파일 다운로드를 위한 HTTP 통신 헤더값 설정
        HttpHeaders headers = new HttpHeaders();

        // 파일 다운로드 헤더값 설정
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename(CmmUtil.nvl(rDTO.getOrgName())).build());

        log.info("{}.downloadFile End!", this.getClass().getName());

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);

    }

}
