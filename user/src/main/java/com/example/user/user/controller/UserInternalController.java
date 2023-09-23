package com.example.user.user.controller;

import com.example.user.restAPI.service.InterRestApiUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/user/internal")
@RequiredArgsConstructor
public class UserInternalController {
    private final InterRestApiUserService interRestApiUserService;

    @GetMapping("/me")
    public Long getCurrentUser(HttpServletRequest request) {
        return interRestApiUserService.getCurrentUser(request);
    }

}
