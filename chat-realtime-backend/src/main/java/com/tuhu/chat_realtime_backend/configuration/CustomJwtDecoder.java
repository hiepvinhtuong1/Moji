package com.tuhu.chat_realtime_backend.configuration;

import com.nimbusds.jose.JOSEException;
import com.tuhu.chat_realtime_backend.service.JwtService;
import com.tuhu.chat_realtime_backend.service.impl.JwtServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Objects;

@Component
public class CustomJwtDecoder implements JwtDecoder {

    @Value("${jwt.access-token-key}")
    private String accessTokenSignerKey;

    private final JwtService jwtService;

    private NimbusJwtDecoder nimbusJwtDecoder = null;

    public CustomJwtDecoder(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        // BƯỚC 1: Kiểm tra tính hợp lệ về nghiệp vụ (Blacklist, Expired, Signature)
        // Chúng ta gọi JwtService trước để đảm bảo token chưa bị Logout
        try {
            jwtService.verifyToken(token, false);
        } catch (JOSEException | ParseException e) {
            // Quan trọng: Phải throw JwtException thì AuthenticationEntryPoint mới bắt được
            throw new JwtException("Token invalid: " + e.getMessage());
        }

        // BƯỚC 2: Khởi tạo NimbusJwtDecoder (chỉ thực hiện 1 lần duy nhất - Thread-safe)
        if (Objects.isNull(nimbusJwtDecoder)) {
            synchronized (this) {
                if (nimbusJwtDecoder == null) {
                    SecretKeySpec secretKeySpec = new SecretKeySpec(accessTokenSignerKey.getBytes(), "HS256");
                    nimbusJwtDecoder = NimbusJwtDecoder
                            .withSecretKey(secretKeySpec)
                            .macAlgorithm(MacAlgorithm.HS256)
                            .build();
                }
            }
        }

        // BƯỚC 3: Giải mã token sang đối tượng Jwt của Spring Security
        // Nimbus sẽ tự động trích xuất các Claims như sub, exp, scope...
        try {
            return nimbusJwtDecoder.decode(token);
        } catch (JwtException e) {
            throw new JwtException("Decode failed: " + e.getMessage());
        }
    }
}
