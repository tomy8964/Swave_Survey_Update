package com.example.user.restAPI.service;

import com.example.user.user.domain.User;
import com.example.user.user.repository.UserRepository;
import com.example.user.user.service.OAuthService;
import com.example.user.util.oAuth.JwtProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
public class RestApiUserServiceTest {

    @Autowired
    OuterRestApiUserService outerRestApiUserService;
    @Autowired
    InterRestApiUserService interRestApiUserService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    OAuthService oAuthService;

    @Test
    public void restApiTest() {
        outerRestApiUserService.sendUserToSurveyDocument(1L);

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
        interRestApiUserService.getCurrentUser(request);
    }

}