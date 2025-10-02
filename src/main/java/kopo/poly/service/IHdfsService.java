package kopo.poly.service;

import kopo.poly.dto.HdfsDTO;

import java.util.List;

public interface IHdfsService {

    /**
     * DB 저장된 파일 업로드 정보 저장하고, 전체 업로드 내역 조회하기
     */
    List<HdfsDTO> insertHdfsFileInfo(HdfsDTO pDTO) throws Exception;

    /**
     * DB 저장된 파일 업로드 정보 조회하기
     */
    List<HdfsDTO> getHdfsInfoList() throws Exception;


    /**
     * DB 저장된 파일 업로드 정보 조회하기
     */
    HdfsDTO getHdfsInfo(HdfsDTO pDTO) throws Exception;
}

