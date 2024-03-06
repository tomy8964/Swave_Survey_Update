//package com.example.user.user.service;
//
//import com.example.user.util.oAuth.OauthToken;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.ResponseEntity;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ActiveProfiles("test")
//@Transactional
//@SpringBootTest
//public class UserServiceTest2 {
//
//    @Mock
//    OAuthService oAuthService;
//    @InjectMocks
//    UserService userService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this); // Initialize mocks
//        userService.setoAuthService(oAuthService);
//    }
//
//    @Test
//    @DisplayName("getLogin 테스트")
//    public void getLogin() {
//        //given
//        String code = "mockCode";
//        String provider = "kakao";
//        Long mockUserId = 12345L;
//        String mockJwtToken = "mockJwtToken";
//        OauthToken oauthToken = new OauthToken(
//                "mock_access_token",
//                "bearer",
//                "mock_refresh_token",
//                3600,
//                "mock_scope",
//                9999);
//
//        //when
//        when(oAuthService.getAccessToken(code, provider)).thenReturn(oauthToken);
//        when(oAuthService.saveUser(oauthToken.getAccess_token(), provider)).thenReturn(mockUserId);
//        when(oAuthService.createJWTToken(mockUserId)).thenReturn(mockJwtToken);
//
//        ResponseEntity<String> response = userService.getLogin(code, provider);
//
//
//        verify(oAuthService).getAccessToken(code, provider);
//        verify(oAuthService).saveUser(oauthToken.getAccess_token(), provider);
//        verify(oAuthService).createJWTToken(mockUserId);
//
//        //then
//        HttpHeaders expectedHeaders = new HttpHeaders();
//        expectedHeaders.set("Authorization", "Bearer " + mockJwtToken);
//
//        ResponseEntity<String> expectedResponse = ResponseEntity
//                .ok()
//                .headers(expectedHeaders)
//                .body("\"success\"");
//        assertThat(response).isEqualTo(expectedResponse);
//    }
//
//}