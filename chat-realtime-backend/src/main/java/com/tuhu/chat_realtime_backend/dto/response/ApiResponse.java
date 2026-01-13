package com.tuhu.chat_realtime_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    int statusCode;
    String message;
    T data;

    @Builder
    public ApiResponse(int statusCode, String message, T data) {
        this.statusCode = (statusCode != 0) ? statusCode : 1000;
        this.message = message;
        this.data = data;
    }

}

