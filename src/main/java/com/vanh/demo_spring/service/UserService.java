package com.vanh.demo_spring.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.vanh.demo_spring.dto.request.UserCreationRequest;
import com.vanh.demo_spring.dto.request.UserUpdateRequest;
import com.vanh.demo_spring.dto.response.UserResponse;
import com.vanh.demo_spring.entity.User;
import com.vanh.demo_spring.exception.AppException;
import com.vanh.demo_spring.exception.ErrorCode;
import com.vanh.demo_spring.mapper.UserMapper;
import com.vanh.demo_spring.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    UserMapper userMapper;

    public User createUser(UserCreationRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.USER_EXISTED);

        User user = userMapper.toUser(request);

        return userRepository.save(user);
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public UserResponse getUser(String id) {
        return userMapper
                .toUserResponse(userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found")));
    }

    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        userMapper.updateUser(user, request);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
}
