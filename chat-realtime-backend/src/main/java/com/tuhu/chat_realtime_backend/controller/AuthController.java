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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

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
    public ApiResponse<LoginResponse> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletResponse response) { // Thêm HttpServletResponse

        var result = authService.login(request);

        // 1. Tạo Cookie cho Refresh Token
        Cookie refreshTokenCookie = new Cookie("refresh_token", result.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true); // Quan trọng: Chống XSS
        refreshTokenCookie.setSecure(false); // Đặt thành true nếu dùng HTTPS
        refreshTokenCookie.setPath("/"); // Áp dụng cho toàn bộ domain
        refreshTokenCookie.setMaxAge(60); // Ví dụ 7 ngày (khớp với JWT expiry)

        // 2. Add cookie vào response
        response.addCookie(refreshTokenCookie);

        // 3. Xóa RefreshToken khỏi Object trả về Body để tăng bảo mật
        result.setRefreshToken(null);

        return ApiResponse.<LoginResponse>builder()
                .data(result)
                .message("Login successfully")
                .build();
    }

    @Operation(summary = "Làm mới Token", description = "Sử dụng Refresh Token để lấy cặp Token mới (Rotation)")
    @PostMapping("/refresh")
    public ApiResponse<RefreshTokenResponse> refresh(
            @CookieValue(name = "refresh_token") String refreshToken, // Lấy từ Cookie
            @RequestBody RefreshTokenRequest request,
            HttpServletResponse response) throws ParseException, JOSEException {

        // Gán refreshToken từ Cookie vào request trước khi xử lý logic
        request.setRefreshToken(refreshToken);

        var result = authService.refreshToken(request);

        // Cập nhật lại Refresh Token Cookie mới (Rotation)
        Cookie newCookie = new Cookie("refresh_token", result.getRefreshToken());
        newCookie.setHttpOnly(true);
        newCookie.setPath("/");
        response.addCookie(newCookie);

        result.setRefreshToken(null); // Không trả về Body
        return ApiResponse.<RefreshTokenResponse>builder()
                .data(result)
                .build();
    }

    @Operation(summary = "Đăng xuất", description = "Vô hiệu hóa Token hiện tại (Đưa vào Blacklist)")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @CookieValue(name = "refresh_token") String refreshToken,
            @RequestBody LogoutRequest request,
            HttpServletResponse response) throws ParseException, JOSEException {

        request.setRefreshToken(refreshToken);
        authService.logout(request);

        // Xóa Cookie khi logout
        Cookie cookie = new Cookie("refresh_token", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        return ApiResponse.<Void>builder()
                .message("Logout successfully")
                .build();
    }
}