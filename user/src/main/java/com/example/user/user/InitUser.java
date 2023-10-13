package com.example.user.user;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.user.user.domain.User;
import com.example.user.user.domain.UserRole;
import com.example.user.util.oAuth.JwtProperties;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class InitUser {

    private final InitUserService initUserService;

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
            User user = em.find(User.class, 1L);
            if (user == null) {
                user = User.builder()
                        .nickname("Ham")
                        .description("admin")
                        .email("tomy8964@naver.com")
                        .userRole(UserRole.USER)
                        .provider("google")
                        .profileImgUrl(null)
                        .build();
                em.persist(user);
            }
            String token = JWT.create()
                    .withSubject(user.getEmail())
                    .withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.EXPIRATION_TIME))
                    .withClaim("id", user.getId())
                    .withClaim("nickname", user.getNickname())
                    .sign(Algorithm.HMAC512(JwtProperties.SECRET));
            System.out.println("Bearer " + token);
        }
    }

}