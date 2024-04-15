package com.example.user.user.service;

import com.example.user.user.exception.JsonParsingException;
import com.example.user.user.exception.UnKnownProviderException;
import com.example.user.user.repository.UserRepository;
import com.example.user.util.oAuth.OauthToken;
import com.example.user.util.oAuth.profile.Profile;
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
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


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
        oAuthService = new OAuthService(webClient, mapper);
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
        OauthToken oauthToken = oAuthService.getOAuthToken(code, stringProvider).block();

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
        OauthToken oauthToken = oAuthService.getOAuthToken(code, stringProvider).block();

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
        OauthToken oauthToken = oAuthService.getOAuthToken(code, stringProvider).block();

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
        assertThrows(JsonParsingException.class, () -> oAuthService.getOAuthToken(code, stringProvider).block());
    }

    @Test
    @DisplayName("OAuthToken으로 유저 정보 조회 - git")
    void getProfile1() {
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
        Mono<OauthToken> mono = new Mono<>() {
            @Override
            public void subscribe(CoreSubscriber<? super OauthToken> actual) {
                actual.onNext(testToken);
            }
        };
        String gitProfileMockResponse = "{ \"name\": \"mock_name\", \"picture\": \"mock_picture\" }";

        mockBackEnd.enqueue(new MockResponse().setBody(gitProfileMockResponse)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // when
        Profile profile = oAuthService.getProfile(mono, stringProvider).block();

        // then
        assertEquals(profile.getName(), "mock_name");
        assertEquals(profile.getPicture(), "mock_picture");
    }

    @Test
    @DisplayName("OAuthToken으로 유저 정보 조회 - kakao")
    void getProfile2() {
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
        Mono<OauthToken> mono = new Mono<>() {
            @Override
            public void subscribe(CoreSubscriber<? super OauthToken> actual) {
                actual.onNext(testToken);
            }
        };
        String kakaoProfileMockResponse = "{ \"id\": 1, \"connectedAt\": \"2022-01-01T00:00:00Z\", " +
                "\"properties\": { \"nickname\": \"mock_name\", \"profile_image\": \"mock_picture\" }, " +
                "\"kakaoAccount\": { \"email\": \"mock_email\", \"profile\": { \"nickname\": \"mock_name\", \"profile_image_url\": \"mock_picture\" } } }";

        mockBackEnd.enqueue(new MockResponse().setBody(kakaoProfileMockResponse)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // when
        Profile profile = oAuthService.getProfile(mono, stringProvider).block();

        // then
        assertEquals(profile.getName(), "mock_name");
        assertEquals(profile.getPicture(), "mock_picture");
    }

    @Test
    @DisplayName("OAuthToken으로 유저 정보 조회 - google")
    void getProfile3() {
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
        Mono<OauthToken> mono = new Mono<>() {
            @Override
            public void subscribe(CoreSubscriber<? super OauthToken> actual) {
                actual.onNext(testToken);
            }
        };
        String googleProfileMockResponse = "{ \"name\": \"mock_name\", \"avatar_url\": \"mock_picture\" }";

        mockBackEnd.enqueue(new MockResponse().setBody(googleProfileMockResponse)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // when
        Profile profile = oAuthService.getProfile(mono, stringProvider).block();

        // then
        assertEquals(profile.getName(), "mock_name");
        assertEquals(profile.getPicture(), "mock_picture");
    }
}