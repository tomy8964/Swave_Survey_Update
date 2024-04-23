package com.example.user.user.service;

import com.example.user.security.jwt.JwtService;
import com.example.user.security.oAuth.OauthToken;
import com.example.user.security.oAuth.profile.Profile;
import com.example.user.security.oAuth.provider.Provider;
import com.example.user.security.oAuth.provider.ProviderList;
import com.example.user.security.oAuth.service.OAuthService;
import com.example.user.user.domain.User;
import com.example.user.user.domain.UserRole;
import com.example.user.user.exception.UserNotFoundException;
import com.example.user.user.repository.UserRepository;
import com.example.user.user.request.UserUpdateRequest;
import com.example.user.user.response.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final OAuthService oAuthService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public UserDto getCurrentUser(HttpServletRequest request) {
        Long userIdByJWT = jwtService.getUserIdByJWT(request);
        User user = userRepository.findById(userIdByJWT).orElseThrow(UserNotFoundException::new);
        return UserDto.builder()
                .profileImgUrl(user.getProfileImgUrl())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .provider(user.getProvider())
                .userRole(user.getUserRole())
                .description(user.getDescription())
                .createTime(user.getCreateTime())
                .build();
    }

    @Transactional
    public String getLogin(String code, String provider) {
        Mono<OauthToken> oAuthToken = oAuthService.getOAuthToken(code, provider);
        User user = userRepository.save(
                findUser(oAuthService.getProfile(oAuthToken, provider).blockOptional()
                                .orElseThrow((UserNotFoundException::new)),
                        provider));
        return jwtService.createJWTToken(user.getEmail(), user.getId(), user.getNickname());
    }

    @Transactional
    public String updateMyPage(HttpServletRequest request, UserUpdateRequest userUpdateRequest) {
        Long userId = jwtService.getUserIdByJWT(request);
        User findUser = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        findUser.updateUser(userUpdateRequest.getNickname(), userUpdateRequest.getDescription());

        return findUser.getNickname();
    }

    @Transactional
    public String deleteUser(HttpServletRequest request) {
        Long userId = jwtService.getUserIdByJWT(request);
        User findUser = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        findUser.setIsDeleted(true);
        return findUser.getNickname();
    }

    private User findUser(Profile profile, String stringProvider) {
        Provider provider = ProviderList.findProvider(stringProvider);
        // 이메일과 프로바이더로 사용자 검색
        Optional<User> existingUser = userRepository.findByEmailAndProvider(profile.getEmail(), provider.getValue());

        // 사용자가 존재하면 반환, 그렇지 않으면 새로운 사용자 생성 후 반환
        return existingUser.orElseGet(() ->
                User.builder()
                        .profileImgUrl(profile.getPicture())
                        .nickname(profile.getName())
                        .email(profile.getEmail())
                        .provider(provider.getValue())
                        .userRole(UserRole.USER)
                        .description("joinBy" + provider.getValue())
                        .build()
        );
    }
}
