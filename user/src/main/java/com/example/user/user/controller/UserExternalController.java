package com.example.user.user.controller;

import com.example.user.user.domain.User;
import com.example.user.user.request.UserUpdateRequest;
import com.example.user.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/external")
public class UserExternalController {
    private UserService userService;

    public UserExternalController(UserService userService) {
        this.userService = userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/oauth/token")
    public ResponseEntity<String> getLogin(@RequestParam("code") String code, @RequestParam("provider") String provider) {
        return userService.getLogin(code, provider);
    }

    @GetMapping("/me")
    @Cacheable(value = "user", key = "'user-' + #request", cacheManager = "cacheManager")
    public User getCurrentUser(HttpServletRequest request) {
        return userService.getCurrentUser(request);
    }

    @PatchMapping("/updatepage")
    @CacheEvict(value = "user", key = "'user-' + #request", cacheManager = "cacheManager")
    public String updateMyPage(HttpServletRequest request, @RequestBody UserUpdateRequest user) {
        return userService.updateMyPage(request, user);
    }

    @PatchMapping("/deleteuser")
    @CacheEvict(value = "user", key = "'user-' + #request", cacheManager = "cacheManager")
    public String deleteUser(HttpServletRequest request) {
        return userService.deleteUser(request);
    }
}
