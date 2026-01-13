package com.tuhu.chat_realtime_backend.configuration;

import com.tuhu.chat_realtime_backend.exception.AppException;
import com.tuhu.chat_realtime_backend.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.Objects;

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
        // 1. Khởi tạo decoder nếu chưa có
        if (nimbusJwtDecoder == null) {
            initializeDecoder();
        }

        try {
            // 2. Kiểm tra nghiệp vụ trước (Hết hạn, Blacklist, v.v.) qua JwtService
            // Nếu verifyToken ném AppException, nó sẽ rơi vào khối catch bên dưới
            jwtService.verifyToken(token, false);

            // 3. Nếu pass nghiệp vụ, tiến hành decode và kiểm tra chữ ký (Signature)
            return nimbusJwtDecoder.decode(token);

        } catch (AppException e) {
            // Ném lỗi nghiệp vụ (ví dụ: TOKEN_EXPIRED_EXCEPTION)
            // Message này sẽ được CustomAuthenticationEntryPoint nhận qua authException.getMessage()
            throw new BadJwtException(e.getErrorCode().name());

        } catch (JwtException e) {
            // Lỗi của Nimbus (ví dụ: Chữ ký sai, Format sai)
            throw new BadJwtException("INVALID_TOKEN");

        } catch (Exception e) {
            // Các lỗi hệ thống khác
            throw new BadJwtException("AUTHENTICATION_FAILED");
        }
    }

    private synchronized void initializeDecoder() {
        if (nimbusJwtDecoder != null) return;

        SecretKeySpec key = new SecretKeySpec(accessTokenSignerKey.getBytes(), "HS256");

        this.nimbusJwtDecoder = NimbusJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}