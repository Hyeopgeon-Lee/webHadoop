package kopo.poly.service.impl;

import kopo.poly.dto.HdfsDTO;
import kopo.poly.mapper.IHdfsMapper;
import kopo.poly.service.IHdfsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class HdfsService implements IHdfsService {

    private final IHdfsMapper hdfsMapper;

    @Override
    public List<HdfsDTO> getHdfsInfoList() throws Exception {

        log.info("{}.getHdfsInfoList Start!", this.getClass().getName());

        // HDFS 업로드된 전체 파일 정보를 DB 조회하기
        List<HdfsDTO> rList = Optional.ofNullable(hdfsMapper.getHdfsInfoList()).orElseGet(ArrayList::new);

        log.info("{}.getHdfsInfoList End!", this.getClass().getName());

        return rList;
    }

    @Override
    public List<HdfsDTO> insertHdfsFileInfo(HdfsDTO pDTO) throws Exception {

        log.info("{}.insertHdfsFileInfo Start!", this.getClass().getName());

        // HDFS 업로드된 파일 정보를 DB 저장하기
        hdfsMapper.insertHdfsInfo(pDTO);

        // HDFS 업로드된 전체 파일 정보를 DB 조회하기
        List<HdfsDTO> rList = Optional.ofNullable(hdfsMapper.getHdfsInfoList()).orElseGet(ArrayList::new);

        log.info("{}.insertHdfsFileInfo End!", this.getClass().getName());

        return rList;
    }

    @Override
    public HdfsDTO getHdfsInfo(HdfsDTO pDTO) throws Exception {

        log.info("{}.getHdfsInfo Start!", this.getClass().getName());

        // HDFS 업로드된 전체 파일 정보를 DB 조회하기
        HdfsDTO rDTO = Optional.ofNullable(hdfsMapper.getHdfsInfo(pDTO)).orElseGet(HdfsDTO::new);

        log.info("{}.getHdfsInfo End!", this.getClass().getName());

        return rDTO;
    }

}

