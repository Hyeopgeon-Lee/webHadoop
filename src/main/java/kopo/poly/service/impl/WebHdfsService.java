package kopo.poly.service.impl;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import kopo.poly.dto.WebHdfsDTO;
import kopo.poly.service.IWebHdfsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriUtils;
import reactor.core.publisher.Mono;

/**
 * Two-step WebHDFS CREATE: NameNode returns HTTP 307 with a DataNode Location,
 * then the DataNode receives the file bytes and returns HTTP 201.
 *
 * WebClient is non-blocking internally; .block() is used because this sample
 * exposes synchronous Spring MVC controller endpoints.
 */
@Service
public class WebHdfsService implements IWebHdfsService {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private final WebClient webClient;
    private final String baseUrl;
    private final String username;

    public WebHdfsService(WebClient.Builder clientBuilder,
                          @Value("${webhdfs.base-url}") String baseUrl,
                          @Value("${webhdfs.username}") String username) {
        this.webClient = clientBuilder.build();
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.username = username;

        if (!this.baseUrl.startsWith("http://") && !this.baseUrl.startsWith("https://")) {
            throw new IllegalArgumentException("webhdfs.base-url must be HTTP or HTTPS");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("webhdfs.username must not be empty");
        }
    }

    @Override
    public String upload(WebHdfsDTO request) {
        URI createUri = buildUri(request.getPath(), "CREATE", "&overwrite=true");
        byte[] bytes = (request.getContent() == null ? "" : request.getContent())
                .getBytes(StandardCharsets.UTF_8);

        // Do NOT send the content to the NameNode: WebHDFS CREATE requires a
        // body-less request first and file bytes only on the redirected PUT.
        String result = webClient.put().uri(createUri)
                .exchangeToMono(response -> {
                    if (response.statusCode().value() != HttpStatus.TEMPORARY_REDIRECT.value()) {
                        return response.createException().flatMap(error ->
                                Mono.error(new IllegalStateException(
                                        "NameNode CREATE did not return HTTP 307 (received "
                                                + response.statusCode().value() + ")", error)));
                    }

                    URI dataNodeUri = response.headers().asHttpHeaders().getLocation();
                    if (dataNodeUri == null || !dataNodeUri.isAbsolute()
                            || !("http".equalsIgnoreCase(dataNodeUri.getScheme())
                            || "https".equalsIgnoreCase(dataNodeUri.getScheme()))) {
                        return Mono.error(new IllegalStateException("Invalid DataNode redirect Location"));
                    }

                    return webClient.put().uri(dataNodeUri)
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .bodyValue(bytes)
                            .exchangeToMono(dataNodeResponse -> {
                                if (dataNodeResponse.statusCode().value() == HttpStatus.CREATED.value()) {
                                    // WebHDFS returns HTTP 201 with an EMPTY body on success.
                                    return dataNodeResponse.releaseBody().thenReturn("true");
                                }
                                return dataNodeResponse.createException().flatMap(Mono::error);
                            });
                })
                .block(REQUEST_TIMEOUT);

        if (!"true".equals(result)) {
            throw new IllegalStateException("WebHDFS upload was not acknowledged");
        }
        return result;
    }

    @Override
    public String delete(WebHdfsDTO request) {
        // Hadoop may respond 200 with {"boolean":false}; that is NOT success.
        Map<?, ?> result = webClient.delete()
                .uri(buildUri(request.getPath(), "DELETE", ""))
                .retrieve()
                .bodyToMono(Map.class)
                .block(REQUEST_TIMEOUT);

        if (result == null || !Boolean.TRUE.equals(result.get("boolean"))) {
            throw new IllegalStateException("WebHDFS did not delete the requested file");
        }
        return "true";
    }

    @Override
    public String list(WebHdfsDTO request) {
        String result = webClient.get()
                .uri(buildUri(request.getPath(), "LISTSTATUS", ""))
                .retrieve()
                .bodyToMono(String.class)
                .block(REQUEST_TIMEOUT);
        if (result == null || result.isBlank()) {
            throw new IllegalStateException("WebHDFS returned an empty LISTSTATUS result");
        }
        return result;
    }

    private URI buildUri(String path, String operation, String extraQuery) {
        if (path == null || !path.startsWith("/") || path.equals("/")
                || path.indexOf('\\') >= 0 || path.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Invalid HDFS path");
        }

        String[] segments = path.substring(1).split("/", -1);
        if (Arrays.stream(segments).anyMatch(s -> s.isBlank()
                || s.equals(".") || s.equals(".."))) {
            throw new IllegalArgumentException("Invalid HDFS path segment");
        }

        String encodedPath = Arrays.stream(segments)
                .map(s -> UriUtils.encodePathSegment(s, StandardCharsets.UTF_8))
                .collect(Collectors.joining("/"));
        String encodedUser = UriUtils.encodeQueryParam(username, StandardCharsets.UTF_8);
        return URI.create(baseUrl + "/" + encodedPath
                + "?op=" + operation + "&user.name=" + encodedUser + extraQuery);
    }
}
