package com.tuhu.chat_realtime_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "invalidated_token")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvalidatedToken {

    @Id
    @Column(name = "id_token")
    private String idToken;


    @Column(name = "expiry_time", nullable = false)
    private Date expiryTime;
}
