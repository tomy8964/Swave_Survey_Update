package com.example.user.util.oAuth.provider;

import com.example.user.util.oAuth.profile.GoogleProfile;
import com.example.user.util.oAuth.profile.Profile;
import lombok.Data;

@Data
public class Google implements Provider {
    private final String value = "google";
    private final String grantType = "authorization_code";
    private final String clientId = "278703087355-limdvm0almc07ldn934on122iorpfdv5.apps.googleusercontent.com";
    private final String clientSecret = "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";
    private final String redirectUri = "172.16.210.80:80/oauth/callback/google";
    private final String requestTokenUrl = "oauth2.googleapis.com/token";
    private final String requestInfoUrl = "www.googleapis.com/oauth2/v3/userinfo";

    @Override
    public String getTokenResponse(String tokenResponse) {
        return tokenResponse;
    }

    @Override
    public Class<? extends Profile> getProfileClass() {
        return GoogleProfile.class;
    }
}
