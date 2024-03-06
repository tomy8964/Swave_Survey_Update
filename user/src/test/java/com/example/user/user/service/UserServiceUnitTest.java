package com.example.user.user.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.user.user.domain.User;
import com.example.user.user.domain.UserRole;
import com.example.user.user.exception.UserNotFoundException;
import com.example.user.user.repository.UserRepository;
import com.example.user.user.request.UserUpdateRequest;
import com.example.user.user.response.UserDto;
import com.example.user.util.oAuth.JwtProperties;
import com.example.user.util.oAuth.OauthToken;
import com.example.user.util.oAuth.provider.Provider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Slf4j
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private OAuthService oAuthService;
    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("JWT Token으로 UserId 가져오기 성공1")
    public void getUserIdSuccess() {
        //given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        String secret = JwtProperties.SECRET;
        String token = JWT.create()
                .withClaim("id", 1L)
                .sign(Algorithm.HMAC512(secret));
        when(request.getHeader(JwtProperties.HEADER_STRING)).thenReturn(token);

        //when
        Long userId = userService.getUserId(request);

        //then
        assertEquals(1L, userId);
    }

    /**
     * 클리언트가 보낸 요청에 담긴 JWT Token의
     * 형식은 맞으나 'id' 'claim'이 존재하지 않을 경우
     * 요청을 보낸 클라이언트에게 null 값을 보내 다시 보내기를 요청한다.
     */
    @Test
    @DisplayName("JWT Token으로 UserId 가져오기 성공2 - id claim이 존재하지 않아 null 값 반환")
    public void getUserIdSuccess2() {
        //given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        String secret = JwtProperties.SECRET;
        String token = JWT.create()
                .sign(Algorithm.HMAC512(secret));
        when(request.getHeader(JwtProperties.HEADER_STRING)).thenReturn(token);

        //when
        Long userId = userService.getUserId(request);

        //then
        assertEquals(null, userId);
    }

    @Test
    @DisplayName("JWT Token으로 UserId 가져오기 실패 - 올바르지 않은 JWT 토큰 형식")
    public void getUserIdFail() {
        // given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        String token = "InvalidToken";
        when(request.getHeader(JwtProperties.HEADER_STRING)).thenReturn(token);

        // when & then
        assertThrows(JWTVerificationException.class, () -> userService.getUserId(request));
    }

    @Test
    @DisplayName("JWT Token으로 현재 유저 정보 가져오기 성공")
    public void getCurrentUserSuccess() {
        //given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        String secret = JwtProperties.SECRET;
        String token = JWT.create()
                .withClaim("id", 1L)
                .sign(Algorithm.HMAC512(secret));
        User user = User.builder()
                .id(1L)
                .profileImgUrl("http://example.com/profile.jpg")
                .nickname("Test Nickname")
                .email("test@example.com")
                .provider(Provider.KAKAO)
                .userRole(UserRole.USER)
                .description("Test Description")
                .build();
        when(request.getHeader(JwtProperties.HEADER_STRING)).thenReturn(token);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        //when
        UserDto userDto = userService.getCurrentUser(request);

        //then
        assertEquals(user.getEmail(), userDto.getEmail());
        assertEquals(user.getProfileImgUrl(), userDto.getProfileImgUrl());
        assertEquals(user.getNickname(), userDto.getNickname());
        assertEquals(user.getEmail(), userDto.getEmail());
        assertEquals(user.getProvider(), userDto.getProvider());
        assertEquals(user.getUserRole(), userDto.getUserRole());
        assertEquals(user.getDescription(), userDto.getDescription());
        assertEquals(user.getCreateTime(), userDto.getCreateTime());
    }

    @Test
    @DisplayName("JWT Token으로 현재 유저 정보 가져오기 실패")
    public void getCurrentUserFail() {
        //given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        String secret = JwtProperties.SECRET;
        String token = JWT.create()
                .withClaim("id", 1L)
                .sign(Algorithm.HMAC512(secret));
        when(request.getHeader(JwtProperties.HEADER_STRING)).thenReturn(token);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        //when & then
        assertThrows(UserNotFoundException.class, () -> userService.getCurrentUser(request));
    }

    @Test
    @DisplayName("로그인 성공")
    public void getLoginSuccess() {
        //given
        Long userId = 1L;
        String code = "testCode";
        Provider provider = Provider.KAKAO;
        String jwtToken = "testJwtToken";
        OauthToken oauthToken = OauthToken.builder()
                .access_token("testAccessToken")
                .build();
        when(oAuthService.getOAuthToken(any(), any())).thenReturn(oauthToken);
        when(oAuthService.saveUser(any(), any())).thenReturn(userId);
        when(oAuthService.createJWTToken(userId)).thenReturn(jwtToken);

        //when
        String response = userService.getLogin(code, provider.getValue());

        //then
        assertEquals(jwtToken, response);
    }

    @Test
    @DisplayName("유저 정보 업데이트 성공")
    public void updateMyPageSuccess() {
        //given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .nickname("Update Nickname")
                .description("유저 정보 업데이트")
                .build();
        User user = User.builder()
                .id(1L)
                .profileImgUrl("http://example.com/profile.jpg")
                .nickname("Test Nickname")
                .email("test@example.com")
                .provider(Provider.KAKAO)
                .userRole(UserRole.USER)
                .description("Test Description")
                .build();
        Long userId = 1L;
        String secret = JwtProperties.SECRET;
        String token = JWT.create()
                .withClaim("id", userId)
                .sign(Algorithm.HMAC512(secret));
        when(request.getHeader(JwtProperties.HEADER_STRING)).thenReturn(token);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        //when
        String response = userService.updateMyPage(request, updateRequest);

        //then
        assertEquals(updateRequest.getNickname(), response);
    }

    @Test
    @DisplayName("유저 정보 업데이트 실패")
    public void updateMyPageFail() {
        // given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .nickname("Update Nickname")
                .description("유저 정보 업데이트")
                .build();
        Long userId = 1L;
        String secret = JwtProperties.SECRET;
        String token = JWT.create()
                .withClaim("id", userId)
                .sign(Algorithm.HMAC512(secret));
        when(request.getHeader(JwtProperties.HEADER_STRING)).thenReturn(token);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(UserNotFoundException.class, () -> userService.updateMyPage(request, updateRequest));
    }

    @Test
    @DisplayName("유저 정보 삭제 성공")
    public void deleteUserSuccess() {
        //given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        User user = User.builder()
                .id(1L)
                .profileImgUrl("http://example.com/profile.jpg")
                .nickname("Test Nickname")
                .email("test@example.com")
                .provider(Provider.KAKAO)
                .userRole(UserRole.USER)
                .description("Test Description")
                .build();
        Long userId = 1L;
        String secret = JwtProperties.SECRET;
        String token = JWT.create()
                .withClaim("id", userId)
                .sign(Algorithm.HMAC512(secret));
        when(request.getHeader(JwtProperties.HEADER_STRING)).thenReturn(token);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        //when
        String response = userService.deleteUser(request);

        //then
        assertEquals(user.getNickname(), response);
    }

    @Test
    @DisplayName("유저 정보 삭제 실패")
    public void deleteUserFail() {
        // given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Long userId = 1L;
        String secret = JwtProperties.SECRET;
        String token = JWT.create()
                .withClaim("id", userId)
                .sign(Algorithm.HMAC512(secret));
        when(request.getHeader(JwtProperties.HEADER_STRING)).thenReturn(token);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(request));
    }
}