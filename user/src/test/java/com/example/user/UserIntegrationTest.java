package com.example.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("JWT 필터와 CustomAuthenticationEntryPoint 통합 테스트")
    void jwtFilterAndCustomAuthEntryPointIntegrationTest() throws Exception {
        mockMvc.perform(get("/api/protected/resource") // 보호된 리소스에 접근하는 요청
                        .header("Authorization", "")) // 헤더가 비어있는 상태로 설정
                .andExpect(status().isUnauthorized()) // 401 Unauthorized 상태 코드를 기대함
                .andExpect(content().string(containsString("인증 에러가 발생했습니다."))); // 기본 에러 메시지 포함 여부 검증
    }
}
