package com.example.user.security.oAuth.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Data
@Configuration
@ConfigurationProperties(prefix = "oauth.git")
public class Git implements Provider {
    private String value;
    private String grantType = "authorization_code";
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String requestTokenUrl;
    private String requestInfoUrl;

    @Override
    public String getTokenResponse(String tokenResponse) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map = new HashMap<>();
        String[] pairs = Objects.requireNonNull(tokenResponse).split("&");
        Arrays.stream(pairs)
                .map(pair -> pair.split("=", 2))
                .forEach(tokens -> {
                    String key = tokens[0];
                    String value = tokens[1];
                    map.put(key, value);
                });
        return mapper.writeValueAsString(map);
    }
}
