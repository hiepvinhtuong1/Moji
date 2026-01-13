package com.tuhu.chat_realtime_backend.exception;

import com.tuhu.chat_realtime_backend.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiResponse> handlingOtherException(Exception e, HttpServletRequest request) {
        ApiResponse response = new ApiResponse();
        response.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());
        response.setStatusCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getErrorCode());
        return ResponseEntity.status(ErrorCode.UNCATEGORIZED_EXCEPTION.getHttpStatusCode()).body(response);
    }

    // handling access denied exception (forbidden)
    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handlingAccessDeniedException(AccessDeniedException exception) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        return ResponseEntity
                .status(errorCode.getHttpStatusCode())
                .body(ApiResponse.builder()
                        .statusCode(errorCode.getErrorCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    //handling app exception
    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse> handlingAppException(AppException e, HttpServletRequest request) {
        ErrorCode errorCode = e.getErrorCode();

        ApiResponse response = new ApiResponse();
        response.setMessage(errorCode.getMessage());
        response.setStatusCode(errorCode.getErrorCode());

        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(response);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handlingValidationException(MethodArgumentNotValidException e) {
        // Lấy thông báo lỗi đầu tiên từ danh sách các lỗi validate
        String enumKey = e.getBindingResult().getFieldError().getDefaultMessage();

        // Bạn có thể thiết lập ErrorCode mặc định cho lỗi Validation
        ErrorCode errorCode = ErrorCode.INVALID_KEY;

        try {
            // Nếu bạn đặt message trong @NotNull là một Key của ErrorCode, có thể map nó ở đây
            // Hoặc đơn giản là trả về message trực tiếp như dưới đây:
            ApiResponse response = ApiResponse.builder()
                    .statusCode(errorCode.getErrorCode())
                    .message(enumKey) // Trả về "Username can not be null"
                    .build();

            return ResponseEntity.badRequest().body(response);
        } catch (IllegalArgumentException iae) {
            // Fallback nếu không map được
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .statusCode(errorCode.getErrorCode())
                            .message(errorCode.getMessage())
                            .build()
            );
        }
    }
}
