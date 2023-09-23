package com.example.user.user.request;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String nickname;
    private String description;
}
