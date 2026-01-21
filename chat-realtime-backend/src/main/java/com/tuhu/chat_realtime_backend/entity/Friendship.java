package com.tuhu.chat_realtime_backend.entity;

import com.tuhu.chat_realtime_backend.constant.enums.FriendShipStatus;
import com.tuhu.chat_realtime_backend.entity.id.FriendshipId;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "friendships")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(FriendshipId.class) // Khóa chính kết hợp
// 1. Khi gọi repository.delete(), nó sẽ chạy câu SQL này
@SQLDelete(sql = "UPDATE friendships SET is_deleted = true WHERE user_id = ? AND friend_id = ?")
// 2. Mặc định tất cả các câu Select sẽ kẹp thêm điều kiện này
@SQLRestriction("is_deleted = false")
public class Friendship extends BaseEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id")
    private User friend;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendShipStatus status; // PENDING, ACCEPTED, BLOCKED

    // Người thực hiện hành động cuối (Ví dụ: ai là người gửi lời mời)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_user_id")
    private User actionUser;

    @Column(name = "is_deleted")
    private boolean deleted = false;
}
