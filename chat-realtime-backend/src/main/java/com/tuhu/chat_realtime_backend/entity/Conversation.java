package com.tuhu.chat_realtime_backend.entity;

import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "conversations")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Conversation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID conversationId;

    private String title; // null nếu là chat 1-1
    private boolean isGroup = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
    private List<ConversationMember> members;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt; // Cực kỳ quan trọng để Sort

    @Column(name = "last_message_content", columnDefinition = "TEXT")
    private String lastMessageContent; // Preview tin nhắn

    @Column(name = "last_message_sender_id")
    private UUID lastMessageSenderId; // Để hiển thị "Bạn: ..."
}
