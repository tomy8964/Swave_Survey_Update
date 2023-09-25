package com.example.user.user.exception;

import com.example.user.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
public class UserNotFoundExceptionTest {
    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("회원 정보가 없습니다 테스트")
    public void UserNotFoundTest() {
        assertThrows(UserNotFoundException.class, () -> userRepository.findById(1L).orElseThrow(UserNotFoundException::new));
    }

}