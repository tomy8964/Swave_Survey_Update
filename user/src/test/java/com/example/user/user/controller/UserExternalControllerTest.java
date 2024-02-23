package com.example.user.user.controller;

import com.example.user.user.domain.User;
import com.example.user.user.repository.UserRepository;
import com.example.user.user.request.UserUpdateRequest;
import com.example.user.user.service.OAuthService;
import com.example.user.util.oAuth.JwtProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@TestConfiguration
public class UserExternalControllerTest {

    @PersistenceContext
    EntityManager em;

    @Autowired
    /*
      웹 API 테스트할 때 사용
      스프링 MVC 테스트의 시작점
      HTTP GET,POST 등에 대해 API 테스트 가능
      */
            MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    OAuthService oAuthService;

    @Test
    @DisplayName("현재 유저 JWT 통해 가져오기 테스트")
    public void testGetCurrentUser() throws Exception {
        // given
        User user = User.builder()
                .nickname("savedUser")
                .description("saved 유저입니다.")
                .email("sample@example.com")
                .provider("kakao")
                .build();
        User savedUser = userRepository.save(user);
        userRepository.flush();

        String jwtToken = oAuthService.createJWTToken(savedUser.getId());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + jwtToken);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/user/external/me")
                        .header(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + jwtToken))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        User responseUser = new ObjectMapper().readValue(responseContent, User.class);

        assertThat(responseUser.getNickname()).isEqualTo(user.getNickname());
        assertThat(responseUser.getDescription()).isEqualTo(user.getDescription());
        assertThat(responseUser.getEmail()).isEqualTo(user.getEmail());
        assertThat(responseUser.getProvider()).isEqualTo(user.getProvider());
    }

    @Test
    @DisplayName("유저 업데이트 컨트롤러 테스트")
    void updateMyPage() throws Exception {
        // given
        User user = User.builder()
                .nickname("savedUser2")
                .description("saved 유저입니다2.")
                .email("sample@example.com")
                .provider("kakao")
                .build();
        User savedUser = userRepository.save(user);
        userRepository.flush();

        // when
        String jwtToken = oAuthService.createJWTToken(savedUser.getId());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + jwtToken);
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest("updatedUser", "update된 유저입니다");
        ObjectMapper mapper = new ObjectMapper();
        String updateRequest = mapper.writeValueAsString(userUpdateRequest);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.patch("/api/user/external/updatepage")
                        .header(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andReturn();
        userRepository.flush();
        // then
        User findUser = userRepository.findById(savedUser.getId()).get();

        assertThat(findUser.getNickname()).isEqualTo("updatedUser");
        assertThat(findUser.getDescription()).isEqualTo("update된 유저입니다");
        assertThat(findUser.getEmail()).isEqualTo("sample@example.com");
        assertThat(result.getResponse().getContentAsString()).isEqualTo(savedUser.getId().toString());
    }

    @Test
    @DisplayName("유저 삭제 컨트롤러 테스트")
    void deleteUser() throws Exception {
        // given
        User user = User.builder()
                .nickname("savedUser")
                .description("saved 유저입니다.")
                .email("sample@example.com")
                .provider("kakao")
                .build();
        User savedUser = userRepository.save(user);
        userRepository.flush();

        String jwtToken = oAuthService.createJWTToken(savedUser.getId());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + jwtToken);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.patch("/api/user/external/deleteuser")
                        .header(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + jwtToken))
                .andExpect(status().isOk())
                .andReturn();
    }
}