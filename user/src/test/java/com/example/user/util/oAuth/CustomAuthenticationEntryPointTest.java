//package com.example.user.util.oAuth;
//
//import jakarta.servlet.http.HttpServletResponse;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.mock.web.MockHttpServletRequest;
//import org.springframework.mock.web.MockHttpServletResponse;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.transaction.annotation.Transactional;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@Transactional
//public class CustomAuthenticationEntryPointTest {
//
//    @Test
//    public void testCommenceExpiredToken() throws Exception {
//        // Given
//        CustomAuthenticationEntryPoint entryPoint = new CustomAuthenticationEntryPoint();
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        request.setAttribute(JwtProperties.HEADER_STRING, "토큰이 만료되었습니다.");
//        MockHttpServletResponse response = new MockHttpServletResponse();
//        AuthenticationException authException = new AuthenticationException("토큰이 만료되었습니다.") {};
//
//        // When
//        entryPoint.commence(request, response, authException);
//
//        // Then
//        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
//        assertEquals("application/json;charset=UTF-8", response.getContentType());
//        assertEquals(JwtProperties.HEADER_STRING + " : 토큰이 만료되었습니다." + System.lineSeparator(), response.getContentAsString());
//    }
//
//    @Test
//    public void testCommenceInvalidToken() throws Exception {
//        // Given
//        CustomAuthenticationEntryPoint entryPoint = new CustomAuthenticationEntryPoint();
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        request.setAttribute(JwtProperties.HEADER_STRING, "유효하지 않은 토큰입니다.");
//        MockHttpServletResponse response = new MockHttpServletResponse();
//        AuthenticationException authException = new AuthenticationException("유효하지 않은 토큰입니다.") {};
//
//        // When
//        entryPoint.commence(request, response, authException);
//
//        // Then
//        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
//        assertEquals("application/json;charset=UTF-8", response.getContentType());
//        assertEquals(JwtProperties.HEADER_STRING + " : 유효하지 않은 토큰입니다." + System.lineSeparator(), response.getContentAsString());
//    }
//}