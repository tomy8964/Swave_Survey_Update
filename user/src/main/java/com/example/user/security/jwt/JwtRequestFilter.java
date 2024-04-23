package com.example.user.security.jwt;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@Profile({"local", "server"})
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    private static final List<String> EXCLUDE_URL = List.of(
            "/api/user/external/oauth/token",
            "/favicon.ico",
            "/swagger/**",
            "/swagger-resources/**",
            "/swagger-ui/**", "/webjars/**", "/swagger-ui.html",
            "/v3/api-docs/**"
    );
    @Value("${jwt.secret}")
    public static String SECRET;
    @Value("${jwt.expiration-time}")
    public static int EXPIRATION_TIME;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            final String jwtHeader = request.getHeader(HEADER_STRING);
            log.info("JWT Filter 진입");
            if (pathMatchesExcludePattern(request.getRequestURI())) {
                filterChain.doFilter(request, response);
                return;
            }

            if (jwtHeader == null || jwtHeader.isEmpty()) {
                request.setAttribute("exception", JwtErrorCode.NOTFOUND_TOKEN);
                throw new JwtFilterException(JwtErrorCode.NOTFOUND_TOKEN);
            }

            if (!jwtHeader.startsWith(TOKEN_PREFIX)) {
                request.setAttribute("exception", JwtErrorCode.UNSUPPORTED_TOKEN);
                throw new JwtFilterException(JwtErrorCode.UNSUPPORTED_TOKEN);
            }

            String token = jwtHeader.replace(TOKEN_PREFIX, "");
            request.setAttribute("userCode", JWT.require(Algorithm.HMAC512(SECRET)).build().verify(token).getClaim("id").asLong());
        } catch (TokenExpiredException e) {
            request.setAttribute("exception", JwtErrorCode.EXPIRED_TOKEN);
            throw new JwtFilterException(JwtErrorCode.EXPIRED_TOKEN);
        } catch (JWTVerificationException e) {
            request.setAttribute("exception", JwtErrorCode.WRONG_TYPE_TOKEN);
            throw new JwtFilterException(JwtErrorCode.WRONG_TYPE_TOKEN);
        } catch (JwtFilterException e) {
            filterChain.doFilter(request, response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean pathMatchesExcludePattern(String requestURI) {
        AntPathMatcher pathMatcher = new AntPathMatcher();
        for (String excludeUrl : EXCLUDE_URL) {
            if (pathMatcher.match(excludeUrl, requestURI)) {
                return true;
            }
        }
        return false;
    }
}