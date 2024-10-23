package com.vanh.demo_spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.vanh.demo_spring.dto.request.UserCreationRequest;
import com.vanh.demo_spring.entity.User;
import com.vanh.demo_spring.service.UserService;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/users")
    User createUser(@RequestBody UserCreationRequest request) {
        return userService.createUser(request);
    }
}
