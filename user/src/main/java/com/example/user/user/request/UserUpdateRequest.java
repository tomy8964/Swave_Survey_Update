package com.example.user.user.request;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserUpdateRequest {
    private String nickname;
    private String description;

    @Builder
    public UserUpdateRequest(String nickname, String description) {
        this.nickname = nickname;
        this.description = description;
    }
}
