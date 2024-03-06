package com.example.user.user.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.user.user.domain.User;
import com.example.user.user.domain.UserRole;
import com.example.user.user.exception.JsonParsingException;
import com.example.user.user.exception.UnKnownProviderException;
import com.example.user.user.exception.UserNotFoundException;
import com.example.user.user.repository.UserRepository;
import com.example.user.util.oAuth.JwtProperties;
import com.example.user.util.oAuth.OauthToken;
import com.example.user.util.oAuth.profile.GitProfile;
import com.example.user.util.oAuth.profile.GoogleProfile;
import com.example.user.util.oAuth.profile.KakaoProfile;
import com.example.user.util.oAuth.profile.Profile;
import com.example.user.util.oAuth.provider.Provider;
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

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {
    private final UserRepository userRepository;
    private final WebClient webClient;
    private final ObjectMapper mapper;

    private static HttpEntity<MultiValueMap<String, String>> getTokenRequest(String code, Provider provider) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8");

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
            if (provider.equals(Provider.GIT)) {
                Map<String, String> map = new HashMap<>();
                String[] pairs = Objects.requireNonNull(tokenResponse).split("&");
                Arrays.stream(pairs)
                        .map(pair -> pair.split("=", 2))
                        .forEach(tokens -> {
                            String key = tokens[0];
                            String value = tokens[1];
                            map.put(key, value);
                        });
                tokenResponse = mapper.writeValueAsString(map);
            }
            return mapper.readValue(tokenResponse, OauthToken.class);
        } catch (JsonProcessingException e) {
            throw new JsonParsingException(e);
        }
    }

    public Mono<OauthToken> getOAuthToken(String code, String providerString) {
        Provider provider = getProvider(providerString);
        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path(provider.getRequestTokenUrl()).build())
                .bodyValue(getTokenRequest(code, provider))
                .retrieve()
                .bodyToMono(String.class)
                .map(tokenResponse -> parseToOAuthToken(provider, tokenResponse));
    }

    public Long saveUser(Mono<OauthToken> token, String providerString) {
        Provider provider = getProvider(providerString);
        HttpHeaders headers = new HttpHeaders();
        headers.add(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + token.subscribe(OauthToken::getAccess_token));
        headers.add(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8");

        Profile profile = getProfile(provider, headers);
        // 프로필 정보로 회원 조회하여 존재하면 반환, 없으면 신규 회원 생성 후 반환
        User user = findUser(profile, provider);

        // 신규 회원이면 저장 후 토큰 생성을 위한 id 반환
        if (user.getId() == null) {
            user = userRepository.save(user);
        }
        return user.getId();
    }

    private Provider getProvider(String provider) {
        try {
            return Provider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnKnownProviderException(e);
        }
    }

    private Profile getProfile(Provider provider, HttpHeaders headers) {
        return switch (provider) {
            case KAKAO -> findProfile(provider.getRequestInfoUrl(), headers, KakaoProfile.class);
            case GOOGLE -> findProfile(provider.getRequestInfoUrl(), headers, GoogleProfile.class);
            default -> findProfile(provider.getRequestInfoUrl(), headers, GitProfile.class);
        };
    }

    private <T> T findProfile(String uri, HttpHeaders headers, Class<T> type) {
        try {
            return mapper.readValue(webClient.post()
                    .uri(uri)
                    .headers(httpHeaders -> httpHeaders.addAll(headers))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(), type);
        } catch (JsonProcessingException e) {
            throw new JsonParsingException(e);
        }
    }

    private User findUser(Profile profile, Provider provider) {
        return userRepository.findByEmailAndProvider(profile.getEmail(), provider.getValue())
                .orElseGet(() ->
                        User.builder()
                                .profileImgUrl(profile.getPicture())
                                .nickname(profile.getName())
                                .email(profile.getEmail())
                                .provider(provider)
                                .userRole(UserRole.USER)
                                .description("joinBy" + provider)
                                .build());
    }

    public String createJWTToken(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        return JWT.create()
                .withSubject(user.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.EXPIRATION_TIME))
                .withClaim("id", user.getId())
                .withClaim("nickname", user.getNickname())
                .sign(Algorithm.HMAC512(JwtProperties.SECRET));
    }
}

