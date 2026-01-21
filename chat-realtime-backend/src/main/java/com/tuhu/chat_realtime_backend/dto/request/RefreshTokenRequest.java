package com.tuhu.chat_realtime_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenRequest {
    private String accessToken;

    private String refreshToken;
}
