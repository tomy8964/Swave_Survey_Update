package com.example.user.util.oAuth.provider;

import com.example.user.util.oAuth.profile.Profile;
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

    Class<? extends Profile> getProfileClass();
}
