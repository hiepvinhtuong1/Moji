package com.tuhu.chat_realtime_backend.constant.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FriendShipStatus {
    PENDING("Pending"),
    ACCEPTED("Accepted"),
    REMOVED("Removed"),
    BLOCKED("Blocked");

    private final String value;

    FriendShipStatus(String value) { this.value = value; }

    @JsonValue
    public String getValue() { return value; }
}
