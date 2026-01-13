package com.tuhu.chat_realtime_backend.configuration;

import com.nimbusds.jose.JOSEException;
import com.tuhu.chat_realtime_backend.exception.AppException;
import com.tuhu.chat_realtime_backend.exception.ErrorCode;
import com.tuhu.chat_realtime_backend.service.JwtService;
import com.tuhu.chat_realtime_backend.service.impl.JwtServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;


@Component
public class CustomJwtDecoder implements JwtDecoder {

    @Value("${jwt.access-token-key}")
    private String accessTokenSignerKey;

    private final JwtService jwtService;
    private NimbusJwtDecoder nimbusJwtDecoder;

    public CustomJwtDecoder(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        if (nimbusJwtDecoder == null) {
            initializeDecoder();
        }
        return nimbusJwtDecoder.decode(token);
    }

    private synchronized void initializeDecoder() {
        if (nimbusJwtDecoder != null) return;

        SecretKeySpec key = new SecretKeySpec(accessTokenSignerKey.getBytes(), "HS256");

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        // Cài đặt Validator tùy chỉnh
        decoder.setJwtValidator(jwt -> {
            try {
                // Logic kiểm tra nghiệp vụ tập trung ở JwtService
                jwtService.verifyToken(jwt.getTokenValue(), false);
                return OAuth2TokenValidatorResult.success();
            } catch (AppException e) {
                // Ánh xạ lỗi nghiệp vụ sang mã lỗi OAuth2 chuẩn
                String errorCode = (e.getErrorCode() == ErrorCode.TOKEN_EXPIRED_EXCEPTION)
                        ? "token_expired" : "invalid_token";

                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error(errorCode, e.getMessage(), null)
                );
            } catch (Exception e) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Authentication failed", null)
                );
            }
        });

        this.nimbusJwtDecoder = decoder;
    }
}