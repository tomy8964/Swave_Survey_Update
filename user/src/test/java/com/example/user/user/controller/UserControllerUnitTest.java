package com.example.user.user.controller;

import com.example.user.security.oAuth.provider.Kakao;
import com.example.user.user.domain.UserRole;
import com.example.user.user.request.UserUpdateRequest;
import com.example.user.user.response.UserDto;
import com.example.user.user.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.sql.Timestamp;

import static com.example.user.security.jwt.JwtRequestFilter.HEADER_STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser
@ActiveProfiles("test")
@WebMvcTest({UserExternalController.class, UserInternalController.class})
public class UserControllerUnitTest {

    @Autowired
    ObjectMapper mapper;

    @Autowired
    MockMvc mockMvc;
    @MockBean
    UserService userService;

    @Test
    @DisplayName("OAuth를 통한 외부 사용자 로그인 테스트")
    public void getLogin() throws Exception {
        // given
        String code = "testCode";
        String provider = "testProvider";
        String jwtToken = "testJwtToken";
        when(userService.getLogin(code, provider)).thenReturn(jwtToken);

        // when
        MvcResult mvcResult = mockMvc.perform(post("/api/user/external/oauth/token")
                        .param("code", code)
                        .param("provider", provider)
                        .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(header().string(HEADER_STRING, jwtToken))
                .andDo(print())
                .andReturn();
        // then
        assertEquals("Login Success", mvcResult.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("JWTToken 통한 현재 사용자 조회")
    void getCurrentUser() throws Exception {
        // given
        UserDto testUserDto = UserDto.builder()
                .profileImgUrl("http://example.com/profile.jpg")
                .nickname("Test Nickname")
                .email("test@example.com")
                .provider(new Kakao().getValue())
                .userRole(UserRole.USER)
                .description("Test Description")
                .createTime(new Timestamp(30))
                .build();

        when(userService.getCurrentUser(any(HttpServletRequest.class))).thenReturn(testUserDto);


        // when
        MvcResult mvcResult = mockMvc.perform(get("/api/user/external/me")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        UserDto actualUser = mapper.readValue(responseBody, new TypeReference<>() {
        });

        // then
        assertEquals(testUserDto.getProfileImgUrl(), actualUser.getProfileImgUrl());
        assertEquals(testUserDto.getNickname(), actualUser.getNickname());
        assertEquals(testUserDto.getEmail(), actualUser.getEmail());
        assertEquals(testUserDto.getProvider(), actualUser.getProvider());
        assertEquals(testUserDto.getUserRole(), actualUser.getUserRole());
        assertEquals(testUserDto.getDescription(), actualUser.getDescription());
        assertEquals(testUserDto.getCreateTime(), actualUser.getCreateTime());
    }

    @Test
    @DisplayName("Mypage 수정 테스트")
    void updateMyPage() throws Exception {
        // given
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .nickname("이름 변경")
                .description("유저 정보를 업데이트했습니다.")
                .build();
        when(userService.updateMyPage(any(HttpServletRequest.class), any())).thenReturn(updateRequest.getNickname());

        // when
        MvcResult mvcResult = mockMvc.perform(patch("/api/user/external/updatepage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();

        // then
        assertEquals(updateRequest.getNickname() + "님의 정보가 변경되었습니다.", responseBody);
    }

    @Test
    @DisplayName("사용자 탈퇴 테스트")
    void deleteUser() throws Exception {
        // given
        when(userService.deleteUser(any(HttpServletRequest.class))).thenReturn("testUser");

        // when
        MvcResult mvcResult = mockMvc.perform(patch("/api/user/external/deleteuser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andDo(print())
                .andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();

        // then
        assertEquals("testUser님의 정보가 삭제되었습니다.", responseBody);
    }

//    @Test
//    @DisplayName("내부 서비스 통신 테스트 - JWTToken 통한 현재 사용자 조회")
//    void getCurrentUserInternal() throws Exception {
//        // given
//        Long testUserId = 1L;
//        when(userService.getUserIdByJWT(any(HttpServletRequest.class))).thenReturn(testUserId);
//
//        // when
//        MvcResult mvcResult = mockMvc.perform(get("/api/user/internal/me")
//                        .with(csrf()))
//                .andExpect(status().isOk())
//                .andDo(print())
//                .andReturn();
//        String responseBody = mvcResult.getResponse().getContentAsString();
//
//        // then
//        assertEquals(testUserId.toString(), responseBody);
//    }
}