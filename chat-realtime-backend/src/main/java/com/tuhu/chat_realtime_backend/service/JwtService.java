package com.tuhu.chat_realtime_backend.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import com.tuhu.chat_realtime_backend.entity.User;

import java.text.ParseException;

public interface JwtService {
    SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException;
    SignedJWT getSignedJWT(String token, boolean isRefresh) throws JOSEException,ParseException;
    String generateAccessToken(User user);
    String generateRefreshToken(User user);
}
