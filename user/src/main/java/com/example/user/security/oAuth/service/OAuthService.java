package com.example.user.security.oAuth.service;

import com.example.user.security.oAuth.OauthToken;
import com.example.user.security.oAuth.profile.Profile;
import com.example.user.security.oAuth.profile.ProfileList;
import com.example.user.security.oAuth.provider.Provider;
import com.example.user.security.oAuth.provider.ProviderList;
import com.example.user.user.exception.JsonParsingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static com.example.user.security.jwt.JwtRequestFilter.HEADER_STRING;
import static com.example.user.security.jwt.JwtRequestFilter.TOKEN_PREFIX;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {
    private final WebClient webClient;
    private final ObjectMapper mapper;

    public Mono<OauthToken> getOAuthToken(String code, String providerString) {
        Provider provider = ProviderList.findProvider(providerString);
        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path(provider.getRequestTokenUrl()).build())
                .bodyValue(getTokenRequest(code, provider))
                .retrieve()
                .bodyToMono(String.class)
                .map(tokenResponse -> parseToOAuthToken(provider, tokenResponse));
    }

    public Mono<Profile> getProfile(Mono<OauthToken> tokenMono, String providerString) {
        Provider provider = ProviderList.findProvider(providerString);
        return tokenMono.flatMap(token ->
                webClient.post()
                        .uri(provider.getRequestInfoUrl())
                        .headers(httpHeaders -> createHeaders(Optional.of(token.getAccess_token())))
                        .retrieve()
                        .bodyToMono(ProfileList.findProfile(provider.getValue()).getClass())
                        .onErrorMap(JsonProcessingException.class, JsonParsingException::new));
    }

    private HttpEntity<MultiValueMap<String, String>> getTokenRequest(String code, Provider provider) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", provider.getGrantType());
        params.add("client_id", provider.getClientId());
        params.add("redirect_uri", provider.getRedirectUri());
        params.add("code", code);
        params.add("client_secret", provider.getClientSecret());

        return new HttpEntity<>(params, createHeaders(Optional.empty()));
    }

    private OauthToken parseToOAuthToken(Provider provider, String tokenResponse) {
        try {
            return mapper.readValue(provider.getTokenResponse(tokenResponse), OauthToken.class);
        } catch (JsonProcessingException e) {
            throw new JsonParsingException(e);
        }
    }

    private HttpHeaders createHeaders(Optional<String> accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_FORM_URLENCODED);
        accessToken.ifPresent(token -> headers.add(HEADER_STRING, TOKEN_PREFIX + token));
        return headers;
    }
}

