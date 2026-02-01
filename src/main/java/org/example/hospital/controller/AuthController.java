package org.example.hospital.controller;

import jakarta.validation.Valid;
import org.example.hospital.dto.JwtResponse;
import org.example.hospital.dto.LoginRequest;
import org.example.hospital.dto.RegisterRequest;
import org.example.hospital.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    // 前端注册入口，成功返回带 JWT 的响应。
    public ResponseEntity<JwtResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    // 登录接口，供前端用户获取访问令牌。
    public ResponseEntity<JwtResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
