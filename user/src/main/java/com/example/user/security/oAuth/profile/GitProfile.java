package com.example.user.security.oAuth.profile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitProfile implements Profile {
    private Long id;
    private String connectedAt;
    private String email;
    private String name;
    private String picture;
}