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

        // 1. Định nghĩa mã lỗi (Sử dụng ErrorCode đã thống nhất)
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        // 2. Thiết lập Header cho Response (JSON UTF-8)
        response.setStatus(errorCode.getHttpStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 3. Tạo Body trả về theo format ApiResponse chuẩn của dự án
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .statusCode(errorCode.getErrorCode())
                .message(errorCode.getMessage())
                .build();

        // 4. Sử dụng ObjectMapper (Jackson) để ghi JSON vào Response
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));

        // 5. Kết thúc và gửi response về Client (React)
        response.flushBuffer();
    }
}
