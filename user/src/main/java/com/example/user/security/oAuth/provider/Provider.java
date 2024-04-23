package com.example.user.security.oAuth.provider;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface Provider {

    String getRequestTokenUrl();

    String getGrantType();

    String getClientId();

    String getRedirectUri();

    String getClientSecret();

    String getTokenResponse(String tokenResponse) throws JsonProcessingException;

    String getValue();

    String getRequestInfoUrl();
}
