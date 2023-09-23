package com.example.user.user.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.user.user.domain.User;
import com.example.user.user.exception.UserNotFoundException;
import com.example.user.user.repository.UserRepository;
import com.example.user.user.request.UserUpdateRequest;
import com.example.user.util.oAuth.JwtProperties;
import com.example.user.util.oAuth.OauthToken;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final OAuthService oAuthService;
    private final UserRepository userRepository;

    private static HttpHeaders getHttpHeaders(String jwtToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + jwtToken);
        return headers;
    }

    public User getUserByJWT(HttpServletRequest request) { //(1)
//        Long userCode = (Long) request.getAttribute("userCode");
        String jwtHeader = (request).getHeader(JwtProperties.HEADER_STRING);
        String token = jwtHeader.replace(JwtProperties.TOKEN_PREFIX, "");
        Long userId = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(token).getClaim("id").asLong();

        return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    }

    public User getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userCode");
        return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    }

    public ResponseEntity<String> getLogin(String code, String provider) {
        OauthToken oauthToken = oAuthService.getAccessToken(code, provider);
        Long saveUserId = oAuthService.SaveUser(oauthToken.getAccess_token(), provider);
        String jwtToken = oAuthService.createJWTToken(saveUserId);
        HttpHeaders headers = getHttpHeaders(jwtToken);

        return ResponseEntity.ok().headers(headers).body("\"success\"");
    }

    @Transactional
    public String updateMyPage(HttpServletRequest request, UserUpdateRequest userUpdateRequest) {
        User user = getUserByJWT(request);
        User findUser = userRepository.findById(user.getId()).orElseThrow(UserNotFoundException::new);
        findUser.updateUser(userUpdateRequest.getNickname(), userUpdateRequest.getDescription());

        return findUser.getId().toString();
    }

    public String deleteUser(HttpServletRequest request) {
        User user = getUserByJWT(request);
        user.setIsDeleted(true);
        userRepository.flush();
        return user.getId().toString();
    }
}
