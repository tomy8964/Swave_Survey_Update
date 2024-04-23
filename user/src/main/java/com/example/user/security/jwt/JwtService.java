package com.example.user.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.example.user.security.jwt.JwtRequestFilter.*;

@Service
public class JwtService {

    public Long getUserIdByJWT(HttpServletRequest request) {
        String jwtHeader = request.getHeader(HEADER_STRING);
        String token = jwtHeader.replace(TOKEN_PREFIX, "");
        return JWT.require(Algorithm.HMAC512(SECRET)).build().verify(token).getClaim("id").asLong();
    }

    public String createJWTToken(String email, Long userId, String name) {
        return JWT.create()
                .withSubject(email)
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .withClaim("id", userId)
                .withClaim("nickname", name)
                .sign(Algorithm.HMAC512(SECRET));
    }
}
