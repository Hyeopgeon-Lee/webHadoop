package kopo.poly.service.impl;

import java.util.ArrayList;
import java.util.List;
import kopo.poly.dto.WebHdfsDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.*;

class WebHdfsServiceTest {
    private WebHdfsService service(ExchangeFunction exchange) {
        return new WebHdfsService(WebClient.builder().exchangeFunction(exchange),
                "http://namenode:9870/webhdfs/v1", "hadoop");
    }

    private WebHdfsDTO dto(String path, String content) {
        WebHdfsDTO dto = new WebHdfsDTO();
        dto.setPath(path);
        dto.setContent(content);
        return dto;
    }

    @Test
    void uploadFollows307AndAcceptsEmpty201Response() {
        List<ClientRequest> requests = new ArrayList<>();
        ExchangeFunction exchange = request -> {
            requests.add(request);
            if (requests.size() == 1) {
                return Mono.just(ClientResponse.create(HttpStatus.TEMPORARY_REDIRECT)
                        .header(HttpHeaders.LOCATION,
                                "http://datanode:9864/webhdfs/v1/01/file.txt?op=CREATE")
                        .build());
            }
            return Mono.just(ClientResponse.create(HttpStatus.CREATED).build());
        };
        assertEquals("true", service(exchange).upload(dto("/01/file.txt", "Hello!")));
        assertEquals(2, requests.size());
        assertEquals("PUT", requests.get(0).method().name());
        assertTrue(requests.get(0).url().getQuery().contains("op=CREATE"));
        assertTrue(requests.get(0).url().getQuery().contains("overwrite=true"));
        assertEquals("datanode", requests.get(1).url().getHost());
    }

    @Test
    void missingLocationFails() {
        WebHdfsService service = service(request ->
                Mono.just(ClientResponse.create(HttpStatus.TEMPORARY_REDIRECT).build()));
        assertThrows(IllegalStateException.class,
                () -> service.upload(dto("/01/file.txt", "test")));
    }

    @Test
    void unsuccessfulDataNodeFails() {
        int[] count = {0};
        WebHdfsService service = service(request -> {
            if (++count[0] == 1) {
                return Mono.just(ClientResponse.create(HttpStatus.TEMPORARY_REDIRECT)
                        .header(HttpHeaders.LOCATION, "http://datanode:9864/file").build());
            }
            return Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("write failed").build());
        });
        assertThrows(Exception.class, () -> service.upload(dto("/01/file.txt", "test")));
    }

    @Test
    void deleteRejectsFalseEvenWithHttp200() {
        WebHdfsService service = service(request ->
                Mono.just(ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, "application/json")
                        .body("{\"boolean\":false}").build()));
        assertThrows(IllegalStateException.class,
                () -> service.delete(dto("/01/missing.txt", null)));
    }

    @Test
    void deleteAcceptsTrue() {
        WebHdfsService service = service(request ->
                Mono.just(ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, "application/json")
                        .body("{\"boolean\":true}").build()));
        assertEquals("true", service.delete(dto("/01/file.txt", null)));
    }

    @Test
    void listReturnsJson() {
        String json = "{\"FileStatuses\":{\"FileStatus\":[]}}";
        WebHdfsService service = service(request ->
                Mono.just(ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, "application/json")
                        .body(json).build()));
        assertEquals(json, service.list(dto("/01", null)));
    }

    @Test
    void traversalFailsWithoutNetworkCall() {
        WebHdfsService service = service(request -> fail("Unexpected WebHDFS request"));
        assertThrows(IllegalArgumentException.class,
                () -> service.upload(dto("/01/../private.txt", "test")));
    }
}
