package com.example.user.user.service;

import com.example.user.user.domain.User;
import com.example.user.user.repository.UserRepository;
import com.example.user.user.request.UserUpdateRequest;
import com.example.user.util.oAuth.JwtProperties;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
public class UserServiceTest {
    @PersistenceContext
    EntityManager em;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserService userService;
    @Autowired
    OAuthService oAuthService;

    @Test
    @DisplayName("JWT 해독 -> 유저 정보 가져오기")
    public void getUserByJWT() {
        // given
        User firstUser = User.builder()
                .email("sample@example.com")
                .provider("kakao")
                .build();

        User savedUser = userRepository.save(firstUser);
        String jwtToken = oAuthService.createJWTToken(savedUser.getId());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + jwtToken);

        //when
        User userByJWT = userService.getUserByJWT(request);

        //then
        assertThat(userByJWT).isNotNull();
        assertThat(savedUser.getId()).isEqualTo(userByJWT.getId());
        assertThat(savedUser.getEmail()).isEqualTo(userByJWT.getEmail());
        assertThat(savedUser.getProvider()).isEqualTo(userByJWT.getProvider());
    }

    @Test
    @DisplayName("유저 업데이트 테스트")
    public void updateMyPage() {
        //given
        User firstUser = User.builder()
                .nickname("savedUser")
                .description("saved 유저입니다.")
                .email("sample@example.com")
                .provider("kakao")
                .build();
        User savedUser = userRepository.save(firstUser);
        em.flush();
        em.clear();
        String jwtToken = oAuthService.createJWTToken(savedUser.getId());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + jwtToken);

        UserUpdateRequest update = new UserUpdateRequest("updateName", "update된 유저입니다.");
        em.flush();
        em.clear();

        //when
        String updateUserId = userService.updateMyPage(request, update);
        User updatedUser = userRepository.findById(Long.valueOf(updateUserId)).get();

        //then
        assertThat(updatedUser.getId()).isEqualTo(savedUser.getId());
        assertThat(updatedUser.getNickname()).isEqualTo("updateName");
        assertThat(updatedUser.getDescription()).isEqualTo("update된 유저입니다.");

    }

    @Test
    @DisplayName("유저 삭제 테스트")
    public void deleteUser() {
        //given
        User firstUser = User.builder()
                .nickname("savedUser")
                .description("saved 유저입니다.")
                .email("sample@example.com")
                .provider("kakao")
                .build();
        User savedUser = userRepository.save(firstUser);
        String jwtToken = oAuthService.createJWTToken(savedUser.getId());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + jwtToken);

        //when
        userService.deleteUser(request);
        User deletedUser = userRepository.findById(savedUser.getId()).get();

        //then
        assertThat(deletedUser.isDeleted()).isTrue();
    }
}