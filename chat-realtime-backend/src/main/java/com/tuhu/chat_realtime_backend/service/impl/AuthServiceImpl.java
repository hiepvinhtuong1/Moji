package com.tuhu.chat_realtime_backend.service.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import com.tuhu.chat_realtime_backend.dto.request.LoginRequest;
import com.tuhu.chat_realtime_backend.dto.request.LogoutRequest;
import com.tuhu.chat_realtime_backend.dto.request.RefreshTokenRequest;
import com.tuhu.chat_realtime_backend.dto.request.RegisterRequest;
import com.tuhu.chat_realtime_backend.dto.response.UserResponse;
import com.tuhu.chat_realtime_backend.dto.response.auth.LoginResponse;
import com.tuhu.chat_realtime_backend.dto.response.auth.RefreshTokenResponse;
import com.tuhu.chat_realtime_backend.entity.InvalidatedToken;
import com.tuhu.chat_realtime_backend.entity.User;
import com.tuhu.chat_realtime_backend.exception.AppException;
import com.tuhu.chat_realtime_backend.exception.ErrorCode;
import com.tuhu.chat_realtime_backend.repository.InvalidatedTokenRepository;
import com.tuhu.chat_realtime_backend.repository.UserRepository;
import com.tuhu.chat_realtime_backend.service.AuthService;
import com.tuhu.chat_realtime_backend.service.JwtService;
import com.tuhu.chat_realtime_backend.utils.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    @Override
    public void register(RegisterRequest request) {
        log.info("register service called");
        // Kiem tra xem username va email da ton tai trong database chua
        boolean isCheck = userRepository.existsByUsernameOrEmail(request.getUsername(),request.getEmail());
        if (isCheck) {
            throw new AppException(ErrorCode.USERNAME_OR_EMAIL_EXISTED);
        }
        User newUser = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .displayName(request.getDisplayName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))

                .build();

        try {
            userRepository.save(newUser);
        } catch (DataIntegrityViolationException exception){
            throw new AppException(ErrorCode.USERNAME_OR_EMAIL_EXISTED);
        }
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("login service called");
        User existedUser = userRepository.findByUsername(request.getUsername()).orElseThrow(
                ()->  new AppException(ErrorCode.USERNAME_OR_PASSWORD_NOT_MATCH)
        );

        if (!passwordEncoder.matches(request.getPassword(),existedUser.getPasswordHash())){
            throw new AppException(ErrorCode.USERNAME_OR_PASSWORD_NOT_MATCH);
        }
        return LoginResponse.builder()
                .accessToken(jwtService.generateAccessToken(existedUser))
                .refreshToken(jwtService.generateRefreshToken(existedUser))
                .user(
                        UserResponse.builder()
                                .id(existedUser.getUserId().toString())
                                .username(existedUser.getUsername())
                                .displayName(existedUser.getDisplayName())
                                .email(existedUser.getEmail())
                                .createdAt(DateTimeUtils.formatLocalDateTime(existedUser.getCreatedAt()))
                                .updatedAt(DateTimeUtils.formatLocalDateTime(existedUser.getUpdatedAt()))
                                .build()
                )
                .build();
    }

    @Override
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) throws ParseException, JOSEException {
        log.info("refreshToken service called");
        //check access token is expired
        SignedJWT signedJWTAccess = jwtService.getSignedJWT(request.getAccessToken(), false);
        String userIdFromAccess = signedJWTAccess.getJWTClaimsSet().getSubject();

        //2 Check xem access token thuc su da het han chua
        if (signedJWTAccess.getJWTClaimsSet().getExpirationTime().after(new Date())){
            throw new AppException(ErrorCode.ACCESS_TOKEN_STILL_VALID);
        }

        //3 verify refresh token
        SignedJWT signedJWTRefresh = jwtService.getSignedJWT(request.getRefreshToken(),true);
        String userIdFromRefresh = signedJWTRefresh.getJWTClaimsSet().getSubject();
        Date expirationTimeRefresh = signedJWTRefresh.getJWTClaimsSet().getExpirationTime();
        String refreshTokenId = signedJWTRefresh.getJWTClaimsSet().getJWTID();
        //4 kiem tra cap token thuoc ve cung 1 user
        if (!userIdFromAccess.equals(userIdFromRefresh)){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
       //5 vo hieu hoa refresh token cu
        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .idToken(refreshTokenId)
                .expiryTime(expirationTimeRefresh)
                .build();

        invalidatedTokenRepository.save(invalidatedToken);

        User user = userRepository.findById(UUID.fromString(userIdFromRefresh))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return RefreshTokenResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();
    }

    @Override
    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        log.info("logout service called");
        SignedJWT signedJWTAccess = jwtService.getSignedJWT(request.getAccessToken(), false);
        String userIdFromAccess = signedJWTAccess.getJWTClaimsSet().getSubject();
        SignedJWT signedJWTRefresh = jwtService.getSignedJWT(request.getRefreshToken(),true);
        String userIdFromRefresh = signedJWTRefresh.getJWTClaimsSet().getSubject();

        if (!userIdFromAccess.equals(userIdFromRefresh)){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Date expiryTimeAccessToken = signedJWTAccess.getJWTClaimsSet().getExpirationTime();
        String accessTokenJWTID = signedJWTAccess.getJWTClaimsSet().getJWTID();
        Date expiryTimeRefreshToken = signedJWTRefresh.getJWTClaimsSet().getExpirationTime();
        String refreshTokenJWTID = signedJWTRefresh.getJWTClaimsSet().getJWTID();

        if (expiryTimeAccessToken.after(new Date())) {
            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .idToken(accessTokenJWTID)
                    .expiryTime(expiryTimeAccessToken)
                    .build();
            invalidatedTokenRepository.save(invalidatedToken);
        }

        if (expiryTimeRefreshToken.after(new Date())) {
            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .idToken(refreshTokenJWTID)
                    .expiryTime(expiryTimeRefreshToken)
                    .build();
            invalidatedTokenRepository.save(invalidatedToken);
        }
    }
}
