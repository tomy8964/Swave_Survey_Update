package com.example.user.util.oAuth.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // sub 필드를 무시하도록 설정
public class GoogleProfile {

    public Long id;
    public String connected_at;
    public String email;
    public String name;
    public String picture;

}