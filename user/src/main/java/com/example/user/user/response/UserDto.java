package com.example.user.user.response;

import com.example.user.user.domain.UserRole;
import com.example.user.security.oAuth.provider.Provider;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class UserDto {
    private String profileImgUrl;
    private String nickname;
    private String email;
    private String description;

    private String provider;
    @Enumerated(EnumType.STRING)
    private UserRole userRole;


    @CreationTimestamp
    private Timestamp createTime;

    @Builder
    public UserDto(String profileImgUrl, String nickname, String email, String provider, UserRole userRole, String description, Timestamp createTime) {
        this.profileImgUrl = profileImgUrl;
        this.nickname = nickname;
        this.email = email;
        this.provider = provider;
        this.userRole = userRole;
        this.description = description;
        this.createTime = createTime;
    }
}
