package com.example.user.user.controller;

import com.example.user.restAPI.service.InterRestApiUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/internal")
@RequiredArgsConstructor
@Tag(name = "UserInternalController", description = "내부(모듈간) API Controller")
public class UserInternalController {
    private final InterRestApiUserService interRestApiUserService;

    @GetMapping("/me")
    @Cacheable(value = "user", key = "'user-' + #request", cacheManager = "cacheManager" )
    public Long getCurrentUser(HttpServletRequest request) {
        return interRestApiUserService.getCurrentUser(request);
    }

}
