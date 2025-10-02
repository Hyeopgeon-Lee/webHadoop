package kopo.poly.mapper;

import kopo.poly.dto.HdfsDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IHdfsMapper {

    /**
     * HDFS 업로드된 파일 정보가 가져오는 쿼리
     */
    void insertHdfsInfo(HdfsDTO pDTO) throws Exception;

    /**
     * HDFS 업로드된 파일 전체 정보가 가져오는 쿼리
     */
    List<HdfsDTO> getHdfsInfoList() throws Exception;

    /**
     * HDFS 업로드된 파일 1개 정보가 가져오는 쿼리
     * - 파일다운로드에 활용됨
     */
    HdfsDTO getHdfsInfo(HdfsDTO pDTO) throws Exception;

}
