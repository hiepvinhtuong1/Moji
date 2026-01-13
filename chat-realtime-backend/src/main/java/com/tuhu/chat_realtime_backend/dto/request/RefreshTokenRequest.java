package com.tuhu.chat_realtime_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class RefreshTokenRequest {
    @NotNull(message = "Access token can not be null")
    private String accessToken;

    @NotNull(message = "Refresh token can not be null")
    private String refreshToken;
}
