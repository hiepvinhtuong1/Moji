package com.tuhu.chat_realtime_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LogoutRequest {
    @NotNull(message = "Access token can not be null")
    String accessToken;
    @NotNull(message = "Refresh token can not be null")
    String refreshToken;
}
