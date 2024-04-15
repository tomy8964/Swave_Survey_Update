package com.example.user.util.oAuth.provider;

import com.example.user.util.oAuth.profile.GitProfile;
import com.example.user.util.oAuth.profile.Profile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Data
public class Git implements Provider {

    private final String value = "git";
    private final String grantType = "authorization_code";
    private final String clientId = "Iv1.986aaa4d78140fb7";
    private final String clientSecret = "0c8e730012e8ca8e41a3922358572457f5cc57e4";
    private final String redirectUri = "172.16.210.80:80/oauth/callback/git";
    private final String requestTokenUrl = "github.com/login/oauth/access_token";
    private final String requestInfoUrl = "api.github.com/user";

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

    @Override
    public Class<? extends Profile> getProfileClass() {
        return GitProfile.class;
    }
}
