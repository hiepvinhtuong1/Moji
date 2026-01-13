package com.tuhu.chat_realtime_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class RegisterRequest {
    @NotNull(message = "Username can not be null")
    private String username;

    @NotNull(message = "Email can not be null")
    private String email;

    @NotNull(message = "Password can not be null")
    private String password;

    @NotNull(message = "Display name can not be null")
    private String displayName;
}
