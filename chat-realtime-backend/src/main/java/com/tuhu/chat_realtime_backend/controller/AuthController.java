package com.tuhu.chat_realtime_backend.controller;

import com.nimbusds.jose.JOSEException;
import com.tuhu.chat_realtime_backend.dto.request.LoginRequest;
import com.tuhu.chat_realtime_backend.dto.request.LogoutRequest;
import com.tuhu.chat_realtime_backend.dto.request.RefreshTokenRequest;
import com.tuhu.chat_realtime_backend.dto.request.RegisterRequest;
import com.tuhu.chat_realtime_backend.dto.response.ApiResponse;
import com.tuhu.chat_realtime_backend.dto.response.auth.LoginResponse;
import com.tuhu.chat_realtime_backend.dto.response.auth.RefreshTokenResponse;
import com.tuhu.chat_realtime_backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Authentication", description = "Các API liên quan đến xác thực người dùng")
public class AuthController {
    AuthService authService;

    @Operation(summary = "Đăng ký tài khoản mới", description = "Tạo tài khoản người dùng mới vào hệ thống")
    @PostMapping("/register")
    public ApiResponse<Void> register(@RequestBody @Valid RegisterRequest request) {
        authService.register(request);
        return ApiResponse.<Void>builder()
                .message("Register successfully")
                .build();
    }

    @Operation(summary = "Đăng nhập", description = "Trả về Access Token và Refresh Token khi đăng nhập thành công")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        var result = authService.login(request);
        return ApiResponse.<LoginResponse>builder()
                .data(result)
                .message("Login successfully")
                .build();
    }

    @Operation(summary = "Làm mới Token", description = "Sử dụng Refresh Token để lấy cặp Token mới (Rotation)")
    @PostMapping("/refresh")
    public ApiResponse<RefreshTokenResponse> refresh(@RequestBody RefreshTokenRequest request)
            throws ParseException, JOSEException {
        var result = authService.refreshToken(request);
        return ApiResponse.<RefreshTokenResponse>builder()
                .data(result)
                .build();
    }

    @Operation(summary = "Đăng xuất", description = "Vô hiệu hóa Token hiện tại (Đưa vào Blacklist)")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody LogoutRequest request)
            throws ParseException, JOSEException {
        authService.logout(request);
        return ApiResponse.<Void>builder()
                .message("Logout successfully")
                .build();
    }
}