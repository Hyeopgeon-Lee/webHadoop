package kopo.poly.service;

import kopo.poly.dto.WebHdfsDTO;

public interface IWebHdfsService {

    String HDFS_URI = "http://192.168.133.131:9870/webhdfs/v1"; // WebHDFS URL
    String USER_NAME = "hadoop"; // HDFS 사용자 이름

    /**
     * HDFS에 파일을 업로드합니다.
     *
     * @param pDTO HDFS 상의 파일 경로 (예: "/user/hdfs/test.txt"), 업로드할 파일의 내용
     * @return 업로드 작업의 결과 (성공/실패 메시지 등)
     */
    String upload(WebHdfsDTO pDTO);

    /**
     * HDFS에서 파일을 삭제합니다.
     *
     * @param pDTO 삭제할 파일의 HDFS 경로
     * @return 삭제 작업의 결과 (성공/실패 메시지 등)
     */
    String delete(WebHdfsDTO pDTO);

    /**
     * HDFS 디렉토리의 파일 및 하위 디렉토리 목록을 조회합니다.
     *
     * @param pDTO 조회할 디렉토리의 HDFS 경로
     * @return 디렉토리의 파일 및 하위 디렉토리 정보 (JSON 문자열 형태)
     */
    String list(WebHdfsDTO pDTO);

}
