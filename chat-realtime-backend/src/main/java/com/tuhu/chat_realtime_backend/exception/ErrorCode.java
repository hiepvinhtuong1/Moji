package com.tuhu.chat_realtime_backend.exception;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized Exception error!", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED(401, "Unauthenticated error!", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(403, "You do not have permission!", HttpStatus.FORBIDDEN),
    TOKEN_EXPIRED_EXCEPTION(2000,"Token was expired",HttpStatus.UNAUTHORIZED ),
    TOKEN_CANNOT_CREATE_EXCEPTION(2001,"Token can not create" ,HttpStatus.UNAUTHORIZED ),
    ACCESS_TOKEN_STILL_VALID(2002,"Access token is still valid" ,HttpStatus.BAD_REQUEST ),
    USERNAME_OR_EMAIL_EXISTED(3000,"Username or email was existed" ,HttpStatus.BAD_REQUEST ),
    USERNAME_OR_PASSWORD_NOT_MATCH(3001,"Username or password dit not match" ,HttpStatus.BAD_REQUEST ),
    USER_NOT_FOUND(3002,"User was not founded" ,HttpStatus.NOT_FOUND ),
    INVALID_KEY(3003,"Invalid key" ,HttpStatus.BAD_REQUEST );
    private final int errorCode;
    private final String message;
    private final HttpStatus httpStatusCode;

    ErrorCode(int errorCode, String message, HttpStatus httpStatusCode) {
        this.errorCode = errorCode;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}
