package com.example.user.user.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.user.user.domain.User;
import com.example.user.user.exception.UserNotFoundException;
import com.example.user.user.repository.UserRepository;
import com.example.user.user.request.UserUpdateRequest;
import com.example.user.user.response.UserDto;
import com.example.user.util.oAuth.JwtProperties;
import com.example.user.util.oAuth.OauthToken;
import com.example.user.util.oAuth.provider.Provider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final OAuthService oAuthService;
    private final UserRepository userRepository;

    private static Long getUserIdByJWT(HttpServletRequest request) {
        String jwtHeader = request.getHeader(JwtProperties.HEADER_STRING);
        String token = jwtHeader.replace(JwtProperties.TOKEN_PREFIX, "");

        return JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(token).getClaim("id").asLong();
    }

    public Long getUserId(HttpServletRequest request) {
        return getUserIdByJWT(request);
    }

    public UserDto getCurrentUser(HttpServletRequest request) {
        Long userIdByJWT = getUserIdByJWT(request);
        User user = userRepository.findById(userIdByJWT).orElseThrow(UserNotFoundException::new);
        return user.toUserDto(user);
    }

    @Transactional
    public String getLogin(String code, String provider) {
        Mono<OauthToken> oAuthToken = oAuthService.getOAuthToken(code, provider);
        Long saveUserId = oAuthService.saveUser(oAuthToken, provider);

        return oAuthService.createJWTToken(saveUserId);
    }

    @Transactional
    public String updateMyPage(HttpServletRequest request, UserUpdateRequest userUpdateRequest) {
        Long userId = getUserIdByJWT(request);
        User findUser = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        findUser.updateUser(userUpdateRequest.getNickname(), userUpdateRequest.getDescription());

        return findUser.getNickname();
    }

    @Transactional
    public String deleteUser(HttpServletRequest request) {
        Long userId = getUserIdByJWT(request);
        User findUser = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        findUser.setIsDeleted(true);
        return findUser.getNickname();
    }
}
