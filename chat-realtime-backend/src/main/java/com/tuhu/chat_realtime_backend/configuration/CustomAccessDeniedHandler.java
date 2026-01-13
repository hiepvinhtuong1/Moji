package com.tuhu.chat_realtime_backend.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuhu.chat_realtime_backend.dto.response.ApiResponse;
import com.tuhu.chat_realtime_backend.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException  {

        // 1. Sử dụng mã lỗi FORBIDDEN (403) thay vì UNAUTHENTICATED
        ErrorCode errorCode = ErrorCode.FORBIDDEN; // Bạn hãy thêm mã này vào enum ErrorCode nhé

        response.setStatus(errorCode.getHttpStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .statusCode(errorCode.getErrorCode())
                .message(errorCode.getMessage())
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));

        response.flushBuffer();
    }
}
