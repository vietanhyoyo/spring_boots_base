package com.spring.identity.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.identity.dto.request.ApiResponse;
import com.spring.identity.dto.request.GoogleRegisterRequest;
import com.spring.identity.dto.request.UserCreationRequest;
import com.spring.identity.dto.response.UserResponse;
import com.spring.identity.service.UserService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/register")
@Slf4j
public class RegisterController {
    UserService userService;

    @PostMapping
    ApiResponse<UserResponse> register(@RequestBody @Valid UserCreationRequest request) {
        log.info("Controller: register User");
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();
    }

    @PostMapping("/google")
    ApiResponse<UserResponse> registerWithGoogle(@RequestBody GoogleRegisterRequest request) {
        log.info("Controller: register User with Google");
        return ApiResponse.<UserResponse>builder()
                .result(userService.createGoogleUser(request))
                .build();
    }
}
