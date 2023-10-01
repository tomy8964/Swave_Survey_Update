package com.example.user.user;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.user.user.domain.User;
import com.example.user.user.domain.UserRole;
import com.example.user.user.service.OAuthService;
import com.example.user.user.service.UserService;
import com.example.user.util.oAuth.JwtProperties;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Profile("local")
@Component
@RequiredArgsConstructor
public class InitUser {

    private final InitUserService initUserService;
    private final OAuthService oAuthService;

    @PostConstruct
    public void init() {
        initUserService.init();
    }

    @Component
    static class InitUserService {
        @PersistenceContext
        private EntityManager em;

        @Transactional
        public void init() {

            User user1 = em.find(User.class, 1L);
            String token = JWT.create()
                    .withSubject(user1.getEmail())
                    .withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.EXPIRATION_TIME))
                    .withClaim("id", user1.getId())
                    .withClaim("nickname", user1.getNickname())
                    .sign(Algorithm.HMAC512(JwtProperties.SECRET));
            System.out.println("Bearer " + token);
            if (!user1.getNickname().equals("Ham")) {
                User user = User.builder()
                        .nickname("Ham")
                        .description("admin")
                        .email("tomy8964@naver.com")
                        .userRole(UserRole.USER)
                        .provider("google")
                        .profileImgUrl(null)
                        .build();
                em.persist(user);
            }


        }
    }

}