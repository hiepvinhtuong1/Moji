package com.tuhu.chat_realtime_backend.service;


import com.nimbusds.jose.JOSEException;
import com.tuhu.chat_realtime_backend.dto.request.LoginRequest;
import com.tuhu.chat_realtime_backend.dto.request.LogoutRequest;
import com.tuhu.chat_realtime_backend.dto.request.RefreshTokenRequest;
import com.tuhu.chat_realtime_backend.dto.request.RegisterRequest;
import com.tuhu.chat_realtime_backend.dto.response.auth.LoginResponse;
import com.tuhu.chat_realtime_backend.dto.response.auth.RefreshTokenResponse;

import java.text.ParseException;

public interface AuthService {
    public void register(RegisterRequest request);
    public LoginResponse login(LoginRequest request);
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) throws ParseException, JOSEException;
    public void logout(LogoutRequest request) throws ParseException, JOSEException;
}
