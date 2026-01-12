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
@EqualsAndHashCode // Bắt buộc phải có để JPA so sánh các khóa
public class FriendshipId implements Serializable {
    private UUID user;   // Tên field phải khớp với tên field trong Entity Friendship
    private UUID friend;
}
