package com.example.user.user.service;

import com.example.user.user.exception.JsonParsingException;
import com.example.user.util.oAuth.OauthToken;
import com.example.user.util.oAuth.profile.Profile;
import com.example.user.util.oAuth.provider.Provider;
import com.example.user.util.oAuth.provider.ProviderList;
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

import static com.example.user.util.oAuth.JwtProperties.HEADER_STRING;
import static com.example.user.util.oAuth.JwtProperties.TOKEN_PREFIX;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

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
        return tokenMono.flatMap(token -> {
            Provider provider = ProviderList.findProvider(providerString);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HEADER_STRING, TOKEN_PREFIX + token.getAccess_token());
            headers.add(CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8");

            return webClient.post()
                    .uri(provider.getRequestInfoUrl())
                    .headers(httpHeaders -> httpHeaders.addAll(headers))
                    .retrieve()
                    .bodyToMono(provider.getProfileClass())
                    .onErrorMap(JsonProcessingException.class, JsonParsingException::new);
        });
    }

    private HttpEntity<MultiValueMap<String, String>> getTokenRequest(String code, Provider provider) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", provider.getGrantType());
        params.add("client_id", provider.getClientId());
        params.add("redirect_uri", provider.getRedirectUri());
        params.add("code", code);
        params.add("client_secret", provider.getClientSecret());

        return new HttpEntity<>(params, headers);
    }

    private OauthToken parseToOAuthToken(Provider provider, String tokenResponse) {
        try {
            return mapper.readValue(provider.getTokenResponse(tokenResponse), OauthToken.class);
        } catch (JsonProcessingException e) {
            throw new JsonParsingException(e);
        }
    }
}

