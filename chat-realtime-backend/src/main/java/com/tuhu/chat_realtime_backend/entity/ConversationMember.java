package com.tuhu.chat_realtime_backend.entity;
import com.tuhu.chat_realtime_backend.constant.enums.UserConversationRole;
import com.tuhu.chat_realtime_backend.entity.id.ConversationMemberId;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "conversation_members")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ConversationMemberId.class) // Composite Key
public class ConversationMember {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserConversationRole role;
    private LocalDateTime joinedAt = LocalDateTime.now();
    private LocalDateTime leftAt;
    private UUID lastReadMessageId; // Pointer để biết đã đọc đến đâu

    private int unreadCount = 0; // Tăng lên khi có tin nhắn mới, reset về 0 khi đọc
}