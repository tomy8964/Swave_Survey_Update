package com.example.user.user.controller;

import com.example.user.user.request.UserUpdateRequest;
import com.example.user.user.response.UserDto;
import com.example.user.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.example.user.security.jwt.JwtRequestFilter.HEADER_STRING;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/external")
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", allowCredentials = "true")
public class UserExternalController {

    private final UserService userService;

    @PostMapping("/oauth/token")
    public ResponseEntity<String> getLogin(@RequestParam("code") String code, @RequestParam("provider") String provider) {
        String jwtToken = userService.getLogin(code, provider);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_STRING, jwtToken);
        return ResponseEntity.ok().headers(headers).body("Login Success");
    }

    @GetMapping("/me")
    @Cacheable(value = "user", key = "'user-' + #request", cacheManager = "cacheManager")
    public ResponseEntity<UserDto> getCurrentUser(HttpServletRequest request) {
        return ResponseEntity.ok(userService.getCurrentUser(request));
    }

    @PatchMapping("/updatepage")
    @CacheEvict(value = "user", key = "'user-' + #request", cacheManager = "cacheManager")
    public ResponseEntity<String> updateMyPage(HttpServletRequest request, @RequestBody UserUpdateRequest user) {
        return ResponseEntity.ok(userService.updateMyPage(request, user) +
                "님의 정보가 변경되었습니다.");
    }

    @PatchMapping("/deleteuser")
    @CacheEvict(value = "user", key = "'user-' + #request", cacheManager = "cacheManager")
    public ResponseEntity<String> deleteUser(HttpServletRequest request) {
        return new ResponseEntity<>(userService.deleteUser(request) + "님의 정보가 삭제되었습니다.", HttpStatus.NO_CONTENT);
    }
}


