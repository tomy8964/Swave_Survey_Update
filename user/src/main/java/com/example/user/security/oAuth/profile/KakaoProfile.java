package com.example.user.security.oAuth.profile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoProfile implements Profile {
    private Long id;
    private String connectedAt;
    private Properties properties;
    private KakaoAccount kakaoAccount;

    @Override
    public String getEmail() {
        return kakaoAccount.getEmail();
    }

    @Override
    public String getName() {
        return kakaoAccount.profile.nickname;
    }

    @Override
    public String getPicture() {
        return kakaoAccount.profile.profile_image_url;
    }

    @Data
    public static class Properties {
        public String nickname;
        public String profile_image; // 이미지 경로 필드1
        public String thumbnail_image;
    }

    @Data
    public static class KakaoAccount {
        public Boolean profile_nickname_needs_agreement;
        public Boolean profile_image_needs_agreement;
        public Profile profile;
        public Boolean has_email;
        public Boolean email_needs_agreement;
        public Boolean is_email_valid;
        public Boolean is_email_verified;
        public String email;

        @Data
        public static class Profile {
            public String nickname;
            public String thumbnail_image_url;
            public String profile_image_url; // 이미지 경로 필드2
            public Boolean is_default_image;
        }
    }
}