package com.example.user.security.oAuth.profile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleProfile implements Profile {
    private Long id;
    private String connectedAt;
    private String email;
    private String name;
    @JsonProperty("avatar_url")
    private String picture;
}