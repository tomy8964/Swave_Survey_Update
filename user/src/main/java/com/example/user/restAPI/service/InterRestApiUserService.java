package com.example.user.restAPI.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.user.util.oAuth.JwtProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterRestApiUserService {

    public Long getCurrentUser(HttpServletRequest request) {
        String jwtHeader = request.getHeader(JwtProperties.HEADER_STRING);
        String token = jwtHeader.replace(JwtProperties.TOKEN_PREFIX, "");

        return JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(token).getClaim("id").asLong();
    }
}
