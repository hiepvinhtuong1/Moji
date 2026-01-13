package com.tuhu.chat_realtime_backend.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuhu.chat_realtime_backend.dto.response.ApiResponse;
import com.tuhu.chat_realtime_backend.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        // Lấy message từ exception gốc
        String errorMessage = authException.getMessage();

        // Nếu bạn ném BadJwtException(e.getErrorCode().name()...)
        // thì errorMessage sẽ chứa tên của ErrorCode đó.
        if (errorMessage != null) {
            if (errorMessage.contains("TOKEN_EXPIRED_EXCEPTION")) {
                errorCode = ErrorCode.TOKEN_EXPIRED_EXCEPTION;
            }
        }

        // Gửi response về cho Client
        response.setStatus(errorCode.getHttpStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .statusCode(errorCode.getErrorCode())
                .message(errorCode.getMessage())
                .build();

        new ObjectMapper().writeValue(response.getWriter(), apiResponse);
    }
}
