package com.example.user.user.controller;

import com.example.user.user.domain.User;
import com.example.user.user.request.UserUpdateRequest;
import com.example.user.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "UserExternalController", description = "외부(클라이언트) API Controller")
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
    public User getCurrentUser(HttpServletRequest request) {
        return userService.getCurrentUser(request);
    }

    @PatchMapping("/updatepage")
    public String updateMyPage(HttpServletRequest request, @RequestBody UserUpdateRequest user) {
        return userService.updateMyPage(request, user);
    }

    @PatchMapping("/deleteuser")
    public String deleteUser(HttpServletRequest request) {
        return userService.deleteUser(request);
    }
}
