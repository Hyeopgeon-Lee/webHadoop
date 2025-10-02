package kopo.poly.service.impl;

import kopo.poly.dto.WebHdfsDTO;
import kopo.poly.service.IWebHdfsService;
import kopo.poly.util.CmmUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class WebHdfsService implements IWebHdfsService {

    private final WebClient webClient = WebClient.create();

    @Override
    public String upload(WebHdfsDTO pDTO) {

        log.info("{}.upload Start!", this.getClass().getName());
        log.info("pDTO : {}", pDTO);

        String hdfsPath = CmmUtil.nvl(pDTO.getPath());
        String content = CmmUtil.nvl(pDTO.getContent());

        String uri = String.format("%s%s?op=CREATE&user.name=%s&overwrite=true", HDFS_URI, hdfsPath, USER_NAME);

        log.info("uri : {}", uri);

        return webClient.put()
                .uri(uri)
                .bodyValue(content)
                .exchangeToMono(response -> {
                    if (response.statusCode().is3xxRedirection()) {
                        String redirectUri = response.headers().asHttpHeaders().getLocation().toString();
                        log.info("Redirect URI: {}", redirectUri);

                        // 다시 PUT 요청을 보내 데이터 업로드
                        return webClient.put()
                                .uri(redirectUri)
                                .bodyValue(content)
                                .retrieve()
                                .bodyToMono(String.class);
                    } else {
                        return response.bodyToMono(String.class);
                    }
                })
                .block();
    }

    @Override
    public String delete(WebHdfsDTO pDTO) {

        log.info("{}.delete Start!", this.getClass().getName());
        log.info("pDTO : {}", pDTO);

        String hdfsPath = CmmUtil.nvl(pDTO.getPath());

        String uri = String.format("%s%s?op=DELETE&user.name=%s", HDFS_URI, hdfsPath, USER_NAME);

        log.info("uri : {}", uri);

        return webClient.delete()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    @Override
    public String list(WebHdfsDTO pDTO) {

        log.info("{}.listFiles Start!", this.getClass().getName());
        log.info("pDTO : {}", pDTO);

        String hdfsPath = CmmUtil.nvl(pDTO.getPath());

        String uri = String.format("%s%s?op=LISTSTATUS&user.name=%s", HDFS_URI, hdfsPath, USER_NAME);

        log.info("uri : {}", uri);

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }


}
