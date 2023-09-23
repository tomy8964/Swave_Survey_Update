package com.example.user.util.oAuth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    // 인증에서 제외할 url
    private static final String Exclude_url = "/api/*";
    private static final List<String> EXCLUDE_URL =
            List.of("/api/user/external/oauth/token", "");

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String jwtHeader = request.getHeader(JwtProperties.HEADER_STRING);
        System.out.println("JwtRequestFilter 진입");

        if (pathMatchesExcludePattern(request.getRequestURI())) {
            // Skip JWT authentication for excluded URLs
            filterChain.doFilter(request, response);
            return;
        } else if (request.getHeader("Authorization") == null) {
            log.info("error");
            request.setAttribute(JwtProperties.HEADER_STRING, "Authorization이 없습니다.");
            System.out.println("Authorization");
            throw new ServletException();
        }
        // header 가 정상적인 형식인지 확인
        if (jwtHeader == null || !jwtHeader.startsWith(JwtProperties.TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        // jwt 토큰을 검증해서 정상적인 사용자인지 확인
        String token = jwtHeader.replace(JwtProperties.TOKEN_PREFIX, "");

        Long userCode;

        if (pathMatchesExcludePattern(request.getRequestURI())) {
            // Skip JWT authentication for excluded URLs
            filterChain.doFilter(request, response);
            return;
        } else {
            try {
                userCode = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(token).getClaim("id").asLong();
                System.out.println(userCode);
            } catch (TokenExpiredException e) {
                request.setAttribute(JwtProperties.HEADER_STRING, "토큰이 만료되었습니다.");
                throw new ServletException(e);
            } catch (JWTVerificationException e) {
                request.setAttribute(JwtProperties.HEADER_STRING, "유효하지 않은 토큰입니다.");
                throw new ServletException(e);
            }
        }

        request.setAttribute("userCode", userCode);

        filterChain.doFilter(request, response);
    }

    // Filter에서 제외할 URL 설정
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return EXCLUDE_URL.stream().anyMatch(exclude -> exclude.equalsIgnoreCase(request.getServletPath()));
    }

    private boolean pathMatchesExcludePattern(String requestURI) {
        AntPathMatcher pathMatcher = new AntPathMatcher();
        String[] excludeUrls = Exclude_url.split(",");
        for (String excludeUrl : excludeUrls) {
            if (pathMatcher.match(excludeUrl, requestURI)) {
                return true;
            }
        }
        return false;
    }
}