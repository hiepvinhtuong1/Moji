package com.tuhu.chat_realtime_backend.service.impl;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.tuhu.chat_realtime_backend.entity.User;
import com.tuhu.chat_realtime_backend.exception.AppException;
import com.tuhu.chat_realtime_backend.exception.ErrorCode;
import com.tuhu.chat_realtime_backend.repository.InvalidatedTokenRepository;
import com.tuhu.chat_realtime_backend.service.JwtService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtServiceImpl implements JwtService {

    InvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal
    @Value("${jwt.access-token-key}")
    String accessTokenSignerKey;

    @NonFinal
    @Value("${jwt.refresh-token-key}")
    String refreshTokenSignerKey;

    @NonFinal
    @Value("${jwt.access-token-duration}")
    int accessTokenDuration;

    @NonFinal
    @Value("${jwt.refresh-token-duration}")
    int refreshTokenDuration;

    private SignedJWT verifyAndParseToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        // 1. Lựa chọn Secret Key phù hợp (Access Key hoặc Refresh Key) dựa vào mục đích sử dụng
        String signerKey = isRefresh ? refreshTokenSignerKey : accessTokenSignerKey;

        // 2. Khởi tạo bộ xác minh chữ ký (Verifier) sử dụng thuật toán HMAC với Secret Key
        JWSVerifier jwsVerifier = new MACVerifier(signerKey);

        // 3. Phân tích chuỗi String token thành đối tượng SignedJWT để truy xuất dữ liệu bên trong
        SignedJWT signedJWT = SignedJWT.parse(token);

        // 4. Kiểm tra chữ ký: Nếu nội dung token bị thay đổi dù chỉ 1 ký tự, hàm này sẽ trả về false
        boolean verified = signedJWT.verify(jwsVerifier);

        if (!verified) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // 6. Kiểm tra danh sách đen (Blacklist): Truy vấn Database xem ID của token này (JTI)
        // có nằm trong danh sách các token đã bị hủy (do người dùng Logout) hay không.
        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return signedJWT;
    }

    @Override
    public SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        // 1. Gọi hàm nội bộ ở trên để kiểm tra Chữ ký và Blacklist trước
        SignedJWT signedJWT = verifyAndParseToken(token, isRefresh);
        // 2. Lấy thời điểm hết hạn được lưu trong phần Payload của Token (Claim "exp")
        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        // 3. Kiểm tra thời hạn: Nếu không có ngày hết hạn hoặc ngày hết hạn đã qua so với hiện tại
        // thì ném lỗi hết hạn để Client biết đường dùng Refresh Token đổi cái mới.
        if (expirationTime == null || !expirationTime.after(new Date())) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED_EXCEPTION);
        }
        return signedJWT;
    }

    @Override
    public SignedJWT getSignedJWT(String token, boolean isRefresh) throws JOSEException, ParseException {
        return verifyAndParseToken(token, isRefresh);
    }

    @Override
    public String generateAccessToken(User user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUserId().toString())
                .issuer("tuanhiepdev")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(accessTokenDuration, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("email", user.getEmail())
                .build();
        try {
            SignedJWT signedJWT = new SignedJWT(jwsHeader, jwtClaimsSet);
            JWSSigner signer = new MACSigner(accessTokenSignerKey.getBytes());
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new AppException(ErrorCode.TOKEN_CANNOT_CREATE_EXCEPTION);
        }
    }

    @Override
    public String generateRefreshToken(User user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUserId().toString())
                .issuer("tuanhiepdev")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(refreshTokenDuration, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .build();
        try {
            SignedJWT signedJWT = new SignedJWT(jwsHeader, jwtClaimsSet);
            JWSSigner signer = new MACSigner(refreshTokenSignerKey.getBytes());
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new AppException(ErrorCode.TOKEN_CANNOT_CREATE_EXCEPTION);
        }
    }
}
