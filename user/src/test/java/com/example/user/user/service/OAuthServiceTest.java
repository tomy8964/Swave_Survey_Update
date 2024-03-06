//package com.example.user.user.service;
//
//import com.example.user.user.domain.User;
//import com.example.user.user.domain.UserRole;
//import com.example.user.user.repository.UserRepository;
//import com.example.user.util.oAuth.OauthToken;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.Mockito.*;
//
//@ActiveProfiles("test")
//@Transactional
//@SpringBootTest
//public class OAuthServiceTest {
//
//    private static final String getMockGitProfile1 = "{\"login\": \"sampleName\", \"avatar_url\": \"samplePicture\"}";
//    @InjectMocks
//    private OAuthService oAuthService;
//    @Autowired
//    private UserRepository userRepository;
//    @Mock
//    private RestTemplate rt;
//
//    private static String getMockKakaoProfile() {
//        return "{"
//                + "\"id\": 12345,"
//                + "\"connected_at\": \"2023-09-23T10:00:00Z\","
//                + "\"properties\": {"
//                + "\"nickname\": \"sampleNickname\","
//                + "\"profile_image\": \"sampleProfileImage\","
//                + "\"thumbnail_image\": \"sampleThumbnailImage\""
//                + "},"
//                + "\"kakao_account\": {"
//                + "\"profile_nickname_needs_agreement\": false,"
//                + "\"profile_image_needs_agreement\": false,"
//                + "\"profile\": {"
//                + "\"nickname\": \"sampleNickname\","
//                + "\"thumbnail_image_url\": \"sampleThumbnailImageUrl\","
//                + "\"profile_image_url\": \"sampleProfileImageUrl\","
//                + "\"is_default_image\": false"
//                + "},"
//                + "\"has_email\": true,"
//                + "\"email_needs_agreement\": false,"
//                + "\"is_email_valid\": true,"
//                + "\"is_email_verified\": true,"
//                + "\"email\": \"sample@example.com\""
//                + "}"
//                + "}";
//    }
//
//    private static String getMockGoogleProfile() {
//        return "{"
//                + "\"id\": 12345,"
//                + "\"connected_at\": \"2023-09-23T10:00:00Z\","
//                + "\"email\": \"sample@example.com\","
//                + "\"name\": \"sampleName\","
//                + "\"picture\": \"samplePicture\""
//                + "}";
//    }
//
//    private static String getMockGitProfile2() {
//        return """
//                [
//                  {
//                    "email": "sample@example.com",
//                    "verified": true,
//                    "primary": true,
//                    "visibility": "public"
//                  },
//                  {
//                    "email": "another@example.com",
//                    "verified": true,
//                    "primary": false,
//                    "visibility": "private"
//                  }
//                ]""";
//    }
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this); // Initialize mocks
//
//        // Create an instance of OAuthService with mocked dependencies
//        oAuthService = new OAuthService(userRepository);
//        oAuthService.setRestTemplate(rt); // Inject the mocked RestTemplate
//    }
//
//    @Test
//    @DisplayName("JSON PARSING ERROR1")
//    public void JSON_PARSING_ERROR1() {
//        assertThrows(RuntimeException.class, () -> oAuthService.saveUser("code", "git"));
//    }
//
//    @Test
//    @DisplayName("깃 새로운 유저 가입시 JSON PARSING ERROR2")
//    void JSON_PARSING_ERROR2() {
//        // given
//        String code = "mockCode";
//        String provider = "git";
//        String tokenResponseJson = "invaild token";
//
//        ResponseEntity<String> mockResponseEntity = ResponseEntity.ok(tokenResponseJson);
//        when(rt.exchange(
//                anyString(),
//                eq(HttpMethod.POST),
//                any(HttpEntity.class),
//                eq(String.class)
//        )).thenReturn(mockResponseEntity);
//
//        assertThrows(RuntimeException.class, () -> oAuthService.getAccessToken(code, provider));
//    }
//
//    @Test
//    @DisplayName("Invalid Provider ERROR3")
//    public void Invalid_Provider_ERROR1() {
//        assertThrows(RuntimeException.class, () -> oAuthService.saveUser("code", "invalid provider"));
//    }
//
//
//    @Test
//    @DisplayName("OAuth 서버 mock -> getAccessToken 테스트")
//    public void getAccessToken() {
//        // given
//        String code = "mockCode";
//        String provider = "kakao";
//        String tokenResponseJson = "{\"access_token\": \"mock_access_token\", \"token_type\": \"bearer\", \"refresh_token\": \"mock_refresh_token\", \"expires_in\": 3600, \"scope\": \"mock_scope\", \"refresh_token_expires_in\": 9999}";
//
//        ResponseEntity<String> mockResponseEntity = ResponseEntity.ok(tokenResponseJson);
//        when(rt.exchange(
//                anyString(), // You can use anyString() for the URL
//                eq(HttpMethod.POST),
//                any(HttpEntity.class),
//                eq(String.class)
//        )).thenReturn(mockResponseEntity);
//
//        System.out.println("mockResponseEntity = " + mockResponseEntity);
//
//        // when
//        OauthToken oauthToken = oAuthService.getAccessToken(code, provider);
//
//        System.out.println("oauthToken = " + oauthToken);
//
//        // then
//        verify(rt).exchange(
//                anyString(),
//                eq(HttpMethod.POST),
//                any(HttpEntity.class),
//                eq(String.class)
//        );
//
//        assertThat("mock_access_token").isEqualTo(oauthToken.getAccess_token());
//        assertThat("bearer").isEqualTo(oauthToken.getToken_type());
//        assertThat("mock_refresh_token").isEqualTo(oauthToken.getRefresh_token());
//        assertThat(3600).isEqualTo(oauthToken.getExpires_in());
//        assertThat(9999).isEqualTo(oauthToken.getRefresh_token_expires_in());
//        assertThat("mock_scope").isEqualTo(oauthToken.getScope());
//    }
//
//    @Test
//    @DisplayName("카카오 새로운 유저 가입")
//    void saveNewUserKakao() {
//        // given
//        String code = "mockCode";
//        String provider = "kakao";
//        String tokenResponseJson = "{\"access_token\": \"mock_access_token\", \"token_type\": \"bearer\", \"refresh_token\": \"mock_refresh_token\", \"expires_in\": 3600, \"scope\": \"mock_scope\", \"refresh_token_expires_in\": 9999}";
//
//        ResponseEntity<String> mockResponseEntity = ResponseEntity.ok(tokenResponseJson);
//        when(rt.exchange(
//                anyString(),
//                eq(HttpMethod.POST),
//                any(HttpEntity.class),
//                eq(String.class)
//        )).thenReturn(mockResponseEntity);
//
//        OauthToken oauthToken = oAuthService.getAccessToken(code, provider);
//
//        System.out.println("oauthToken.getAccess_token() = " + oauthToken.getAccess_token());
//
//        // Create a sample JSON response for the Kakao profile
//        String sampleKakaoProfileJson = getMockKakaoProfile();
//        // Create a mock response entity for the Kakao profile
//        ResponseEntity<String> mockKakaoProfileResponse = ResponseEntity.ok(sampleKakaoProfileJson);
//
//        // Mock the behavior of restTemplate.exchange method for the Kakao profile request
//        when(rt.exchange(
//                eq("https://kapi.kakao.com/v2/user/me"), // URL should match your request URL
//                eq(HttpMethod.POST), // HTTP method should match your request method
//                any(HttpEntity.class), // You can use any(HttpEntity.class) to match the request entity
//                eq(String.class) // Response type should match your expected response type
//        )).thenReturn(mockKakaoProfileResponse);
//
//        // when
//        Long savedUserId = oAuthService.saveUser(oauthToken.getAccess_token(), provider);
//        User findUser = userRepository.findById(savedUserId).get();
//
//        // then
//        assertThat(findUser.getId()).isEqualTo(savedUserId);
//        assertThat(findUser.getEmail()).isEqualTo("sample@example.com");
//        assertThat(findUser.getProfileImgUrl()).isEqualTo("sampleProfileImageUrl");
//        assertThat(findUser.getProvider()).isEqualTo("kakao");
//        assertThat(findUser.getNickname()).isEqualTo("sampleNickname");
//        assertThat(findUser.getUserRole()).isEqualTo(UserRole.USER);
//        assertThat(findUser.getDescription()).isEqualTo("joinByKakao");
//    }
//
//    @Test
//    @DisplayName("구글 새로운 유저 가입")
//    void saveNewUserGoogle() {
//        // given
//        String code = "mockCode";
//        String provider = "google";
//        String tokenResponseJson = "{\"access_token\": \"mock_access_token\", \"token_type\": \"bearer\", \"refresh_token\": \"mock_refresh_token\", \"expires_in\": 3600, \"scope\": \"mock_scope\", \"refresh_token_expires_in\": 9999}";
//
//        ResponseEntity<String> mockResponseEntity = ResponseEntity.ok(tokenResponseJson);
//        when(rt.exchange(
//                anyString(),
//                eq(HttpMethod.POST),
//                any(HttpEntity.class),
//                eq(String.class)
//        )).thenReturn(mockResponseEntity);
//
//        OauthToken oauthToken = oAuthService.getAccessToken(code, provider);
//
//        System.out.println("oauthToken.getAccess_token() = " + oauthToken.getAccess_token());
//
//        // Create a sample JSON response for the Kakao profile
//        String sampleGoogleProfile = getMockGoogleProfile();
//        // Create a mock response entity for the Kakao profile
//        ResponseEntity<String> mockGoogleProfileResponse = ResponseEntity.ok(sampleGoogleProfile);
//
//        // Mock the behavior of restTemplate.exchange method for the Kakao profile request
//        when(rt.exchange(
//                eq("https://www.googleapis.com/oauth2/v3/userinfo"), // URL should match your request URL
//                eq(HttpMethod.POST), // HTTP method should match your request method
//                any(HttpEntity.class), // You can use any(HttpEntity.class) to match the request entity
//                eq(String.class) // Response type should match your expected response type
//        )).thenReturn(mockGoogleProfileResponse);
//
//        // when
//        Long savedUserId = oAuthService.saveUser(oauthToken.getAccess_token(), provider);
//        User findUser = userRepository.findById(savedUserId).get();
//
//        // then
//        assertThat(findUser.getId()).isEqualTo(savedUserId);
//        assertThat(findUser.getEmail()).isEqualTo("sample@example.com");
//        assertThat(findUser.getProfileImgUrl()).isEqualTo("samplePicture");
//        assertThat(findUser.getProvider()).isEqualTo("google");
//        assertThat(findUser.getNickname()).isEqualTo("sampleName");
//        assertThat(findUser.getUserRole()).isEqualTo(UserRole.USER);
//        assertThat(findUser.getDescription()).isEqualTo("joinByGoogle");
//    }
//
//    @Test
//    @DisplayName("깃 새로운 유저 가입")
//    void saveNewUserGit() {
//        // given
//        String code = "mockCode";
//        String provider = "git";
//        String tokenResponseJson = "access_token=mock_access_token&token_type=bearer&refresh_token=mock_refresh_token&expires_in=3600&scope=mock_scope&refresh_token_expires_in=9999";
//
//        ResponseEntity<String> mockResponseEntity = ResponseEntity.ok(tokenResponseJson);
//        when(rt.exchange(
//                anyString(),
//                eq(HttpMethod.POST),
//                any(HttpEntity.class),
//                eq(String.class)
//        )).thenReturn(mockResponseEntity);
//
//        OauthToken oauthToken = oAuthService.getAccessToken(code, provider);
//
//        System.out.println("oauthToken.getAccess_token() = " + oauthToken.getAccess_token());
//
//        // Create a sample JSON response for the Kakao profile
//        String sampleGitProfile1 = getMockGitProfile1;
//        String sampleGitProfile2 = getMockGitProfile2();
//        // Create a mock response entity for the Kakao profile
//        ResponseEntity<String> mockGitProfileResponse1 = ResponseEntity.ok(sampleGitProfile1);
//        ResponseEntity<String> mockGitProfileResponse2 = ResponseEntity.ok(sampleGitProfile2);
//
//        // Mock the behavior of restTemplate.exchange method for the Kakao profile request
//        when(rt.exchange(
//                eq("https://api.github.com/user"), // URL should match your request URL
//                eq(HttpMethod.GET), // HTTP method should match your request method
//                any(HttpEntity.class), // You can use any(HttpEntity.class) to match the request entity
//                eq(String.class) // Response type should match your expected response type
//        )).thenReturn(mockGitProfileResponse1);
//
//        when(rt.exchange(
//                eq("https://api.github.com/user/emails"), // URL should match your request URL
//                eq(HttpMethod.GET), // HTTP method should match your request method
//                any(HttpEntity.class), // You can use any(HttpEntity.class) to match the request entity
//                eq(String.class) // Response type should match your expected response type
//        )).thenReturn(mockGitProfileResponse2);
//
//        // when
//        Long savedUserId = oAuthService.saveUser(oauthToken.getAccess_token(), provider);
//        User findUser = userRepository.findById(savedUserId).get();
//
//        // then
//        assertThat(findUser.getId()).isEqualTo(savedUserId);
//        assertThat(findUser.getEmail()).isEqualTo("sample@example.com");
//        assertThat(findUser.getProfileImgUrl()).isEqualTo("samplePicture");
//        assertThat(findUser.getProvider()).isEqualTo("git");
//        assertThat(findUser.getNickname()).isEqualTo("sampleName");
//        assertThat(findUser.getUserRole()).isEqualTo(UserRole.USER);
//        assertThat(findUser.getDescription()).isEqualTo("joinByGit");
//    }
//
//    @Test
//    @DisplayName("기존 유저 가입")
//    void saveExistUser() {
//        // given
//        User firstUser = User.builder()
//                .email("sample@example.com")
//                .provider("kakao")
//                .build();
//
//        userRepository.save(firstUser);
//
//        String code = "mockCode";
//        String provider = "kakao";
//        String tokenResponseJson = "{\"access_token\": \"mock_access_token\", \"token_type\": \"bearer\", \"refresh_token\": \"mock_refresh_token\", \"expires_in\": 3600, \"scope\": \"mock_scope\", \"refresh_token_expires_in\": 9999}";
//
//        ResponseEntity<String> mockResponseEntity = ResponseEntity.ok(tokenResponseJson);
//        when(rt.exchange(
//                anyString(),
//                eq(HttpMethod.POST),
//                any(HttpEntity.class),
//                eq(String.class)
//        )).thenReturn(mockResponseEntity);
//
//        OauthToken oauthToken = oAuthService.getAccessToken(code, provider);
//
//        System.out.println("oauthToken.getAccess_token() = " + oauthToken.getAccess_token());
//
//        // Create a sample JSON response for the Kakao profile
//        String sampleKakaoProfileJson = getMockKakaoProfile();
//        // Create a mock response entity for the Kakao profile
//        ResponseEntity<String> mockKakaoProfileResponse = ResponseEntity.ok(sampleKakaoProfileJson);
//
//        // Mock the behavior of restTemplate.exchange method for the Kakao profile request
//        when(rt.exchange(
//                eq("https://kapi.kakao.com/v2/user/me"), // URL should match your request URL
//                eq(HttpMethod.POST), // HTTP method should match your request method
//                any(HttpEntity.class), // You can use any(HttpEntity.class) to match the request entity
//                eq(String.class) // Response type should match your expected response type
//        )).thenReturn(mockKakaoProfileResponse);
//
//        oAuthService.saveUser(oauthToken.getAccess_token(), provider);
//        oAuthService.saveUser(oauthToken.getAccess_token(), provider);
//        // when
//        List<User> findAll = userRepository.findAll();
//        // then
//        assertThat(findAll.size()).isEqualTo(2);
//    }
//
//    @Test
//    @DisplayName("JWT 생성 테스트")
//    void createJWTTokenAndVerify() {
//        // given
//        User firstUser = User.builder()
//                .email("sample@example.com")
//                .provider("kakao")
//                .build();
//
//        User savedUser = userRepository.save(firstUser);
//        // when
//        String jwtToken = oAuthService.createJWTToken(savedUser.getId());
//        // then
//        System.out.println("jwtToken = " + jwtToken);
//        assertThat(jwtToken).isNotEmpty();
//    }
//}