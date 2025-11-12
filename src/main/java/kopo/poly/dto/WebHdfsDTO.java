package kopo.poly.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * WebHdfsDTO
 *
 * 웹 애플리케이션에서 HDFS와 데이터를 주고받을 때 사용하는 데이터 전송 객체(DTO)입니다.
 * - path: HDFS 상의 파일 또는 디렉토리 경로
 * - content: 파일의 내용(업로드/다운로드 시 사용되는 문자열 데이터)
 *
 * 이 클래스는 Lombok의 @Getter, @Setter, @ToString을 사용하여 보일러플레이트 코드를 줄입니다.
 */
@ToString
@Getter
@Setter
public class WebHdfsDTO {

    /**
     * HDFS 리소스의 경로입니다. 예: /user/hadoop/data/file.txt
     */
    private String path;

    /**
     * 파일의 내용 또는 전송할 텍스트 데이터입니다.
     * 업로드할 때는 이 필드에 파일 내용을 문자열로 담아 전송합니다.
     */
    private String content;
}
