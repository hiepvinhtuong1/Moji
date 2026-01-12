package com.tuhu.chat_realtime_backend.entity.id;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ConversationMemberId implements Serializable {
    private UUID conversation; // Khớp với field trong Entity ConversationMember
    private UUID user;
}
