package com.tuhu.chat_realtime_backend.constant.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UserConversationRole {
    ADMIN("Admin"),
    MEMBER("Member");

    private final String value;

    UserConversationRole(String value) { this.value = value; }

    @JsonValue // Frontend sẽ nhận được "text" thay vì "TEXT"
    public String getValue() { return value; }
}
