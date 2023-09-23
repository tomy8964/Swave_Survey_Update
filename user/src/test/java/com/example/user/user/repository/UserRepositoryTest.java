package com.example.user.user.repository;

import com.example.user.user.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;


@ActiveProfiles("test")
@Transactional
@SpringBootTest
public class UserRepositoryTest {

    @PersistenceContext
    EntityManager em;

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("이름 & Provider로 찾기 테스트")
    public void findByEmailAndProvider() {
        //given
        User user = User.builder()
                .nickname("ham")
                .email("tomy8964@naver.com")
                .provider("kakao")
                .build();
        userRepository.save(user);

        //when
        User findUser = userRepository.findByEmailAndProvider(user.getEmail(), user.getProvider()).get();

        //then
        assertThat(findUser.getEmail()).isEqualTo(user.getEmail());
        assertThat(findUser.getProvider()).isEqualTo(user.getProvider());
    }


}