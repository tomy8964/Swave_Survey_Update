package com.example.user.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static com.example.user.security.jwt.JwtRequestFilter.HEADER_STRING;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class JwtRequestFilterTest {

    @InjectMocks
    private JwtRequestFilter jwtRequestFilter;

    @Test
    @DisplayName("doFilterInternal 메서드 테스트1")
    void doFilterInternal() throws ServletException, IOException {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        when(request.getHeader(HEADER_STRING)).thenReturn("Bearer testToken");
        when(request.getRequestURI()).thenReturn("/api/user/external/oauth/token");

        // when
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal 메서드 테스트2")
    void doFilterInternal2() throws ServletException, IOException {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        when(request.getHeader(HEADER_STRING)).thenReturn("");

        // when
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
    }
}