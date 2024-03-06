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
import com.example.user.util.oAuth.provider.Provider;
import com.example.user.util.web.WebClientConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class OAuthServiceUnitTest {
    private static MockWebServer mockBackEnd;

    @Mock
    private UserRepository userRepository;

    private ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    private OAuthService oAuthService;

    @BeforeEach
    void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();

        String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .filter(WebClientConfig.logRequest())
                .build();
        oAuthService = new OAuthService(userRepository, webClient, mapper);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    @DisplayName("응답으로 받은 토큰을 OAuthToken으로 파싱 성공 - git")
    void getOAuthTokenSuccess1() {
        // given
        String stringProvider = "git";
        String tokenResponse = "access_token=test_access&token_type=test_type&refresh_token=test_refresh&expires_in=3600&scope=test_scope&refresh_token_expires_in=7200";
        String code = "test_code";
        mockBackEnd.enqueue(new MockResponse().setBody(tokenResponse)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // when
        OauthToken oauthToken = oAuthService.getOAuthToken(code, stringProvider);

        // then
        assertEquals("test_access", oauthToken.getAccess_token());
        assertEquals("test_type", oauthToken.getToken_type());
        assertEquals("test_refresh", oauthToken.getRefresh_token());
        assertEquals(3600, oauthToken.getExpires_in());
        assertEquals("test_scope", oauthToken.getScope());
        assertEquals(7200, oauthToken.getRefresh_token_expires_in());
    }

    @Test
    @DisplayName("응답으로 받은 토큰을 OAuthToken으로 파싱 성공 - kakao")
    void getOAuthTokenSuccess2() {
        // given
        String stringProvider = "kakao";
        String tokenResponse = "{\"access_token\":\"test_access\",\"refresh_token_expires_in\":\"7200\",\"refresh_token\":\"test_refresh\",\"scope\":\"test_scope\",\"token_type\":\"test_type\",\"expires_in\":\"3600\"}";
        String code = "test_code";
        mockBackEnd.enqueue(new MockResponse().setBody(tokenResponse)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // when
        OauthToken oauthToken = oAuthService.getOAuthToken(code, stringProvider);

        // then
        assertEquals("test_access", oauthToken.getAccess_token());
        assertEquals("test_type", oauthToken.getToken_type());
        assertEquals("test_refresh", oauthToken.getRefresh_token());
        assertEquals(3600, oauthToken.getExpires_in());
        assertEquals("test_scope", oauthToken.getScope());
        assertEquals(7200, oauthToken.getRefresh_token_expires_in());
    }

    @Test
    @DisplayName("응답으로 받은 토큰을 OAuthToken으로 파싱 성공 - google")
    void getOAuthTokenSuccess3() {
        // given
        String stringProvider = "google";
        String tokenResponse = "{\"access_token\":\"test_access\",\"refresh_token_expires_in\":\"7200\",\"refresh_token\":\"test_refresh\",\"scope\":\"test_scope\",\"token_type\":\"test_type\",\"expires_in\":\"3600\"}";
        String code = "test_code";
        mockBackEnd.enqueue(new MockResponse().setBody(tokenResponse)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // when
        OauthToken oauthToken = oAuthService.getOAuthToken(code, stringProvider);

        // then
        assertEquals("test_access", oauthToken.getAccess_token());
        assertEquals("test_type", oauthToken.getToken_type());
        assertEquals("test_refresh", oauthToken.getRefresh_token());
        assertEquals(3600, oauthToken.getExpires_in());
        assertEquals("test_scope", oauthToken.getScope());
        assertEquals(7200, oauthToken.getRefresh_token_expires_in());
    }

    @Test
    @DisplayName("응답으로 받은 토큰을 OAuthToken으로 파싱 실패 - 알 수 없는 Provider")
    void getOAuthTokenFail1() {
        // given
        String stringProvider = "test_provider";
        String tokenResponse = "access_token=test_access&token_type=test_type&refresh_token=test_refresh&expires_in=3600&scope=test_scope&refresh_token_expires_in=7200";
        String code = "test_code";
        mockBackEnd.enqueue(new MockResponse().setBody(tokenResponse)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // when & then
        assertThrows(UnKnownProviderException.class, () -> oAuthService.getOAuthToken(code, stringProvider));
    }

    @Test
    @DisplayName("응답으로 받은 토큰을 OAuthToken으로 파싱 실패 - JSON 파싱 실패")
    void getOAuthTokenFail2() {
        // given
        String stringProvider = "git";
        String tokenResponse = "invaildToken=test_access";
        String code = "test_code";
        mockBackEnd.enqueue(new MockResponse().setBody(tokenResponse)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // when & then
        assertThrows(JsonParsingException.class, () -> oAuthService.getOAuthToken(code, stringProvider));
    }

    @Test
    @DisplayName("OAuthToken으로 유저 신규 가입 성공 - git")
    void saveUser1() {
        // given
        String stringProvider = "git";
        OauthToken testToken = OauthToken.builder()
                .access_token("test_access")
                .token_type("test_type")
                .refresh_token("test_refresh")
                .expires_in(3600)
                .scope("test_scope")
                .refresh_token_expires_in(7200)
                .build();
        String gitProfileMockResponse = "{ \"name\": \"mock_name\", \"avatar_url\": \"mock_picture\" }";

        User user = User.builder()
                .id(1L)
                .profileImgUrl("mock_picture")
                .nickname("mock_name")
                .email("mock_email")
                .provider(Provider.GIT)
                .userRole(UserRole.USER)
                .description("joinBy" + "git")
                .build();

        mockBackEnd.enqueue(new MockResponse().setBody(gitProfileMockResponse)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
        when(userRepository.findByEmailAndProvider(any(), any())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(user);

        // when
        Long savedUserId = oAuthService.saveUser(testToken, stringProvider);

        // then
        assertEquals(1L, savedUserId);
    }

    @Test
    @DisplayName("OAuthToken으로 유저 신규 가입 성공 - kakao")
    void saveUser2() {
        // given
        String stringProvider = "kakao";
        OauthToken testToken = OauthToken.builder()
                .access_token("test_access")
                .token_type("test_type")
                .refresh_token("test_refresh")
                .expires_in(3600)
                .scope("test_scope")
                .refresh_token_expires_in(7200)
                .build();
        String kakaoProfileMockResponse = "{ \"id\": 1, \"connectedAt\": \"2022-01-01T00:00:00Z\", " +
                "\"properties\": { \"nickname\": \"mock_name\", \"profile_image\": \"mock_picture\" }, " +
                "\"kakaoAccount\": { \"email\": \"mock_email\", \"profile\": { \"nickname\": \"mock_name\", \"profile_image_url\": \"mock_picture\" } } }";

        User user = User.builder()
                .id(1L)
                .profileImgUrl("mock_picture")
                .nickname("mock_name")
                .email("mock_email")
                .provider(Provider.GIT)
                .userRole(UserRole.USER)
                .description("joinBy" + "git")
                .build();

        mockBackEnd.enqueue(new MockResponse().setBody(kakaoProfileMockResponse)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
        when(userRepository.findByEmailAndProvider(any(), any())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(user);

        // when
        Long savedUserId = oAuthService.saveUser(testToken, stringProvider);

        // then
        assertEquals(1L, savedUserId);
    }

    @Test
    @DisplayName("OAuthToken으로 유저 신규 가입 성공 - google")
    void saveUser3() {
        // given
        String stringProvider = "google";
        OauthToken testToken = OauthToken.builder()
                .access_token("test_access")
                .token_type("test_type")
                .refresh_token("test_refresh")
                .expires_in(3600)
                .scope("test_scope")
                .refresh_token_expires_in(7200)
                .build();
        String googleProfileMockResponse = "{ \"name\": \"mock_name\", \"avatar_url\": \"mock_picture\" }";

        User user = User.builder()
                .id(1L)
                .profileImgUrl("mock_picture")
                .nickname("mock_name")
                .email("mock_email")
                .provider(Provider.GIT)
                .userRole(UserRole.USER)
                .description("joinBy" + "git")
                .build();

        mockBackEnd.enqueue(new MockResponse().setBody(googleProfileMockResponse)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
        when(userRepository.findByEmailAndProvider(any(), any())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(user);

        // when
        Long savedUserId = oAuthService.saveUser(testToken, stringProvider);

        // then
        assertEquals(1L, savedUserId);
    }

    @Test
    @DisplayName("OAuthToken으로 기존 유저 로그인 성공")
    void loginUser() {
        // given
        String stringProvider = "google";
        OauthToken testToken = OauthToken.builder()
                .access_token("test_access")
                .token_type("test_type")
                .refresh_token("test_refresh")
                .expires_in(3600)
                .scope("test_scope")
                .refresh_token_expires_in(7200)
                .build();
        String googleProfileMockResponse = "{ \"name\": \"mock_name\", \"avatar_url\": \"mock_picture\" }";

        User user = User.builder()
                .id(1L)
                .profileImgUrl("mock_picture")
                .nickname("mock_name")
                .email("mock_email")
                .provider(Provider.GIT)
                .userRole(UserRole.USER)
                .description("joinBy" + "git")
                .build();

        mockBackEnd.enqueue(new MockResponse().setBody(googleProfileMockResponse)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
        when(userRepository.findByEmailAndProvider(any(), any())).thenReturn(Optional.of(user));

        // when
        Long savedUserId = oAuthService.saveUser(testToken, stringProvider);

        // then
        assertEquals(1L, savedUserId);
    }

    @Test
    @DisplayName("OAuthToken으로 기존 유저 로그인 실패 - 알 수 없는 Provider")
    void saveUserFail1() {
        // given
        String stringProvider = "test_provider";
        OauthToken testToken = OauthToken.builder()
                .access_token("test_access")
                .token_type("test_type")
                .refresh_token("test_refresh")
                .expires_in(3600)
                .scope("test_scope")
                .refresh_token_expires_in(7200)
                .build();

        // when & then
        assertThrows(UnKnownProviderException.class, () -> oAuthService.saveUser(testToken, stringProvider));
    }

    @Test
    @DisplayName("OAuthToken으로 기존 유저 로그인 실패 - JSON 파싱 실패")
    void saveUserFail2() {
        String stringProvider = "git";
        OauthToken testToken = OauthToken.builder()
                .access_token("test_access")
                .token_type("test_type")
                .refresh_token("test_refresh")
                .expires_in(3600)
                .scope("test_scope")
                .refresh_token_expires_in(7200)
                .build();
        String gitProfileMockResponse = "Invalid JSON String";

        mockBackEnd.enqueue(new MockResponse().setBody(gitProfileMockResponse)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // when & then
        assertThrows(JsonParsingException.class, () -> oAuthService.saveUser(testToken, stringProvider));
    }

    @Test
    @DisplayName("JWT 토큰 생성 성공")
    void createJWTTokenSuccess() {
        // given
        User user = User.builder()
                .id(1L)
                .profileImgUrl("mock_picture")
                .nickname("mock_name")
                .email("mock_email")
                .provider(Provider.GIT)
                .userRole(UserRole.USER)
                .description("joinBy" + "git")
                .build();
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        // when
        String jwtToken = oAuthService.createJWTToken(user.getId());
        Long id = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(jwtToken).getClaim("id").asLong();
        String nickname = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(jwtToken).getClaim("nickname").asString();

        // then
        assertEquals(user.getNickname(), nickname);
        assertEquals(user.getId(), id);
    }

    @Test
    @DisplayName("JWT 토큰 생성 실패 - 존재 하지 않는 유저")
    void createJWTTokenFail() {
        // given
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        // when & then
        assertThrows(UserNotFoundException.class, () -> oAuthService.createJWTToken(2L));
    }
}