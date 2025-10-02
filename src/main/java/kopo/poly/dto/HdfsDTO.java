package kopo.poly.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HdfsDTO {

    private Long fileSeq; // 파일 순번

    private String filePath; // 하둡분산파일시스템 저장된 파일 경로

    private String fileName; // 하둡분산파일시스템에 저장된 파일 이름

    private String orgName; // 실제 파일이름

    private String ext; // 파일 확장자

    private String regDt; // 하둡분산파일시스템에 저장한 날짜
}
