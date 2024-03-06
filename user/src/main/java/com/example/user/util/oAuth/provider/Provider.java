package com.example.user.util.oAuth.provider;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Provider {
    KAKAO("kakao", "authorization_code",
            "4646a32b25c060e42407ceb8c13ef14a",
            "AWyAH1M24R9EYfUjJ1KCxcsh3DwvK8F7",
            "https://172.16.210.80:80/oauth/callback/kakao",
            "kauth.kakao.com/oauth/token",
            "kapi.kakao.com/v2/user/me"),
    GOOGLE("google", "authorization_code",
            "278703087355-limdvm0almc07ldn934on122iorpfdv5.apps.googleusercontent.com",
            "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",
            "172.16.210.80:80/oauth/callback/google",
            "oauth2.googleapis.com/token",
            "www.googleapis.com/oauth2/v3/userinfo"),
    GIT("git", "authorization_code",
            "Iv1.986aaa4d78140fb7",
            "0c8e730012e8ca8e41a3922358572457f5cc57e4",
            "172.16.210.80:80/oauth/callback/git",
            "github.com/login/oauth/access_token",
            "api.github.com/user");

    private final String value;
    private final String grantType;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String requestTokenUrl;
    private final String requestInfoUrl;
}
