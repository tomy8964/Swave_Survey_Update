package com.example.user.user.controller;

import com.example.user.user.domain.User;
import com.example.user.user.request.UserUpdateRequest;
import com.example.user.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/user/external")
@RequiredArgsConstructor
public class UserExternalController {
    private final UserService userService;

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
