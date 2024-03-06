package com.example.user.user.controller;

import com.example.user.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/internal")
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", allowCredentials = "true")
@Tag(name = "UserInternalController", description = "내부(모듈간) API Controller")
public class UserInternalController {
    private final UserService userService;

    @GetMapping("/me")
    @Cacheable(value = "user", key = "'user-' + #request", cacheManager = "cacheManager")
    public ResponseEntity<Long> getCurrentUser(HttpServletRequest request) {
        return ResponseEntity.ok(userService.getUserId(request));
    }

}
