package com.example.user.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_master")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_Id")
    private Long id;

    private String profileImgUrl;
    private String nickname;
    private String email;
    private String provider;
    private UserRole userRole;
    private String description;

    @CreationTimestamp
    private Timestamp createTime;

    private boolean isDeleted = false;

    @Builder
    public User(String profileImgUrl, String nickname,
                String email, String provider, UserRole userRole, String description) {
        this.profileImgUrl = profileImgUrl;
        this.nickname = nickname;
        this.email = email;
        this.provider = provider;
        this.userRole = userRole;
        this.description = description;
    }

    public void updateUser(String nickname, String description) {
        this.nickname = nickname;
        this.description = description;
    }

    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

}
