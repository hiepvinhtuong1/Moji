package com.tuhu.chat_realtime_backend.constant.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum MessageType {
    TEXT("text"),
    IMAGE("image");

    private final String value;

    MessageType(String value) { this.value = value; }

    @JsonValue // Frontend sẽ nhận được "text" thay vì "TEXT"
    public String getValue() { return value; }
}
