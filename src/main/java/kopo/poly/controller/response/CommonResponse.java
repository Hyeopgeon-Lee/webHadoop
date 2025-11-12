package kopo.poly.controller.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

/**
 * CommonResponse
 * <p>
 * REST API의 공통 응답 형식을 표현하는 제네릭 DTO입니다.
 * 응답은 HTTP 상태 코드(HttpStatus), 설명 메시지(String), 비즈니스 데이터(T)를 포함합니다.
 * <p>
 * 사용 예:
 * - 성공 응답: CommonResponse.of(HttpStatus.OK, "Success", data)
 * - 오류 응답: CommonResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "Error", errorMessage)
 *
 * @param <T> 응답 본문의 데이터 타입
 */
@Getter
@Setter
public class CommonResponse<T> {

    /**
     * HTTP 상태 정보
     */
    private HttpStatus httpStatus;

    /**
     * 클라이언트에 전달할 설명 메시지
     */
    private String message;

    /**
     * 실제 응답 데이터(성공 시 반환되는 모델 또는 실패 시 에러 정보 등)
     */
    private T data;

    /**
     * Lombok 빌더를 사용하는 생성자
     * 외부에서 직접 new로 생성하기보다 정적 팩토리 메서드 `of`를 사용하면 가독성이 좋아집니다.
     */
    @Builder
    public CommonResponse(HttpStatus httpStatus, String message, T data) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.data = data;
    }

    /**
     * 정적 팩토리 메서드
     * 코드에서 간편하게 CommonResponse 객체를 생성할 때 사용합니다.
     *
     * @param httpStatus HTTP 상태
     * @param message    응답 메시지
     * @param data       응답 데이터
     * @param <T>        데이터 타입
     * @return CommonResponse 객체
     */
    public static <T> CommonResponse<T> of(HttpStatus httpStatus, String message, T data) {
        return new CommonResponse<>(httpStatus, message, data);
    }

}
