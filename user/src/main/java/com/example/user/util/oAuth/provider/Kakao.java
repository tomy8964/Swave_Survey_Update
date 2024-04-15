package com.example.user.util.oAuth.provider;

import com.example.user.util.oAuth.profile.KakaoProfile;
import com.example.user.util.oAuth.profile.Profile;
import lombok.Data;

@Data
public class Kakao implements Provider {
    private final String value = "kakao";
    private final String grantType = "authorization_code";
    private final String clientId = "4646a32b25c060e42407ceb8c13ef14a";
    private final String clientSecret = "AWyAH1M24R9EYfUjJ1KCxcsh3DwvK8F7";
    private final String redirectUri = "https://172.16.210.80:80/oauth/callback/kakao";
    private final String requestTokenUrl = "kauth.kakao.com/oauth/token";
    private final String requestInfoUrl = "kapi.kakao.com/v2/user/me";

    @Override
    public String getTokenResponse(String tokenResponse) {
        return tokenResponse;
    }

    @Override
    public Class<? extends Profile> getProfileClass() {
        return KakaoProfile.class;
    }
}
