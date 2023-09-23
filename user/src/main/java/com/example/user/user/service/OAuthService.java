package com.example.user.user.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.user.restAPI.service.OuterRestApiUserService;
import com.example.user.user.domain.User;
import com.example.user.user.domain.UserRole;
import com.example.user.user.exception.UserNotFoundException;
import com.example.user.user.repository.UserRepository;
import com.example.user.util.oAuth.JwtProperties;
import com.example.user.util.oAuth.OauthToken;
import com.example.user.util.oAuth.git.GitProfile;
import com.example.user.util.oAuth.google.GoogleProfile;
import com.example.user.util.oAuth.kakao.KakaoProfile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class OAuthService {

    private final UserRepository userRepository;
    private final OuterRestApiUserService apiUserService;
    private RestTemplate rt;

    public OAuthService(UserRepository userRepository, OuterRestApiUserService apiUserService) {
        this.userRepository = userRepository;
        this.apiUserService = apiUserService;
    }

    public void setRestTemplate(RestTemplate rt) {
        this.rt = rt;
    }

    public OauthToken getAccessToken(String code, String provider) {
        HttpEntity<MultiValueMap<String, String>> tokenRequest = getTokenRequest(code, provider);

        ResponseEntity<String> tokenResponse = rt.exchange(
                getProviderTokenUrl(provider),
                HttpMethod.POST,
                tokenRequest,
                String.class
        );

        return getOAuthToken(provider, tokenResponse);
    }

    public Long SaveUser(String token, String provider) {
        User user = null;

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token); //(1-4)
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        switch (provider) {
            case "kakao" -> {
                KakaoProfile profile = findKakaoProfile(headers);

                user = userRepository.findByEmailAndProvider(profile.getKakao_account().getEmail(), provider)
                        .orElseGet(() ->
                                User.builder()
                                        .profileImgUrl(profile.getKakao_account().getProfile().getProfile_image_url())
                                        .nickname(profile.getKakao_account().getProfile().getNickname())
                                        .email(profile.getKakao_account().getEmail())
                                        .provider(provider)
                                        .description("joinByKakao")
                                        .userRole(UserRole.USER)
                                        .build());
            }
            case "google" -> {
                GoogleProfile profile = findGoogleProfile(headers);

                user = userRepository.findByEmailAndProvider(profile.getEmail(), provider)
                        .orElseGet(() ->
                                User.builder()
                                        .profileImgUrl(profile.getPicture())
                                        .nickname(profile.getName())
                                        .email(profile.getEmail())
                                        .provider(provider)
                                        .description("joinByGoogle")
                                        .userRole(UserRole.USER).build());
            }
            case "git" -> {
                GitProfile profile = findGitProfile(headers);
                try {
                    JSONParser parser = new JSONParser();
                    String strJson = profile.getEmail();
                    JSONArray emails = (JSONArray) parser.parse(strJson);
                    JSONObject value = (JSONObject) emails.get(0);

                    String email = (String) value.get("email");

                    user = userRepository.findByEmailAndProvider(email, provider)
                            .orElseGet(() ->
                                    User.builder()
                                            .profileImgUrl(profile.getPicture())
                                            .nickname(profile.getName())
                                            .email(email)
                                            .provider(provider)
                                            .userRole(UserRole.USER)
                                            .description("joinByGit")
                                            .build());
                } catch (ParseException e) {
                    throw new RuntimeException("JSON Parsing ERROR", e);
                }
            }
        }

        User savedUser = userRepository.save(Objects.requireNonNull(user));
        // Document 에 알리기
        apiUserService.sendUserToSurveyDocument(savedUser.getId());
        //기존 회원이면 저장 건너뛰고 토큰 생성
        return savedUser.getId();
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

    private static OauthToken getOAuthToken(String provider, ResponseEntity<String> tokenResponse) {
        ObjectMapper objectMapper = new ObjectMapper();
        OauthToken oauthToken;
        try {
            if (provider.equals("git")) {
                // git은 body가 문자열 형식
                String responseBody = tokenResponse.getBody();
                // 문자열 파싱하여 Map 객체 생성
                Map<String, String> map = new HashMap<>();
                String[] pairs = Objects.requireNonNull(responseBody).split("&");
                Arrays.stream(pairs).map(pair -> pair.split("=", 2)).forEach(tokens -> {
                    String key = tokens[0];
                    String value = tokens.length == 2 ? tokens[1] : "";
                    map.put(key, value);
                });
                // Map 객체를 JSON 형태로 변환
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(map);

                oauthToken = objectMapper.readValue(json, OauthToken.class);
            } else {
                oauthToken = objectMapper.readValue(tokenResponse.getBody(), OauthToken.class);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Json Parsing Error", e);
        }
        return oauthToken;
    }

    private static HttpEntity<MultiValueMap<String, String>> getTokenRequest(String code, String provider) {
        String grantType;
        String clientId;
        String clientSecret;
        String redirectUri;

        switch (provider) {
            case "kakao" -> {
                grantType = "authorization_code";
                clientId = "4646a32b25c060e42407ceb8c13ef14a";
                clientSecret = "AWyAH1M24R9EYfUjJ1KCxcsh3DwvK8F7";
                redirectUri = "http://172.16.210.80:80/oauth/callback/kakao";
            }
            case "google" -> {
                grantType = "authorization_code";
                clientId = "278703087355-limdvm0almc07ldn934on122iorpfdv5.apps.googleusercontent.com";
                clientSecret = "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";
                redirectUri = "http://172.16.210.80:80/oauth/callback/google";
            }
            case "git" -> {
                grantType = "authorization_code";
                clientId = "Iv1.986aaa4d78140fb7";
                clientSecret = "0c8e730012e8ca8e41a3922358572457f5cc57e4";
                redirectUri = "http://172.16.210.80:80/oauth/callback/git";
            }
            default -> throw new IllegalArgumentException("Invalid Provider: " + provider);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", grantType);
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);
        params.add("client_secret", clientSecret);
        return new HttpEntity<>(params, headers);
    }

    //provider에 따라 URL 제공 구분
    private String getProviderTokenUrl(String provider) {
        return switch (provider) {
            case "kakao" -> "https://kauth.kakao.com/oauth/token";
            case "google" -> "https://oauth2.googleapis.com/token";
            case "git" -> "https://github.com/login/oauth/access_token";
            default -> throw new IllegalArgumentException("Invalid Provider: " + provider);
        };
    }

    private KakaoProfile findKakaoProfile(HttpHeaders headers) {

        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest =
                new HttpEntity<>(headers);

        // Http 요청 (POST 방식) 후, response 변수에 응답을 받음
        ResponseEntity<String> kakaoProfileResponse = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoProfileRequest,
                String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        KakaoProfile kakaoProfile;

        try {
            kakaoProfile = objectMapper.readValue(kakaoProfileResponse.getBody(), KakaoProfile.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return kakaoProfile;
    }

    private GoogleProfile findGoogleProfile(HttpHeaders headers) {

        HttpEntity<MultiValueMap<String, String>> googleProfileRequest =
                new HttpEntity<>(headers);

        // Http 요청 (POST 방식) 후, response 변수에 응답을 받음
        ResponseEntity<String> googleProfileResponse = rt.exchange(
                "https://www.googleapis.com/oauth2/v3/userinfo",
                HttpMethod.POST,
                googleProfileRequest,
                String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        GoogleProfile googleProfile;
        try {
            googleProfile = objectMapper.readValue(googleProfileResponse.getBody(), GoogleProfile.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return googleProfile;
    }

    private GitProfile findGitProfile(HttpHeaders headers) {

        HttpEntity<MultiValueMap<String, String>> gitProfileRequest =
                new HttpEntity<>(headers);
        //spring oauth access token gitHub server userinfo
        // Http 요청 (POST 방식) 후, response 변수에 응답을 받음
        ResponseEntity<String> gitProfileResponse = rt.exchange(
                "https://api.github.com/user",
                HttpMethod.GET,
                gitProfileRequest,
                String.class
        );
        String name;
        String picture;
        try {
            JSONParser parser = new JSONParser();
            String userInfo = gitProfileResponse.getBody();
            JSONObject jsonObject = (JSONObject) parser.parse(userInfo);
            name = (String) jsonObject.get("login");
            picture = (String) jsonObject.get("avatar_url");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }


        //Git은 email 정보를 다시 한번 받아와야 함
        ResponseEntity<String> gitEmailResponse = rt.exchange(
                "https://api.github.com/user/emails",
                HttpMethod.GET,
                gitProfileRequest,
                String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        GitProfile gitProfile;
        try {
            gitProfile = objectMapper.readValue(gitProfileResponse.getBody(), GitProfile.class);

            gitProfile.email = gitEmailResponse.getBody();
            gitProfile.name = name;
            gitProfile.picture = picture;

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return gitProfile;
    }
}

