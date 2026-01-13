package com.tuhu.chat_realtime_backend.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuhu.chat_realtime_backend.dto.response.ApiResponse;
import com.tuhu.chat_realtime_backend.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // 1. Mặc định là UNAUTHORIZED (401)
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        // 2. Kiểm tra nếu nguyên nhân lỗi là do Token hết hạn
        // Message này khớp với "Token invalid: " + e.getMessage() trong CustomJwtDecoder
        if (authException.getMessage() != null && authException.getMessage().contains("expired")) {
            errorCode = ErrorCode.TOKEN_EXPIRED_EXCEPTION;
        }

        // 3. Cấu hình Response
        response.setStatus(errorCode.getHttpStatusCode().value()); // Sẽ là 410 nếu hết hạn
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .statusCode(errorCode.getErrorCode()) // Mã nội bộ (VD: 2000)
                .message(errorCode.getMessage())
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        response.flushBuffer();
    }
}
