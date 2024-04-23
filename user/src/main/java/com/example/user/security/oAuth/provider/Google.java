package com.example.user.security.oAuth.provider;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "oauth.google")
public class Google implements Provider {
    private String value;
    private String grantType = "authorization_code";
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String requestTokenUrl;
    private String requestInfoUrl;

    @Override
    public String getTokenResponse(String tokenResponse) {
        return tokenResponse;
    }
}
