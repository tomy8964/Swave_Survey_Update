package com.example.user.user.controller;

import com.example.user.user.domain.User;
import com.example.user.user.repository.UserRepository;
import com.example.user.user.request.UserUpdateRequest;
import com.example.user.user.service.OAuthService;
import com.example.user.user.service.UserService;
import com.example.user.util.oAuth.JwtProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class UserExternalControllerTest2 {
    @Mock
    UserService userService;
    @InjectMocks
    UserExternalController userExternalController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
        userExternalController.setUserService(userService);
    }

    @Test
    public void getLogin() throws Exception {
        // given
        String code = "mockCode";
        String provider = "kakao";

        // Mock a response from your OAuthService
        String mockJwtToken = "mockJwtToken";
        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.set("Authorization", "Bearer " + mockJwtToken);

        when(userService.getLogin(code, provider)).thenReturn(ResponseEntity.ok().headers(expectedHeaders).body("\"success\""));

        ResponseEntity<String> expectedResponse = ResponseEntity
                .ok()
                .headers(expectedHeaders)
                .body("\"success\"");

        ResponseEntity<String> response = userExternalController.getLogin(code, provider);
        assertThat(response).isEqualTo(expectedResponse);
    }
}