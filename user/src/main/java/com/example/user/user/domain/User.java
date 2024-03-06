package com.example.user.user.domain;

import com.example.user.user.response.UserDto;
import com.example.user.util.oAuth.provider.Provider;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.sql.Timestamp;

@Entity
@Getter
@Where(clause = "is_deleted = false")
@SQLDelete(sql = "UPDATE user SET is_deleted = true where user_id = ?")
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
    private String description;

    @Enumerated(EnumType.STRING)
    private Provider provider;
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @JsonProperty("deleted")
    private boolean isDeleted = false;

    @CreationTimestamp
    private Timestamp createTime;


    @Builder
    public User(Long id, String profileImgUrl, String nickname,
                String email, Provider provider, UserRole userRole, String description) {
        this.id = id;
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

    public UserDto toUserDto(User user) {
        return UserDto.builder()
                .profileImgUrl(user.getProfileImgUrl())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .provider(user.getProvider())
                .userRole(user.getUserRole())
                .description(user.getDescription())
                .createTime(user.getCreateTime())
                .build();
    }
}
