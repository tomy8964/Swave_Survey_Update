package com.example.user.util.oAuth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
public class JwtRequestFilterTest {

    @Mock
    private FilterChain filterChain;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @InjectMocks
    private JwtRequestFilter jwtRequestFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("인증 제외 URL 테스트")
    public void skipAuthenticationForExcludedURL() throws IOException, ServletException {
        // Given: A request for an excluded URL
        when(request.getRequestURI()).thenReturn("/api/user/external/oauth/token");
        when(request.getServletPath()).thenReturn("/api/user/external/oauth/token");


        // When: Calling doFilterInternal
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Then: The filterChain should be invoked without authentication
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("인증 실패 No Authorization")
    public void noAuthorizationHeader() {
        // Given: A request with no Authorization header
        when(request.getRequestURI()).thenReturn("/api/noHeader");
        when(request.getHeader("Authorization")).thenReturn(null);

        // When: Calling doFilterInternal
        assertThrows(ServletException.class, () ->
                jwtRequestFilter.doFilterInternal(request, response, filterChain));

        // Then: The request attribute should be set and ServletException should be thrown
        verify(request).setAttribute(eq(JwtProperties.HEADER_STRING), eq("Authorization이 없습니다."));
    }

    @Test
    @DisplayName("인증 실패 InValid Authorization1")
    public void invalidToken1() {
        // Given: A request with an invalid JWT token
        when(request.getRequestURI()).thenReturn("/api/some-other-endpoint");
        when(request.getHeader("Authorization")).thenReturn("invalid_token");

        // When: Calling doFilterInternal
        // then
        assertThrows(ServletException.class, () ->
                jwtRequestFilter.doFilterInternal(request, response, filterChain));
    }

    @Test
    @DisplayName("인증 실패 InValid Authorization2")
    public void invalidToken2() {
        // Given: A request with an invalid JWT token
        when(request.getRequestURI()).thenReturn("/api/some-other-endpoint");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid_token");

        // When: Calling doFilterInternal
        assertThrows(ServletException.class, () ->
                jwtRequestFilter.doFilterInternal(request, response, filterChain));

        // Then: The request attribute should be set and ServletException should be thrown
        verify(request).setAttribute(eq(JwtProperties.HEADER_STRING), eq("유효하지 않은 토큰입니다."));
    }
}